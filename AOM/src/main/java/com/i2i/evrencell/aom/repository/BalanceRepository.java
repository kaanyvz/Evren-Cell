package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.exception.NotFoundException;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.request.CreateBalanceRequest;
import com.i2i.evrencell.voltdb.VoltPackageDetails;
import com.i2i.evrencell.voltdb.VoltdbOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.sql.*;

@Repository
public class BalanceRepository {
    private final OracleConnection oracleConnection;
    private final VoltdbOperator voltdbOperator;
    private final Logger logger = LoggerFactory.getLogger(BalanceRepository.class);

    public BalanceRepository(OracleConnection oracleConnection,
                             VoltdbOperator voltdbOperator) {
        this.oracleConnection = oracleConnection;
        this.voltdbOperator = voltdbOperator;
    }


    public ResponseEntity<String> createOracleBalance(CreateBalanceRequest createBalanceRequest) throws ClassNotFoundException, SQLException {
        logger.debug("Creating balance for customer with balanceId: {}", createBalanceRequest.balanceId());
        logger.debug("Creating balance for customer with customerId: {}",  createBalanceRequest.customerId());
        logger.debug("Creating balance for customer with packageId: {}",  createBalanceRequest.packageId());

        logger.debug("Connecting to OracleDb");
        try (Connection connection = oracleConnection.getOracleConnection();) {


            logger.debug("Connected to OracleDb");

            logger.debug("Creating callable statement for SELECT_PACKAGE_DETAILS_ID");
            VoltPackageDetails voltPackageDetails = getOraclePackageDetails(connection, createBalanceRequest);
            Timestamp[] balancePeriod = calculateBalancePeriod(voltPackageDetails.period());

            insertBalanceToOracle(createBalanceRequest, voltPackageDetails, balancePeriod, connection);
            logger.debug("Balance created successfully.");

            return new ResponseEntity<>("Balance created successfully", HttpStatus.CREATED);
        }
    }

    private VoltPackageDetails getOraclePackageDetails(Connection connection, CreateBalanceRequest request){
        logger.debug("Retrieving package details for package id: {}", request.packageId());
        try (CallableStatement balanceStmt = connection.prepareCall("{call SELECT_PACKAGE_DETAILS_ID(?, ?, ?, ?, ?)}")){
            balanceStmt.setInt(1, request.packageId());
            balanceStmt.registerOutParameter(2, Types.INTEGER);
            balanceStmt.registerOutParameter(3, Types.INTEGER);
            balanceStmt.registerOutParameter(4, Types.INTEGER);
            balanceStmt.registerOutParameter(5, Types.INTEGER);

            balanceStmt.execute();

            int amountMinutes = balanceStmt.getInt(2);
            int amountData = balanceStmt.getInt(3);
            int amountSms = balanceStmt.getInt(4);
            int period = balanceStmt.getInt(5);
            if(amountMinutes == 0 && amountSms == 0 && amountData == 0 && period == 0){
                throw new NotFoundException("Package not found in oracle.");
            }
            return new VoltPackageDetails(period, amountMinutes, amountSms, amountData);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertBalanceToOracle(CreateBalanceRequest request,
                                       VoltPackageDetails voltPackageDetails,
                                       Timestamp[] period,
                                       Connection connection) throws SQLException {
        logger.debug("Inserting balance to OracleDb for customerId: {}", request.customerId());
        try (CallableStatement stmt = connection.prepareCall("{call INSERT_BALANCE_TO_CUSTOMER(?, ?, ?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, request.customerId());
            stmt.setInt(2, request.packageId());
            stmt.setInt(3, voltPackageDetails.amountMinutes());
            stmt.setInt(4, voltPackageDetails.amountSms());
            stmt.setInt(5, voltPackageDetails.amountData());
            stmt.setTimestamp(6, period[0]);
            stmt.setTimestamp(7, period[1]);
            stmt.execute();
        }
    }


    private Timestamp[] calculateBalancePeriod(int period) {
        Timestamp sdate = new Timestamp(System.currentTimeMillis());
        Timestamp edate = new Timestamp(sdate.getTime() + period * 24L * 60L * 60L * 1000L);
        return new Timestamp[]{sdate, edate};
    }

    //==VOLTDB==


    public ResponseEntity<String> createVoltBalance(CreateBalanceRequest createBalanceRequest) throws IOException, ProcCallException, InterruptedException {

        logger.debug("Creating balance for customer with balanceId: " + createBalanceRequest.balanceId());
        logger.debug("Creating balance for customer with customerId: " + createBalanceRequest.customerId());
        logger.debug("Creating balance for customer with packageId: " + createBalanceRequest.packageId());
        VoltPackageDetails voltPackageDetails = voltdbOperator.getPackageInfoByPackageId(createBalanceRequest.packageId());
        Timestamp[] balancePeriod = calculateBalancePeriod(voltPackageDetails.period());

        int balanceId = generateVoltBalanceId();
        insertVoltBalance(createBalanceRequest, voltPackageDetails, balancePeriod, balanceId);
        logger.debug("Balance created successfully in VOLTDB");
        return new ResponseEntity<>("Balance created successfully in VOLTDB", HttpStatus.CREATED);
    }

    private void insertVoltBalance(CreateBalanceRequest request,
                                   VoltPackageDetails packageDetails,
                                   Timestamp[] period,
                                   int balanceId){
        logger.debug("Inserting balance to VOLTDB for customer id: {}", request.customerId());
        voltdbOperator.insertBalance(
                balanceId,
                request.customerId(),
                request.packageId(),
                packageDetails.amountMinutes(),
                packageDetails.amountSms(),
                packageDetails.amountData(),
                period[0],
                period[1]
        );
    }

    private int generateVoltBalanceId(){
        logger.debug("Generating new balance id for VOLTDB");
        int maxBalanceId = voltdbOperator.getMaxBalanceId();
        return maxBalanceId + 1;
    }

}

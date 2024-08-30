package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.enumeration.TokenType;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.model.Token;
import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TokenRepository{
    private static final Logger logger = LoggerFactory.getLogger(TokenRepository.class);
    private final OracleConnection oracleConnection;

    public TokenRepository(OracleConnection oracleConnection) {
        this.oracleConnection = oracleConnection;
    }

    /*START FIND ALL VALID TOKENS*/
    public List<Token> findAllValidTokensByUser(Integer userId) throws SQLException, ClassNotFoundException {
        logger.debug("Finding all valid tokens by user id: " + userId);
        Connection connection = oracleConnection.getOracleConnection();
        CallableStatement callableStatement = null;
        ResultSet resultSet = null;

        try {
            callableStatement = prepareFindAllValidTokensByUserStatement(connection, userId);
            resultSet = executeFindAllValidTokensByUser(callableStatement);
            return mapTokensFromResultSet(resultSet);
        } finally {
            closeResources(resultSet, callableStatement, connection);
        }
    }
    private CallableStatement prepareFindAllValidTokensByUserStatement(Connection connection, Integer userId) throws SQLException {
        logger.debug("Preparing SQL statement to find all valid tokens by user id");
        CallableStatement callableStatement = connection.prepareCall("{call FIND_ALL_VALID_TOKENS_BY_USER(?, ?)}");
        callableStatement.setInt(1, userId);
        callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
        return callableStatement;
    }
    private ResultSet executeFindAllValidTokensByUser(CallableStatement callableStatement) throws SQLException {
        logger.debug("Executing SQL statement to find all valid tokens by user id");
        callableStatement.execute();
        return (ResultSet) callableStatement.getObject(2);
    }
    private List<Token> mapTokensFromResultSet(ResultSet resultSet) throws SQLException {
        List<Token> tokens = new ArrayList<>();
        while (resultSet.next()) {
            Token token = Token.builder()
                    .tokenId(resultSet.getInt("ID"))
                    .token(resultSet.getString("TOKEN"))
                    .tokenType(TokenType.valueOf(resultSet.getString("TOKEN_TYPE")))
                    .expired(resultSet.getBoolean("EXPIRED"))
                    .revoked(resultSet.getBoolean("REVOKED"))
                    .userId(resultSet.getInt("USER_ID"))
                    .build();
            tokens.add(token);
        }
        return tokens;
    }
    /*END FIND ALL VALID TOKENS*/

    /*START FIND BY TOKEN*/
    public Optional<Token> findByToken(String tokenValue) {
        logger.debug("Finding token by token value: " + tokenValue);
        Connection connection = null;
        CallableStatement callableStatement = null;
        ResultSet resultSet = null;

        try {
            connection = oracleConnection.getOracleConnection();
            callableStatement = prepareFindByTokenStatement(connection, tokenValue);
            resultSet = executeFindByToken(callableStatement);
            return mapTokenFromResultSet(resultSet);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error while finding token by token value: " + tokenValue, e);
        } finally {
            closeResources(resultSet, callableStatement, connection);
        }
        return Optional.empty();
    }
    private CallableStatement prepareFindByTokenStatement(Connection connection, String tokenValue) throws SQLException {
        logger.debug("Preparing SQL statement to find token by token value");
        CallableStatement callableStatement = connection.prepareCall("{call FIND_TOKEN_BY_VALUE(?, ?)}");
        callableStatement.setString(1, tokenValue);
        callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
        return callableStatement;
    }
    private ResultSet executeFindByToken(CallableStatement callableStatement) throws SQLException {
        logger.debug("Executing SQL statement to find token by token value");
        callableStatement.execute();
        return (ResultSet) callableStatement.getObject(2);
    }
    private Optional<Token> mapTokenFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            Token token = Token.builder()
                    .tokenId(resultSet.getInt("ID"))
                    .token(resultSet.getString("TOKEN"))
                    .tokenType(TokenType.valueOf(resultSet.getString("TOKEN_TYPE")))
                    .expired(resultSet.getBoolean("EXPIRED"))
                    .revoked(resultSet.getBoolean("REVOKED"))
                    .userId(resultSet.getInt("USER_ID"))
                    .build();
            return Optional.of(token);
        }
        return Optional.empty();
    }
    /*END FIND BY TOKEN*/


    /*START ADD TOKEN*/
    public void addToken(Token token) {
        logger.debug("Adding token: " + token);
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = oracleConnection.getOracleConnection();
            callableStatement = prepareAddTokenStatement(connection, token);
            executeAddToken(callableStatement);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error while adding token: " + token, e);
        } finally {
            closeResources(null, callableStatement, connection);
        }
    }
    private CallableStatement prepareAddTokenStatement(Connection connection, Token token) throws SQLException {
        logger.debug("Preparing SQL statement to add token");
        CallableStatement callableStatement = connection.prepareCall("{call INSERT_TOKEN(?, ?, ?, ?, ?)}");
        callableStatement.setString(1, token.getToken());
        callableStatement.setString(2, token.getTokenType().name());
        callableStatement.setString(3, token.isExpired() ? "Y" : "N");
        callableStatement.setString(4, token.isRevoked() ? "Y" : "N");
        callableStatement.setInt(5, token.getUserId());
        return callableStatement;
    }
    private void executeAddToken(CallableStatement callableStatement) throws SQLException {
        logger.debug("Executing SQL statement to add token");
        callableStatement.execute();
    }
    /*END ADD TOKEN*/

    /*START UPDATE TOKEN*/
    public void updateTokenStatus(Token token) {
        logger.debug("Updating token status: " + token);
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = oracleConnection.getOracleConnection();
            callableStatement = prepareUpdateTokenStatusStatement(connection, token);
            executeUpdateTokenStatus(callableStatement);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error while updating token status: " + token, e);
        } finally {
            closeResources(null, callableStatement, connection);
        }
    }
    private CallableStatement prepareUpdateTokenStatusStatement(Connection connection, Token token) throws SQLException {
        logger.debug("Preparing SQL statement to update token status");
        CallableStatement callableStatement = connection.prepareCall("{call UPDATE_TOKEN_STATUS(?, ?, ?)}");
        callableStatement.setInt(1, token.getTokenId());
        callableStatement.setString(2, token.isExpired() ? "Y" : "N");
        callableStatement.setString(3, token.isRevoked() ? "Y" : "N");
        return callableStatement;
    }
    private void executeUpdateTokenStatus(CallableStatement callableStatement) throws SQLException {
        logger.debug("Executing SQL statement to update token status");
        callableStatement.execute();
    }
    private void closeResources(ResultSet resultSet, CallableStatement callableStatement, Connection connection) {
        logger.debug("Closing resources");
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (callableStatement != null) {
                callableStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error while closing resources", e);
        }
    }
    /*END UPDATE TOKEN*/
}

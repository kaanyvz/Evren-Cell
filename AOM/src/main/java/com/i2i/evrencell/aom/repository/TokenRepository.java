package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.enumeration.TokenType;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.model.Token;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(TokenRepository.class);
    private final OracleConnection oracleConnection;

    public TokenRepository(OracleConnection oracleConnection) {
        this.oracleConnection = oracleConnection;
    }

    public List<Token> findAllValidTokensByUser(Integer userId) throws SQLException, ClassNotFoundException {
        logger.debug("Finding all valid tokens by user id: " + userId);
        List<Token> tokens = new ArrayList<>();
        Connection connection = oracleConnection.getOracleConnection();

        logger.debug("Finding all valid tokens by user id");
        CallableStatement callableStatement = connection.prepareCall("{call FIND_ALL_VALID_TOKENS_BY_USER(?, ?)}");
        callableStatement.setInt(1, userId);
        callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
        logger.debug("executing...");
        callableStatement.execute();
        logger.debug("worked...");
        ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
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
        logger.debug("Executed, closing connection");
        resultSet.close();
        callableStatement.close();
        connection.close();
        return tokens;
    }

    public Optional<Token> findByToken(String tokenValue){
        logger.debug("Finding token by token value: " + tokenValue);
        Connection connection = null;
        CallableStatement callableStatement = null;
        ResultSet resultSet = null;
        try {
            connection = oracleConnection.getOracleConnection();
            callableStatement = connection.prepareCall("{call FIND_TOKEN_BY_VALUE(?, ?)}");
            callableStatement.setString(1, tokenValue);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
            callableStatement.execute();
            resultSet = (ResultSet) callableStatement.getObject(2);
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
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error while finding token by token value: " + tokenValue, e);
        } finally {
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
        return Optional.empty();
    }

    public void addToken(Token token){
        logger.debug("Adding token: " + token);
        Connection connection = null;
        CallableStatement callableStatement = null;
        try {
            connection = oracleConnection.getOracleConnection();
            callableStatement = connection.prepareCall("{call INSERT_TOKEN(?, ?, ?, ?, ?)}");
            callableStatement.setString(1, token.getToken());
            callableStatement.setString(2, token.getTokenType().name());
            callableStatement.setString(3, token.isExpired() ? "Y" : "N");
            callableStatement.setString(4, token.isRevoked() ? "Y" : "N");
            callableStatement.setInt(5, token.getUserId());
            callableStatement.execute();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error while adding token: " + token, e);
        } finally {
            try {
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
    }

    public void updateTokenStatus(Token token){
        logger.debug("Updating token status: " + token);
        Connection connection = null;
        CallableStatement callableStatement = null;
        try {
            connection = oracleConnection.getOracleConnection();
            callableStatement = connection.prepareCall("{call UPDATE_TOKEN_STATUS(?, ?, ?)}");
            callableStatement.setInt(1, token.getTokenId());
            callableStatement.setString(2, token.isExpired() ? "Y" : "N");
            callableStatement.setString(3, token.isRevoked() ? "Y" : "N");
            callableStatement.execute();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error while updating token status: " + token, e);
        } finally {
            try {
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
    }
}

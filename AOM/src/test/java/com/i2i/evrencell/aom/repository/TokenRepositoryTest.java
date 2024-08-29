package com.i2i.evrencell.aom.repository;

import com.i2i.evrencell.aom.enumeration.TokenType;
import com.i2i.evrencell.aom.helper.OracleConnection;
import com.i2i.evrencell.aom.model.Token;
import oracle.jdbc.OracleTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TokenRepositoryTest {

    @Mock
    private OracleConnection oracleConnection;

    @InjectMocks
    private TokenRepository tokenRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAllValidTokensByUser_Success() throws SQLException, ClassNotFoundException {
        int userId = 1;
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call FIND_ALL_VALID_TOKENS_BY_USER(?, ?)}")).thenReturn(callableStatement);
        doNothing().when(callableStatement).setInt(1, userId);
        when(callableStatement.getObject(2)).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("TOKEN")).thenReturn("test-token");
        when(resultSet.getString("TOKEN_TYPE")).thenReturn("BEARER");
        when(resultSet.getBoolean("EXPIRED")).thenReturn(false);
        when(resultSet.getBoolean("REVOKED")).thenReturn(false);
        when(resultSet.getInt("USER_ID")).thenReturn(userId);

        List<Token> tokens = tokenRepository.findAllValidTokensByUser(userId);

        assertEquals(1, tokens.size());
        assertEquals("test-token", tokens.get(0).getToken());

        verify(callableStatement).execute();
        verify(resultSet).close();
        verify(callableStatement).close();
        verify(connection).close();
    }

    @Test
    public void testFindByToken_Success() throws SQLException, ClassNotFoundException {
        String tokenValue = "test-token";
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call FIND_TOKEN_BY_VALUE(?, ?)}")).thenReturn(callableStatement);
        doNothing().when(callableStatement).setString(1, tokenValue);
        when(callableStatement.getObject(2)).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("TOKEN")).thenReturn(tokenValue);
        when(resultSet.getString("TOKEN_TYPE")).thenReturn("BEARER");
        when(resultSet.getBoolean("EXPIRED")).thenReturn(false);
        when(resultSet.getBoolean("REVOKED")).thenReturn(false);
        when(resultSet.getInt("USER_ID")).thenReturn(1);

        Optional<Token> token = tokenRepository.findByToken(tokenValue);

        assertTrue(token.isPresent());
        assertEquals(tokenValue, token.get().getToken());

        verify(callableStatement).execute();
        verify(resultSet).close();
        verify(callableStatement).close();
        verify(connection).close();
    }

    @Test
    public void testAddToken_Success() throws SQLException, ClassNotFoundException {
        Token token = Token.builder()
                .tokenId(1)
                .token("test-token")
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .userId(1)
                .build();

        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call INSERT_TOKEN(?, ?, ?, ?, ?)}")).thenReturn(callableStatement);

        doNothing().when(callableStatement).setString(1, token.getToken());
        doNothing().when(callableStatement).setString(2, token.getTokenType().name());
        doNothing().when(callableStatement).setString(3, token.isExpired() ? "Y" : "N");
        doNothing().when(callableStatement).setString(4, token.isRevoked() ? "Y" : "N");
        doNothing().when(callableStatement).setInt(5, token.getUserId());

        tokenRepository.addToken(token);

        verify(callableStatement).execute();
        verify(callableStatement).close();
        verify(connection).close();
    }

    @Test
    public void testUpdateTokenStatus_Success() throws SQLException, ClassNotFoundException {
        Token token = Token.builder()
                .tokenId(1)
                .token("test-token")
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .userId(1)
                .build();

        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(oracleConnection.getOracleConnection()).thenReturn(connection);
        when(connection.prepareCall("{call UPDATE_TOKEN_STATUS(?, ?, ?)}")).thenReturn(callableStatement);

        doNothing().when(callableStatement).setInt(1, token.getTokenId());
        doNothing().when(callableStatement).setString(2, token.isExpired() ? "Y" : "N");
        doNothing().when(callableStatement).setString(3, token.isRevoked() ? "Y" : "N");

        tokenRepository.updateTokenStatus(token);

        verify(callableStatement).execute();
        verify(callableStatement).close();
        verify(connection).close();
    }
}
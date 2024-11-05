package dataaccess.implementations;

import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.AuthDAO;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlAuthDAO implements AuthDAO {

    // added the catch block to make sure that the throw error doesn't pop up
    public MySqlAuthDAO() {
        try{
            this.configureAuthDatabase();
        } catch (DataAccessException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    // inserting into the table the authtoken, username, and the userdata json string for easy access
    @Override
    public AuthData createAuth(UserData user) throws DataAccessException {
        var statement = "INSERT INTO auth (authtoken, username, json) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        user = new UserData(user.username(), hashedPassword, user.email());
        var json = new Gson().toJson(user);
        String authToken = generateToken();
        executeUpdate(statement, authToken, user.username(), json);
        return new AuthData(authToken, user.username());
    }

    // gets the authtoken and userdata string by the authtoken in order to verify the identity of the person
    @Override
    public boolean verifyAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authtoken, json FROM auth WHERE authtoken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    // deletes a row depending on the authtoken for logouts
    @Override
    public boolean deleteAuth(String UUID) throws DataAccessException{
        try{
            var statement = "DELETE FROM auth WHERE authtoken=?";
            executeUpdate(statement, UUID);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auth";
        executeUpdate(statement);
    }

    public UserData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM auth WHERE authtoken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    rs.next();
                    var json = rs.getString("json");
                    return new Gson().fromJson(json, UserData.class);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String) ps.setString(i + 1, (String) param);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public HashMap<String, UserData> getAuthList() throws DataAccessException {
        HashMap<String, UserData> authLst = new HashMap<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auth";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        authLst.put(rs.getString("authtoken"), new Gson().fromJson(rs.getString("json"), UserData.class));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return authLst;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
              `authtoken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              `json` TEXT NOT NULL,
              PRIMARY KEY (`authtoken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureAuthDatabase() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}

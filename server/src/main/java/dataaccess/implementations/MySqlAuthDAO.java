package dataaccess.implementations;

import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.AuthDAO;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.UUID;

public class MySqlAuthDAO implements AuthDAO {

    // added the catch block to make sure that the throw error doesn't pop up
    public MySqlAuthDAO() {
        try{
            DatabaseManager.configureDatabase(createStatements);
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
    public boolean deleteAuth(String authToken) throws DataAccessException{
        try{
            var statement = "DELETE FROM auth WHERE authtoken=?";
            return executeUpdate(statement, authToken) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // clears all the rows in the auth table by truncating everything
    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auth";
        executeUpdate(statement);
    }

    // this function is really only used to get the userdata based on the authtoken
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

    // This is the common method to execute the actual statement for sql
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        return ExecuteUpdate.execute(statement, params);
    }

    // gets a list by selecting all the rows from the table
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

    // Statement to create the table if it doens't already exist
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

    // geenerates the reandom UUID for the authtoken
    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}

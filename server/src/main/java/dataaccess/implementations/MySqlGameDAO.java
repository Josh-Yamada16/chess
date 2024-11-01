package dataaccess.implementations;

import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.AuthDAO;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlGameDAO implements AuthDAO {

    public MySqlGameDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData createAuth(UserData user) throws DataAccessException {
        var statement = "INSERT INTO auth (authtoken, username) VALUES (?, ?)";
        var json = new Gson().toJson(user);
        String authToken = generateToken();
        executeUpdate(statement, authToken, user.username(), json);
        return new AuthData(authToken, user.username());
    }

    @Override
    public boolean verifyAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authtoken, json FROM auth WHERE authtoken=?";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    public Collection<Pet> listPets() throws DataAccessException {
        var result = new ArrayList<Pet>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, json FROM pet";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readPet(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    @Override
    public boolean deleteAuth(String UUID) throws DataAccessException{
        try{
            var statement = "DELETE FROM auth WHERE UUID=?";
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

    private UserData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authtoken FROM auth WHERE authtoken=?";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    var json = rs.getString("json");
                    return new Gson().fromJson(json, UserData.class);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    private Object executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
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

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
              `authtoken` varchar(256) NOT NULL,
              'username' varchar(256) NOT NULL,
              'json' TEXT DEFAULT NULL,
              INDEX(authtoken),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
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

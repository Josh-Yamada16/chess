package dataaccess.implementations;

import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.AuthDAO;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData createAuth(UserData user) throws DataAccessException {
        var statement = "INSERT INTO auth (authtoken, username) VALUES (?, ?)";
        var json = new Gson().toJson(user);
        String token = generateToken();
        executeUpdate(statement, token, user.username(), json);
        return new AuthData(token, user.username());
    }

    @Override
    public boolean verifyAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authtoken, json FROM auth WHERE authtoken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readPet(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
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
        var statement = "TRUNCATE pet";
        executeUpdate(statement);
    }

    public UserData getAuth(String authToken) {
        return auths.get(authToken);
    }

    private UserData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authtoken FROM auth WHERE authtoken=?";
        executeUpdate(statement, authToken);
        return pet.setId(id);
    }

    private Object executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
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
            CREATE TABLE IF NOT EXISTS  pet (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(256) NOT NULL,
              `type` ENUM('CAT', 'DOG', 'FISH', 'FROG', 'ROCK') DEFAULT 'CAT',
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(type),
              INDEX(name)
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

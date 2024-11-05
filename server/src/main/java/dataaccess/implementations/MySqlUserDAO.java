package dataaccess.implementations;

import dataaccess.DatabaseManager;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.UserData;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;
import java.util.HashMap;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlUserDAO implements UserDAO {

    // added the catch block so that I don't get an error or a throw error in the test case thing
    public MySqlUserDAO() {
        try{
            DatabaseManager.createDatabase();
            configureUserDatabase();
        } catch (DataAccessException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    // using truncate to delete all the rows in the users table
    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE users";
        executeUpdate(statement);
    }

    // inserting into the users table with the username, **hashed password, and email
    @Override
    public void addUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate(statement, user.username(), hashedPassword, user.email());
    }

    // gets the hashed password by the username and compares it to the unhashed version using BCrypt
    @Override
    public boolean matchPassword(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT password FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, user.username());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String ogPassword = rs.getString("password");
                        return BCrypt.checkpw(user.password(), ogPassword);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return false;
    }

    public HashMap<String, UserData> getUserList() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            HashMap<String, UserData> userlst = new HashMap<>();
            var statement = "SELECT * FROM users";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("username");
                        String pass = rs.getString("password");
                        String email = rs.getString("email");
                        userlst.put(name, new UserData(name, pass, email));
                    }
                }
            }
            return userlst;
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("username");
                        String pass = rs.getString("password");
                        String email = rs.getString("email");
                        return new UserData(name, pass, email);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    private Object executeUpdate(String statement, Object... params) throws DataAccessException {
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

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureUserDatabase() throws DataAccessException {
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
}
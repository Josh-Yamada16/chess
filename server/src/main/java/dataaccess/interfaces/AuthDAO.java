package dataaccess.interfaces;

import exception.DataAccessException;
import model.AuthData;
import model.UserData;

public interface AuthDAO {
    void clear() throws DataAccessException;

    AuthData createAuth(UserData user) throws DataAccessException;

    boolean verifyAuth(String authToken) throws DataAccessException;

    boolean deleteAuth(String UUID) throws DataAccessException;

    String generateToken();
}

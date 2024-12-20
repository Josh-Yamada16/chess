package dataaccess.interfaces;

import exception.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.HashMap;

public interface AuthDAO {
    void clear() throws DataAccessException;

    AuthData createAuth(UserData user) throws DataAccessException;

    boolean verifyAuth(String authToken) throws DataAccessException;

    boolean deleteAuth(String uuid) throws DataAccessException;

    String generateToken();

    UserData getAuth(String authToken) throws DataAccessException;

    HashMap<String, UserData> getAuthList() throws DataAccessException;
}

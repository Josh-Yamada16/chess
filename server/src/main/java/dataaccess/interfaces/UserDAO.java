package dataaccess.interfaces;

import exception.DataAccessException;
import model.UserData;

import java.util.HashMap;

public interface UserDAO {
    void clear() throws DataAccessException;

    void addUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    boolean matchPassword(UserData user) throws DataAccessException;

    HashMap<String, UserData> getUserList() throws DataAccessException;
}

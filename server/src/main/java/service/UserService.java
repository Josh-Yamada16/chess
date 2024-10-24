package service;

import dataaccess.implementations.MemoryAuthDAO;
import dataaccess.implementations.MemoryUserDAO;
import exception.DataAccessException;
import model.*;

import java.util.HashMap;

public class UserService {

    private final MemoryUserDAO userDao;
    private final MemoryAuthDAO authDao;

    public UserService(MemoryUserDAO userDAO, MemoryAuthDAO authDAO) {
        this.userDao = userDAO;
        this.authDao = authDAO;
    }

    // exceptional handling in the register service point
    public AuthData registerUser(UserData userData) throws DataAccessException {
        if (userDao.getUser(userData.username()) != null) {
            throw new DataAccessException(403, "Error: already taken");
        }
        // if no database setup -> 400 error bad request
        if (userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new DataAccessException(400, "Error: bad request");
        }
        userDao.addUser(userData);
        return authDao.createAuth(userData);
    }

    public AuthData loginUser(UserData user) throws DataAccessException {
        if (userDao.getUser(user.username()) == null){
            throw new DataAccessException(401, "Error: unauthorized");
        }
        if (!userDao.matchPassword(user)){
            throw new DataAccessException(401, "Error: unauthorized");
        }
        return authDao.createAuth(user);
    }

    public void logout(String authToken) throws DataAccessException {
        if (!authDao.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        authDao.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        userDao.clear();
        authDao.clear();
    }

    public UserData getUser(UserData user) {
        return userDao.getUser(user.username());
    }

    public HashMap<String, UserData> getAllUsers() {
        return userDao.getUserList();
    }

}
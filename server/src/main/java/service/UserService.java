package service;

import dataaccess.implementations.MemoryAuthDAO;
import dataaccess.implementations.MemoryUserDAO;
import exception.DataAccessException;
import model.*;

public class UserService {

    private final MemoryUserDAO userDao;
    private final MemoryAuthDAO authDao;

    public UserService(MemoryUserDAO userDAO, MemoryAuthDAO authDAO) {
        this.userDao = userDAO;
        this.authDao = authDAO;
    }

    public AuthData registerUser(UserData userData) throws DataAccessException {
        if (userDao.getUser(userData.username()) != null) {
            throw new DataAccessException(402, "Error: already taken");
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

    public void logout(AuthData auth) {}

    public void deleteAuth(String UUID){
        authDao.deleteAuth(UUID);
    }

    public UserData getUser(String username) {
        return userDao.getUser(username);
    }

    public void clear() throws DataAccessException {
        userDao.clear();
    }


}
package dataaccess;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import server.Server;
import requests.JoinGameRequest;
import service.GameService;
import service.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SqlUserTest {
    static UserDAO userDao;
    static GameDAO gameDao;
    static AuthDAO authDao;

    private static JoinGameRequest createRequest;
    private static UserData existingUser;
    private static UserData newUser;
    private static Server server;

    @BeforeAll
    public static void init() {

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");

        createRequest = new JoinGameRequest(JoinGameRequest.PlayerColor.WHITE,1);

        server = new Server();
        server.run(0);
        authDao = server.getAuthDAO();
        gameDao = server.getGameDAO();
        userDao = server.getUserDAO();
    }

    @BeforeEach
    void clear() throws DataAccessException {
        userDao.clear();
        gameDao.clear();
        authDao.clear();
    }

    @Test
    void testClear() throws DataAccessException{
        userDao.clear();
        assertEquals(0, userDao.getUserList().size());
    }

    @Test
    void testAddUser() throws DataAccessException {
        userDao.addUser(existingUser);
        var userList = userDao.getUserList();
        assertEquals(1, userList.size());
        assertTrue(userList.containsKey(existingUser.username()));
    }

    @Test
    void failAddUser() throws DataAccessException {
        userDao.addUser(existingUser);
        assertThrows(DataAccessException.class,() -> userDao.addUser(new UserData(null, null, null)));
    }

    @Test
    void testMatchPassword() throws DataAccessException {
        userDao.addUser(existingUser);
        assertTrue(userDao.matchPassword(existingUser));
    }

    @Test
    void failMatchPassword() throws DataAccessException {
        userDao.addUser(existingUser);
        assertFalse(userDao.matchPassword(newUser));
    }

    @Test
    void testGetUserList() throws DataAccessException {
        userDao.addUser(existingUser);
        userDao.addUser(newUser);
        HashMap<String, UserData> expected = new HashMap<>();
        expected.put(newUser.username(), existingUser);
        expected.put(existingUser.username(), newUser);

        var actual = userDao.getUserList();
        assertNotEquals(actual.size(), 0);
    }

    @Test
    void failGetUserList() throws DataAccessException {
        userDao.addUser(existingUser);
        userDao.addUser(newUser);
        HashMap<String, UserData> expected = new HashMap<>();
        expected.put(newUser.username(), existingUser);
        expected.put(existingUser.username(), newUser);

        var actual = userDao.getUserList();
        assertNotEquals(actual.size(), 1);
    }

    @Test
    void testGetUser() throws DataAccessException {
        userDao.addUser(existingUser);
        UserData dbUser = userDao.getUser(existingUser.username());
        assertEquals(existingUser.username(), dbUser.username());
        assertTrue(BCrypt.checkpw(existingUser.password(), dbUser.password()));
        assertEquals(existingUser.email(), dbUser.email());
    }

    @Test
    void failGetUser() throws DataAccessException {
        userDao.addUser(existingUser);
        assertNull(userDao.getUser(newUser.username()));
    }
}
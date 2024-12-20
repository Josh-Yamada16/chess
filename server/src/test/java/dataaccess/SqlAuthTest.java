package dataaccess;

import dataaccess.implementations.MySqlAuthDAO;
import dataaccess.implementations.MySqlGameDAO;
import dataaccess.implementations.MySqlUserDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import server.Server;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlAuthTest {
    static AuthDAO authDao = new MySqlAuthDAO();
    static UserDAO userDao = new MySqlUserDAO();
    static GameDAO gameDao = new MySqlGameDAO();

    private static UserData existingUser;
    private static UserData newUser;

    @BeforeAll
    public static void init() {

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    void clear() throws DataAccessException {
        authDao.clear();
        userDao.clear();
        gameDao.clear();
    }

    @Test
    void testClear() throws DataAccessException {
        authDao.createAuth(existingUser);
        authDao.clear();
        assertEquals(0, authDao.getAuthList().size());
    }

    @Test
    void testCreateAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        UserData dbuser = authDao.getAuth(adat.authToken());
        assertEquals(existingUser.username(), dbuser.username());
        assertTrue(BCrypt.checkpw(existingUser.password(), dbuser.password()));
        assertEquals(existingUser.email(), dbuser.email());
    }

    @Test
    void failCreateAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        UserData dbuser = authDao.getAuth(adat.authToken());
        assertNotEquals(newUser.username(), dbuser.username());
        assertFalse(BCrypt.checkpw(newUser.password(), dbuser.password()));
        assertNotEquals(newUser.email(), dbuser.email());
    }

    @Test
    void testVerifyAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        assertTrue(authDao.verifyAuth(adat.authToken()));
    }

    @Test
    void failVerifyAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        assertFalse(authDao.verifyAuth("1234567"));
    }

    @Test
    void testDeleteAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        assertTrue(authDao.deleteAuth(adat.authToken()));
    }

    @Test
    void failDeleteAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        authDao.deleteAuth("1234567");
        assertNotNull(authDao.getAuth(adat.authToken()));
    }

    @Test
    void testGetAuth() throws DataAccessException {
        AuthData adata = authDao.createAuth(new UserData("ExistingUser", "existingUserPassword", "eu@mail.com"));
        UserData udata = authDao.getAuth(adata.authToken());
        assertEquals(existingUser.username(), udata.username());
        assertTrue(BCrypt.checkpw(existingUser.password(), udata.password()));
        assertEquals(existingUser.email(), udata.email());
    }

    @Test
    void failGetAuth() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        UserData udat = authDao.getAuth(adat.authToken());
        assertNotEquals(new UserData("NewUser", "newUserPassword", "nu@mail.com").username(), udat.username());
        assertFalse(BCrypt.checkpw(new UserData("NewUser", "newUserPassword", "nu@mail.com").password(), udat.password()));
        assertNotEquals(new UserData("NewUser", "newUserPassword", "nu@mail.com").email(), udat.email());
    }

    @Test
    void testGetAuthList() throws DataAccessException {
        AuthData adat = authDao.createAuth(existingUser);
        AuthData bdat = authDao.createAuth(existingUser);
        AuthData cdat = authDao.createAuth(existingUser);
        assertEquals(3, authDao.getAuthList().size());
    }

    @Test
    void failGetAuthList() throws DataAccessException {
        AuthData bdat = authDao.createAuth(existingUser);
        AuthData cdat = authDao.createAuth(existingUser);
        assertNotEquals(1, authDao.getAuthList().size());
    }
}
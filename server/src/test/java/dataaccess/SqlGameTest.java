package dataaccess;

import dataaccess.implementations.MySqlAuthDAO;
import dataaccess.implementations.MySqlGameDAO;
import dataaccess.implementations.MySqlUserDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlGameTest {
    static UserDAO userDao = new MySqlUserDAO();
    static GameDAO gameDao = new MySqlGameDAO();
    static AuthDAO authDao = new MySqlAuthDAO();

    private static JoinGameRequest createRequest;
    private static UserData existingUser;

    @BeforeAll
    public static void init() {

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        createRequest = new JoinGameRequest(JoinGameRequest.PlayerColor.WHITE,1);

    }

    @BeforeEach
    void clear() throws DataAccessException {
        gameDao.clear();
        userDao.clear();
        authDao.clear();
    }

    // GAME TESTS
    @Test
    void successCreateGame() throws DataAccessException {
        int gameID = gameDao.createGame("newGame");
        assertEquals(1, gameID);
        assertEquals("newGame", gameDao.getGame(gameID).gameName());
    }

    @Test
    void failCreateGame() throws DataAccessException {
        gameDao.createGame("newGame");
        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameDao.createGame(null));
        assertEquals(500, ex.statusCode());
    }

    @Test
    void listGames() throws DataAccessException {
        List<Integer> expected = new ArrayList<>();
        expected.add(gameDao.createGame("game1"));
        expected.add(gameDao.createGame("game2"));
        expected.add(gameDao.createGame("game3"));

        var actual = gameDao.onlyGames();
        assertIterableEquals(expected, actual);
    }

    @Test
    void failListGames() throws DataAccessException {
        gameDao.createGame("newGame");
        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameDao.createGame(null));
        var actual = gameDao.listGames();
        assertNotEquals(0, actual.size());
    }

    @Test
    void successOnlyGames() throws DataAccessException {
        gameDao.createGame("newGame");
        gameDao.createGame("newGame2");
        assertEquals(2, gameDao.onlyGames().size());
    }

    @Test
    void failOnlyGames() throws DataAccessException{
        gameDao.createGame("newGame");
        assertThrows(DataAccessException.class,() -> gameDao.createGame(null));
        assertNotEquals(2, gameDao.onlyGames().size());
    }

    @Test
    void successGetGame() throws DataAccessException {
        int gameID = gameDao.createGame("newGame");
        assertInstanceOf(GameData.class, gameDao.getGame(gameID));
    }

    @Test
    void failGetGame() throws DataAccessException {
        gameDao.createGame("newGame");
        assertNull(gameDao.getGame(2));
    }

    @Test
    void testAddPlayer() throws DataAccessException {
        int gameID = gameDao.createGame("newGame");
        gameDao.addPlayer(gameID, createRequest.getPlayerColor(), existingUser.username());
        assertEquals(existingUser.username(), gameDao.getGame(gameID).whiteUsername());
    }

    @Test
    void failAddPlayer() throws DataAccessException {
        int gameID = gameDao.createGame("newGame");
        gameDao.addPlayer(gameID, createRequest.getPlayerColor(), existingUser.username());
        assertFalse(gameDao.addPlayer(gameID, createRequest.getPlayerColor(), existingUser.username()));
    }

    @Test
    void testClear() throws DataAccessException{
        gameDao.createGame("newGame");
        gameDao.clear();
        assertEquals(0, gameDao.onlyGames().size());
    }
}
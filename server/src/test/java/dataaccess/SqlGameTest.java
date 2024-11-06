package dataaccess;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlGameTest {
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

        createRequest = new JoinGameRequest(JoinGameRequest.playerColor.WHITE,1);

        server = new Server();
        server.run(0);
        authDao = server.getAuthDAO();
        gameDao = server.getGameDAO();
        userDao = server.getUserDAO();
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
        assertEquals(500, ex.StatusCode());
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

//    @Test
//    void testAddPlayer() throws DataAccessException {
//        AuthData auth = userService.registerUser(existingUser);
//        int gameID = gameService.createGame("newGame", auth.authToken());
//        gameService.joinGame(createRequest, auth.authToken());
//        assertEquals(existingUser.username(), gameService.getGame(gameID).whiteUsername());
//    }
//
//    @Test
//    void failAddPlayer() throws DataAccessException {
//        AuthData auth = userService.registerUser(existingUser);
//        int gameID = gameService.createGame("newGame", auth.authToken());
//        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameService.joinGame(new JoinGameRequest(null, gameID), auth.authToken()));
//        assertEquals(400, ex.StatusCode());
//        assertEquals("Error: bad request", ex.getMessage());
//    }
//
//    @Test
//    void testClear() throws DataAccessException{
//        AuthData auth = userService.registerUser(existingUser);
//        gameService.createGame("newGame", auth.authToken());
//        gameService.clear();
//        assertEquals(0, gameService.onlyGames().size());
//        assertEquals(0, authDao.getAuthList().size());
//    }
}
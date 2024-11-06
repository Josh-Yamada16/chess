package service;

import dataaccess.implementations.MemoryAuthDAO;
import dataaccess.implementations.MemoryGameDAO;
import dataaccess.implementations.MemoryUserDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryServiceTests {
    static UserDAO userDao;
    static GameDAO gameDao;
    static AuthDAO authDao;
    static GameService gameService;
    static UserService userService;

    private static JoinGameRequest createRequest;
    private static UserData existingUser;
    private static UserData newUser;

    @BeforeAll
    public static void init() {

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");

        createRequest = new JoinGameRequest(JoinGameRequest.PlayerColor.WHITE,1);

        authDao = new MemoryAuthDAO();
        gameDao = new MemoryGameDAO();
        userDao = new MemoryUserDAO();
        gameService = new GameService(gameDao, authDao);
        userService = new UserService(userDao, authDao);
    }

    @BeforeEach
    void clear() throws DataAccessException {
        gameService.clear();
        userService.clear();
    }


    // USER TESTS
    @Test
    void successRegisterUser() throws DataAccessException {
        var user = new UserData("fourarms", "Shoji#16", "fourarms216@gmail.com");
        userService.registerUser(user);
        var userList = userService.getAllUsers();
        assertEquals(1, userList.size());
        assertTrue(userList.containsKey(user.username()));
    }

    @Test
    void doubleRegisterUser() throws DataAccessException {
        var user = new UserData("fourarms", "Shoji#16", "fourarms216@gmail.com");
        userService.registerUser(user);
        DataAccessException ex = assertThrows(DataAccessException.class,() -> userService.registerUser(user));
        assertEquals(403, ex.statusCode());
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void validLogin() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        userService.logout(auth.authToken());
        userService.loginUser(existingUser);
        assertNotNull(userService.getUser(existingUser));
        assertEquals(1, userService.getAllUsers().size());
        assertEquals(1, authDao.getAuthList().size());
    }

    @Test
    void unauthorizedLogin() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        userService.logout(auth.authToken());
        DataAccessException ex = assertThrows(DataAccessException.class,() -> userService.loginUser(newUser));
        assertEquals(401, ex.statusCode());
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void validLogout() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        userService.logout(auth.authToken());
        assertEquals(1, userService.getAllUsers().size());
        assertEquals(0, authDao.getAuthList().size());
    }

    @Test
    void unauthorizedLogout() throws DataAccessException {
        userService.registerUser(existingUser);
        DataAccessException ex = assertThrows(DataAccessException.class,() -> userService.logout("123"));
        assertEquals(401, ex.statusCode());
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void clearUserService() throws DataAccessException{
        userService.clear();
        assertEquals(0, userService.getAllUsers().size());
        assertEquals(0, authDao.getAuthList().size());
    }

    @Test
    void testGetUser() throws DataAccessException {
        userService.registerUser(existingUser);
        UserData dbUser = userService.getUser(existingUser);
        assertEquals(existingUser.username(), dbUser.username());
        assertTrue(BCrypt.checkpw(existingUser.password(), dbUser.password()));
        assertEquals(existingUser.email(), dbUser.email());
    }

    @Test
    void failGetUser() throws DataAccessException {
        userService.registerUser(existingUser);
        assertEquals(null, userService.getUser(newUser));
    }

    // GAME TESTS
    @Test
    void successCreateGame() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        int gameID = gameService.createGame("newGame", auth.authToken());
        assertEquals(1, gameService.onlyGames().size());
        assertEquals("newGame", gameService.getGame(gameID).gameName());
    }

    @Test
    void failCreateGame() throws DataAccessException {
        userService.registerUser(existingUser);
        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameService.createGame("newGame", "123"));
        assertEquals(401, ex.statusCode());
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void successJoinGame() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        int gameID = gameService.createGame("newGame", auth.authToken());
        gameService.joinGame(createRequest, auth.authToken());
        assertEquals(existingUser.username(), gameService.getGame(gameID).whiteUsername());
    }

    @Test
    void failJoinGame() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        int gameID = gameService.createGame("newGame", auth.authToken());
        DataAccessException ex = assertThrows(DataAccessException.class,() ->
                gameService.joinGame(new JoinGameRequest(null, gameID), auth.authToken()));
        assertEquals(400, ex.statusCode());
        assertEquals("Error: bad request", ex.getMessage());
    }

    @Test
    void listGames() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        List<Integer> expected = new ArrayList<>();
        expected.add(gameService.createGame("game1", auth.authToken()));
        expected.add(gameService.createGame("game2", auth.authToken()));
        expected.add(gameService.createGame("game3", auth.authToken()));

        var actual = gameService.onlyGames();
        assertIterableEquals(expected, actual);
    }

    @Test
    void failListGames() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        gameService.createGame("newGame", auth.authToken());
        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameService.listGames("123"));
        assertEquals(401, ex.statusCode());
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void clearGameService() throws DataAccessException{
        AuthData auth = userService.registerUser(existingUser);
        gameService.createGame("newGame", auth.authToken());
        gameService.clear();
        assertEquals(0, gameService.onlyGames().size());
        assertEquals(0, authDao.getAuthList().size());
    }

    @Test
    void successOnlyGames() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        gameService.createGame("newGame", auth.authToken());
        gameService.createGame("newGame2", auth.authToken());
        assertEquals(2, gameService.onlyGames().size());
    }

    @Test
    void failOnlyGames() throws DataAccessException{
        AuthData auth = userService.registerUser(existingUser);
        gameService.createGame("newGame", auth.authToken());
        assertThrows(DataAccessException.class,() -> gameService.createGame("newGame2", "123"));
        assertNotEquals(2, gameService.onlyGames().size());
    }

    @Test
    void successGetGame() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        int gameID = gameService.createGame("newGame", auth.authToken());
        assertInstanceOf(GameData.class, gameService.getGame(gameID));
    }

    @Test
    void failGetGame() throws DataAccessException {
        AuthData auth = userService.registerUser(existingUser);
        gameService.createGame("newGame", auth.authToken());
        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameService.getGame(2));
        assertEquals(404, ex.statusCode());
        assertEquals("Error: Game not found", ex.getMessage());
    }
}
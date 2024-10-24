package passoff;

import chess.ChessGame;
import dataaccess.implementations.*;
import exception.DataAccessException;
import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import passoff.model.TestCreateRequest;
import passoff.model.TestUser;
import passoff.server.TestServerFacade;
import server.Server;
import server.requests.JoinGameRequest;
import service.GameService;
import service.UserService;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTests {
    static final MemoryUserDAO userDao = new MemoryUserDAO();
    static final MemoryGameDAO gameDao = new MemoryGameDAO();
    static final MemoryAuthDAO authDao = new MemoryAuthDAO();
    static final GameService gameService = new GameService(gameDao, authDao);
    static final UserService userService = new UserService(userDao, authDao);

    private static JoinGameRequest createRequest;
    private static UserData existingUser;
    private static UserData newUser;
    private String existingAuth;

    @BeforeAll
    public static void init() {

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");

        createRequest = new JoinGameRequest(JoinGameRequest.playerColor.WHITE,1);
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
        assertEquals(403, ex.StatusCode());
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
        assertEquals(401, ex.StatusCode());
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
        assertEquals(401, ex.StatusCode());
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
        assertEquals(existingUser, userService.getUser(existingUser));
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
        assertEquals(401, ex.StatusCode());
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
        DataAccessException ex = assertThrows(DataAccessException.class,() -> gameService.joinGame(new JoinGameRequest(null, gameID), auth.authToken()));
        assertEquals(400, ex.StatusCode());
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
        assertEquals(401, ex.StatusCode());
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
        assertEquals(404, ex.StatusCode());
        assertEquals("Error: Game not found", ex.getMessage());
    }
}
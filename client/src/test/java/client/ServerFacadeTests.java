package client;

import chess.ChessGame;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import requests.JoinGameRequest;
import server.Server;
import server.ServerFacade;

import java.net.HttpURLConnection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static requests.JoinGameRequest.PlayerColor.WHITE;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static UserData existingUser, newUser;
    private static GameData newGame;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(String.format("http://localhost:%s", port));

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
        newGame = new GameData(0, null, null, "testGame", null);
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        serverFacade.clear();
        serverFacade.registerUser(existingUser);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Normal User Registration")
    public void successRegister() throws DataAccessException {
        AuthData registerResult = serverFacade.registerUser(newUser);
        Assertions.assertEquals(newUser.username(), registerResult.username(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(registerResult.authToken(), "Response did not contain an authentication string");
    }

    @Test
    @DisplayName("Re-Register User")
    public void registerTwice() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> serverFacade.registerUser(existingUser));
    }

    @Test
    @DisplayName("Normal User Login")
    public void successLogin() throws DataAccessException {
        AuthData loginResult = serverFacade.login(existingUser.username(), existingUser.password());
        Assertions.assertEquals(existingUser.username(), loginResult.username(),
                "Response did not give the same username as user");
        Assertions.assertNotNull(loginResult.authToken(), "Response did not return authentication String");
    }

    @Test
    @DisplayName("Login Invalid User")
    public void loginInvalidUser() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> serverFacade.login(newUser.username(), newUser.password()));
    }

    @Test
    @DisplayName("Normal Logout")
    public void successLogout() throws DataAccessException {
        AuthData loginResult = serverFacade.login(existingUser.username(), existingUser.password());
        boolean result = serverFacade.logout(loginResult.authToken());
        assertTrue(result);
    }

    @Test
    @DisplayName("Invalid Auth Logout")
    public void failLogout() throws DataAccessException {
        AuthData loginResult = serverFacade.login(existingUser.username(), existingUser.password());
        serverFacade.logout(loginResult.authToken());
        assertFalse(serverFacade.logout(loginResult.authToken()));
    }

    @Test
    @DisplayName("List Multiple Games")
    public void gamesList() throws DataAccessException {
        AuthData user = serverFacade.login(existingUser.username(), existingUser.password());
        UserData userA = new UserData("a", "A", "a.A");
        UserData userB = new UserData("b", "B", "b.B");
        UserData userC = new UserData("c", "C", "c.C");
        AuthData authA = serverFacade.registerUser(userA);
        AuthData authB = serverFacade.registerUser(userB);
        AuthData authC = serverFacade.registerUser(userC);
        //create games
        Collection<GameData> expectedList = new HashSet<>();
        //1 as black from A
        String game1Name = "I'm numbah one!";
        boolean game1 = serverFacade.createGame(game1Name, authA.authToken());
        assertTrue(game1);
        expectedList.add(new GameData(1, null, null, game1Name, new ChessGame()));
        //1 as white from B
        String game2Name = "Lonely";
        boolean game2 = serverFacade.createGame(game2Name, authB.authToken());
        assertTrue(game2);
        expectedList.add(new GameData(2, null, null, game2Name, new ChessGame()));
        //1 of each from C
        String game3Name = "GG";
        boolean game3 = serverFacade.createGame(game3Name, authC.authToken());
        assertTrue(game3);
        expectedList.add(new GameData(3, null, null, game2Name, new ChessGame()));
        //list games
        ArrayList<GameData> listResult = serverFacade.listGames(user.authToken());
        assertNotNull(listResult);
        Collection<GameData> returnedList = new HashSet<>(listResult);
        //check
        Assertions.assertEquals(expectedList.size(), returnedList.size(), "Returned Games list was incorrect");
    }

    @Test
    @DisplayName("Unique Authtoken Each Login")
    public void uniqueAuthorizationTokens() throws DataAccessException {
        AuthData loginOne = serverFacade.login(existingUser.username(), existingUser.password());
        Assertions.assertNotNull(loginOne.authToken(), "Login result did not contain an authToken");

        AuthData loginTwo = serverFacade.login(existingUser.username(), existingUser.password());
        Assertions.assertNotNull(loginTwo.authToken(), "Login result did not contain an authToken");

        boolean createResult = serverFacade.createGame("createRequest", loginOne.authToken());
        assertTrue(createResult);

        serverFacade.logout(loginTwo.authToken());
        assertFalse(serverFacade.createGame("createGameTwo", loginTwo.authToken()));

        ArrayList<GameData> listResult = serverFacade.listGames(loginOne.authToken());
        assertNotNull(listResult);
        Assertions.assertEquals(1, listResult.size());
    }

    @Test
    @DisplayName("Valid Creation")
    public void goodCreate() throws DataAccessException {
        AuthData user = serverFacade.login(existingUser.username(), existingUser.password());
        boolean createResult = serverFacade.createGame("createRequest", user.authToken());
        assertTrue(createResult);
    }

    @Test
    @DisplayName("Create with Bad Authentication")
    public void badAuthCreate() throws DataAccessException {
        boolean createResult = serverFacade.createGame("createRequest", "123");
        assertFalse(createResult);
    }

    @Test
    @DisplayName("Join Created Game")
    public void goodJoin() throws DataAccessException {
        AuthData user = serverFacade.login(existingUser.username(), existingUser.password());
        //create game
        serverFacade.createGame("createRequest", user.authToken());
        //join as white
        JoinGameRequest joinRequest = new JoinGameRequest(WHITE, 1);
        //try join
        boolean joinResult = serverFacade.joinGame(joinRequest, user.authToken());
        //check
        assertTrue(joinResult);
        ArrayList<GameData> listResult = serverFacade.listGames(user.authToken());
        Assertions.assertEquals(1, listResult.size());
        Assertions.assertEquals(existingUser.username(), listResult.get(0).whiteUsername());
        Assertions.assertNull(listResult.get(0).blackUsername());
    }

    @Test
    @DisplayName("Join Bad Authentication")
    public void badAuthJoin() throws DataAccessException {
        AuthData user = serverFacade.login(existingUser.username(), existingUser.password());
        //create game
        serverFacade.createGame("createRequest", user.authToken());
        JoinGameRequest joinRequest = new JoinGameRequest(WHITE, 1);
        //try join as white
        boolean joinResult = serverFacade.joinGame(joinRequest, "bad stuff");
        //check
        assertFalse(joinResult);
    }

    @Test
    @DisplayName("Clear Test")
    public void clearData() throws DataAccessException {
        AuthData user = serverFacade.login(existingUser.username(), existingUser.password());
        //create filler games
        serverFacade.createGame("Mediocre game", user.authToken());
        serverFacade.createGame("Awesome game", user.authToken());
        //log in new user
        UserData user1 = new UserData("ClearMe", "cleared", "clear@mail.com");
        AuthData registerResult = serverFacade.registerUser(user1);
        //create and join game for new user
        boolean createResult = serverFacade.createGame("Clear game",
                registerResult.authToken());
        JoinGameRequest joinRequest = new JoinGameRequest(WHITE, 1);
        serverFacade.joinGame(joinRequest, registerResult.authToken());
        //do clear
        serverFacade.clear();
        //first user
        assertThrows(DataAccessException.class, () -> serverFacade.login(existingUser.username(), existingUser.password()));
        //second user
        assertThrows(DataAccessException.class, () -> serverFacade.login(user1.username(), user1.password()));
        //try to use old auth token to list games
        assertNull(serverFacade.listGames(user.authToken()));
        //log in new user and check that list is empty
        registerResult = serverFacade.registerUser(existingUser);
        assertNotNull(registerResult);
        ArrayList<GameData> listResult = serverFacade.listGames(registerResult.authToken());
        assertNotNull(listResult);
        //check listResult
        Assertions.assertEquals(0, listResult.size(), "list result did not return 0 games after clear");
    }

}

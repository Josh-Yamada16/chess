package server;

import com.google.gson.Gson;
import dataaccess.implementations.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.*;
//import server.websocket.WebSocketHandler;
import requests.JoinGameRequest;
import service.*;

import spark.*;

import java.util.Map;

public class Server {
    private UserService userService;
    private GameService gameService;
    private UserDAO userDAO = new MySqlUserDAO();
    private GameDAO gameDAO = new MySqlGameDAO();
    private AuthDAO authDAO = new MySqlAuthDAO();
//    private final WebSocketHandler webSocketHandler;

    public Server() {
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
//        webSocketHandler = new WebSocketHandler();
    }

    public Server(int memory){
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    public int run(int port) {
        Spark.port(port);

        Spark.staticFiles.location("web");
        Spark.init();
//        Spark.webSocket("/ws", webSocketHandler);

        Spark.post("/user", this::registerUser); // Registration
        Spark.post("/session", this::login); // Login
        Spark.delete("/session", this::logout); // Logout
        Spark.get("/game", this::listGames); // List Games
        Spark.post("/game", this::createGame); // Create Game
        Spark.put("/game", this::joinGame); // Join Game
        Spark.delete("/db", this::clear); // Clear Application
        Spark.exception(DataAccessException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        res.status(ex.statusCode());
    }

    // Added exception handling for the login endpoint
    private Object login(Request req, Response res) {
        try{
            var user = new Gson().fromJson(req.body(), UserData.class);
            return new Gson().toJson(userService.loginUser(user));
        } catch (DataAccessException ex){
            res.status(ex.statusCode());
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    // added exception handling for the logout endpoint
    private Object logout(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            userService.logout(authToken);
            return new Gson().toJson(null);
        } catch (DataAccessException ex){
            res.status(ex.statusCode());
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    // added exception handling for the register endpoint
    private Object registerUser(Request req, Response res) {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            AuthData auth = userService.registerUser(user);
            return new Gson().toJson(auth);
        } catch (DataAccessException ex){
            res.status(ex.statusCode());
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    // added exception handling for the listgames endpoint
    private Object listGames(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            var list = gameService.listGames(authToken).toArray();
            return new Gson().toJson(Map.of("games", list));
        } catch (DataAccessException ex){
            res.status(ex.statusCode());
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    private Object createGame(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            var game = new Gson().fromJson(req.body(), GameData.class);
            int gameID = gameService.createGame(game.gameName(), authToken);
            return new Gson().toJson(Map.of("gameID", gameID));
        } catch (DataAccessException ex){
            res.status(ex.statusCode());
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    private Object joinGame(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            var game = new Gson().fromJson(req.body(), JoinGameRequest.class);
            gameService.joinGame(game, authToken);
            return new Gson().toJson(null);
        } catch (DataAccessException ex){
            res.status(ex.statusCode());
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        userService.clear();
        gameService.clear();
        res.status(200);
        return new Gson().toJson(null);
    }

    public UserService getUserService() {
        return userService;
    }

    public GameService getGameService() {
        return gameService;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }

    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
}
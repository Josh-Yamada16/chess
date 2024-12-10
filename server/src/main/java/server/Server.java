package server;

import com.google.gson.Gson;
import dataaccess.implementations.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.DataAccessException;
import model.*;
import requests.JoinGameRequest;
import service.*;

import spark.*;
import websocket.WebSocketHandler;
import java.util.Map;

public class Server {
    private UserService userService;
    private GameService gameService;
    private UserDAO userDAO = new MySqlUserDAO();
    private GameDAO gameDAO = new MySqlGameDAO();
    private AuthDAO authDAO = new MySqlAuthDAO();

    public Server() {
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
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

        Spark.webSocket("/ws", WebSocketHandler.class);

        Spark.post("/user", this::registerUser); // Registration
        Spark.post("/session", this::login); // Login
        Spark.delete("/session", this::logout); // Logout
        Spark.get("/game", this::listGames); // List Games
        Spark.post("/game", this::createGame); // Create Game
        Spark.put("/game", this::joinGame); // Join Game
        Spark.put("/update", this::update);
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

    // added exception handling for the listgames endpoint
    private Object listGames(Request req, Response res) {
        try{
            res.type("application/json");
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

    private Object update(Request req, Response res){
        try{
            var game = new Gson().fromJson(req.body(), GameData.class);
            gameService.updateGame(game.gameID(), game.game());
            return new Gson().toJson(null);
        } catch (DataAccessException e) {
            return new Gson().toJson(Map.of("message", e.getMessage()));
        }
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        userService.clear();
        gameService.clear();
        res.status(200);
        return new Gson().toJson(null);
    }
}
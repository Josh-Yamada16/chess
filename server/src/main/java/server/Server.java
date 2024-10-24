package server;

import com.google.gson.Gson;
import dataaccess.implementations.*;
import exception.DataAccessException;
import model.*;
//import server.websocket.WebSocketHandler;
import server.requests.*;
import service.*;

import spark.*;

import java.util.Map;

public class Server {
    private UserService userService;
    private GameService gameService;
//    private final WebSocketHandler webSocketHandler;

    public Server() {
        userService = null;
        gameService = null;
//        webSocketHandler = new WebSocketHandler();
    }

    public int run(int port) {
        Spark.port(port);

        Spark.staticFiles.location("web");
        Spark.init();
        MemoryGameDAO gameDAo = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryUserDAO userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAo, authDAO);

//        Spark.webSocket("/ws", webSocketHandler);

        Spark.post("/user", this::registerUser); // Registration
        Spark.post("/session", this::login); // Login
        Spark.delete("/session", this::logout); // Logout
//        Spark.get("/game", this::listGames); // List Games
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
        res.status(ex.StatusCode());
    }

    private Object login(Request req, Response res) throws DataAccessException {
        try{
            var user = new Gson().fromJson(req.body(), UserData.class);
            return new Gson().toJson(userService.loginUser(user));
        } catch (DataAccessException ex){
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        return null;
//        var auth = new Gson().fromJson(req.headers(), AuthData.class);
    }

    private Object registerUser(Request req, Response res) throws DataAccessException {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            AuthData auth = userService.registerUser(user);
            return new Gson().toJson(auth);
        } catch (DataAccessException ex){
            return new Gson().toJson(Map.of("message", ex.getMessage()));
        }
    }

//    private Object getGame(Request req, Response res) throws DataAccessException {
//
//    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        res.type("application/json");
        var list = gameService.listGames().toArray();
        return new Gson().toJson(Map.of("games", list));
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        var game = new Gson().fromJson(req.body(), GameData.class);
        int gameID = gameService.createGame(game.gameName());
        return new Gson().toJson(Map.of("gameID", gameID));
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        var game = new Gson().fromJson(req.body(), JoinGameRequest.class);
        gameService.joinGame(game);
        return new Gson().toJson(null);
    }


    private Object deleteAuth(Request req, Response res) throws DataAccessException {
        var username = req.params(":username");
        var user = userService.getUser(username);
        if (user != null) {
//            userService.deleteAuth(id);
//            webSocketHandler.makeNoise(pet.name(), pet.sound());
            res.status(204);
        } else {
            res.status(404);
        }
        return "";
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        userService.clear();
        gameService.clear();
        res.status(204);
        return "";
    }
}
package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import exception.ResponseException;
import model.*;
//import server.websocket.WebSocketHandler;
import org.eclipse.jetty.server.Authentication;
import service.Service;
import spark.*;

import java.util.Map;

public class Server {
    private Service service;
//    private final WebSocketHandler webSocketHandler;

    public Server() {
        service = null;
//        webSocketHandler = new WebSocketHandler();
    }

    public int run(int port) {
        Spark.port(port);

        Spark.staticFiles.location("web");
        Spark.init();
        DataAccess dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);

//        Spark.webSocket("/ws", webSocketHandler);

        Spark.post("/user", this::registerUser); // Registration
        Spark.post("/session", this::loginUser); // Login
//        Spark.delete("/session", this::deleteAuth); // Logout
//        Spark.get("/game", this::listGames); // List Games
//        Spark.post("/game", this::getGame); // Create Game
//        Spark.put("/game", this::addUser); // Join Game
//        Spark.delete("/db", this::deleteAllData); // Clear Application
        Spark.exception(ResponseException.class, this::exceptionHandler);

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

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.StatusCode());
    }

    private Object loginUser(Request req, Response res) throws ResponseException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        return service.loginUser(user);
    }

    private Object registerUser(Request req, Response res) throws ResponseException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        AuthData auth = service.registerUser(user);
//        webSocketHandler.makeNoise(pet.username(), pet.sound());
        return new Gson().toJson(auth);
    }

    private Object getGame(Request req, Response res) throws ResponseException {

    }

    private Object listGames(Request req, Response res) throws ResponseException {
        res.type("application/json");
        var list = service.listGames().toArray();
        return new Gson().toJson(Map.of("games", list));
    }


    private Object deleteAuth(Request req, Response res) throws ResponseException {
        var username = req.params(":username");
        var user = service.getUser(username);
        if (user != null) {
            service.deleteAuth(id);
//            webSocketHandler.makeNoise(pet.name(), pet.sound());
            res.status(204);
        } else {
            res.status(404);
        }
        return "";
    }

    private Object deleteAllData(Request req, Response res) throws ResponseException {
        service.deleteAllData();
        res.status(204);
        return "";
    }
}
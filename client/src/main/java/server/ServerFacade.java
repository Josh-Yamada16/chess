package server;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.DataAccessException;
import model.*;
import requests.JoinGameRequest;
import websocket.NotificationHandler;
import websocket.commands.*;

import javax.websocket.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerFacade extends Endpoint {

    private final String serverUrl;
    private Session session;
    private NotificationHandler notificationHandler;

    public ServerFacade(String url, NotificationHandler notificationHandler) throws URISyntaxException, DeploymentException, IOException {
        serverUrl = url;
        this.notificationHandler = notificationHandler;
    }

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData registerUser(UserData user) throws DataAccessException {
        var path = "/user";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws DataAccessException {
        try {
            var path = "/session";
            UserData user = new UserData(username, password, null);
            return this.makeRequest("POST", path, user, AuthData.class, null);
        } catch (DataAccessException ex) {
            throw new DataAccessException(ex.statusCode(), ex.getMessage());
        }
    }

    public boolean logout(String authToken) {
        var path = "/session";
        try{
            this.makeRequest("DELETE", path, null, null, authToken);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    public ArrayList<GameData> listGames(String authToken) throws DataAccessException {
        try{
            var path = "/game";
            record ListGamesResponse(ArrayList<GameData> games) {
            }
            var response = this.makeRequest("GET", path, null, ListGamesResponse.class, authToken);
            return response.games();
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public boolean createGame(String gameName, String authToken) throws DataAccessException {
        try{
            var path = "/game";
            GameData game = new GameData(0, null, null, gameName, null);
            this.makeRequest("POST", path, game, null, authToken);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean joinGame(JoinGameRequest request, String authToken) {
        try{
            var path = "/game";
            this.makeRequest("PUT", path, request, null, authToken);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public void clear() throws DataAccessException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws DataAccessException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.addRequestProperty("Authorization", authToken);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, DataAccessException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new DataAccessException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    // Websocket methods
    public void connect(Integer gameID, String authToken) throws DataAccessException {
        try {
            URI socketURI = new URI(serverUrl.replace("http", "ws") + "/ws");
            this.session = ContainerProvider.getWebSocketContainer().connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    notificationHandler.notify(message);
                }
            });
            var connect = new ConnectCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(connect));
        } catch (IOException | DeploymentException | URISyntaxException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void makeMove(Integer gameID, String authToken, ChessMove move) throws DataAccessException {
        try {
            var make = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(make));
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void leaveGame(Integer gameID, String authToken) throws DataAccessException {
        try {
            var leave = new LeaveCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(leave));
            this.session.close();
            this.session = null;
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void resignGame(Integer gameID, String authToken) throws DataAccessException {
        try {
            var action = new ResignCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
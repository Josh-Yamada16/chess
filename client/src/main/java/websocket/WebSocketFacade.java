package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.DataAccessException;
import requests.JoinGameRequest;
import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;


    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws DataAccessException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String player, JoinGameRequest.PlayerColor team, Integer gameID, String authToken) throws DataAccessException {
        try {
            var connect = new ConnectCommand(UserGameCommand.CommandType.CONNECT,authToken, gameID, player);
            this.session.getBasicRemote().sendText(new Gson().toJson(connect));
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void connectObserver(String visitorName) throws DataAccessException {
        try {
            var action = new Action(Action.Type.EXIT, visitorName);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
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

    public void leaveGame(String player, Integer gameID, String authToken) throws DataAccessException {
        try {
            var leave = new LeaveCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, player);
            this.session.getBasicRemote().sendText(new Gson().toJson(leave));
            this.session.close();
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void resignGame(String player, Integer gameID, String authToken) throws DataAccessException {
        try {
            var action = new ResignCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, player);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void inCheck(String visitorName) throws DataAccessException {
        try {
            var action = new Action(Action.Type.EXIT, visitorName);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    public void inCheckmate(String visitorName) throws DataAccessException {
        try {
            var action = new Action(Action.Type.EXIT, visitorName);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

}

package websocket;

import com.google.gson.Gson;
import dataaccess.implementations.MySqlAuthDAO;
import dataaccess.implementations.MySqlGameDAO;
import exception.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import requests.JoinGameRequest;
import utility.Utility;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private MySqlAuthDAO authDAO = new MySqlAuthDAO();
    private MySqlGameDAO gameDAO = new MySqlGameDAO();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand com = new Gson().fromJson(message, UserGameCommand.class);

        switch (com.getCommandType()) {
            case CONNECT -> connect(new Gson().fromJson(message, ConnectCommand.class), session);
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class));
            case LEAVE -> leave(new Gson().fromJson(message, LeaveCommand.class));
            case RESIGN -> resign(new Gson().fromJson(message, ResignCommand.class));
        }
    }

    private void connect(ConnectCommand com, Session session) throws IOException, DataAccessException {
        try{
            connections.add(authDAO.getAuth(com.getAuthToken()).username(), session, com.getGameID());
            JoinGameRequest.PlayerColor team = gameDAO.getTeamColor(com.getGameID(), authDAO.getAuth(com.getAuthToken()).username());
            String message;
            if (team != null) {
                message = String.format("%s joined the game as %s!", authDAO.getAuth(com.getAuthToken()).username(), team.name());
            }
            else{
                message = String.format("%s joined as an observer!", authDAO.getAuth(com.getAuthToken()).username());
            }
            broadcast(message, authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
            connections.sendLoadGame(authDAO.getAuth(com.getAuthToken()).username(),
                    ServerMessage.ServerMessageType.LOAD_GAME, gameDAO.getGame(com.getGameID()));
        } catch (DataAccessException | IOException ex){
            connections.sendLoadGame(,
                    ServerMessage.ServerMessageType.ERROR, "ERROR");
        }
    }

    private void makeMove(MakeMoveCommand com) throws IOException, DataAccessException {
        try{
            var result = Utility.convertMoveToString(com.getMove());
            String start = result.getFirst();
            String end = result.getSecond();
            var message = String.format("%s moved %s to %s!", authDAO.getAuth(com.getAuthToken()).username(), start, end);
            broadcast(message, authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        } catch (DataAccessException | IOException ex) {
            broadcast(ex.getMessage(), authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        }
    }

    private void leave(LeaveCommand com) throws IOException, DataAccessException {
        try{
            connections.remove(authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
            var message = String.format("%s left the game", authDAO.getAuth(com.getAuthToken()).username());
            broadcast(message, authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        } catch (DataAccessException | IOException ex) {
            broadcast(ex.getMessage(), authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        }
    }

    private void resign(ResignCommand com) throws IOException, DataAccessException {
        try{
            var message = String.format("%s resigns the match", authDAO.getAuth(com.getAuthToken()).username());
            broadcast(message, null, com.getGameID());
        } catch (DataAccessException | IOException ex) {
            broadcast(ex.getMessage(), authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        }
    }

    private void broadcast(String message, String player, Integer gameID) throws IOException {
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(player, notification, gameID);
    }
}

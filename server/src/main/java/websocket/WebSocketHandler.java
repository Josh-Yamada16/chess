
package websocket;

import com.google.gson.Gson;
import dataaccess.interfaces.GameDAO;
import exception.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import utility.Utility;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

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

    private void connect(ConnectCommand com, Session session) throws IOException {
        connections.add(com.getUsername(), session, com.getGameID());
        var message = String.format("%s joined the game as %s!", com.getUsername(), com.getColor());
        broadcast(message, com.getUsername(), com.getGameID());
        connections.sendLoadGame(com.getUsername(), );
    }

    private void makeMove(MakeMoveCommand com) throws IOException {
        var result = Utility.convertMoveToString(com.getMove());
        String start = result.getFirst();
        String end = result.getSecond();
        var message = String.format("%s moved %s to %s!", com.getUsername(), start, end);
        broadcast(message, com.getUsername(), com.getGameID());
    }

    private void leave(LeaveCommand com) throws IOException {
        connections.remove(com.getUsername());
        var message = String.format("%s left the game", com.getUsername());
        broadcast(message, com.getUsername(), com.getGameID());
    }

    private void resign(ResignCommand com) throws IOException {
        var message = String.format("%s resigns the match", com.getUsername());
        broadcast(message, null, com.getGameID());
    }

    private void broadcast(String message, String player, Integer gameID) throws IOException {
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(player, notification, gameID);
    }
}

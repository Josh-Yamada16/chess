
package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
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
            case CONNECT -> connect(((ConnectCommand) com).getUsername(), session, ((ConnectCommand) com).getGameID());
            case MAKE_MOVE -> makeMove(((MakeMoveCommand) com).getMove());
            case LEAVE -> leave(((LeaveCommand) com).getUsername());
            case RESIGN -> resign(((ResignCommand) com).getUsername());
        }
    }

    private void connect(String player, Session session, Integer gameID) throws IOException {
        connections.add(player, session, gameID);
        var message = String.format("%s joined the game!", player);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(player, notification, gameID);
    }

    private void makeMove(String player, Session session, String start, String end, Integer gameID) throws IOException {
        var message = String.format("%s moved %s to %s!", player, start, end);
    }

    private void leave(String player) throws IOException {
        connections.remove(player);
        var message = String.format("%s left the game", player);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(player, notification);
    }

    public void resign(String player) throws DataAccessException {
        try {
            var message = String.format("%s resigns the match", player);
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }
}

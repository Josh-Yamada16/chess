package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, List<Connection>> map = new ConcurrentHashMap<>();

    public void add(String player, Session session, Integer gameID) {
        map.computeIfAbsent(gameID, k -> Collections.synchronizedList(new ArrayList<>())).add(new Connection(player, session));
    }

    public List<Connection> getValues(Integer gameID) {
        return map.getOrDefault(gameID, Collections.emptyList());
    }

    public void remove(String player, Integer gameID) {
        List<Connection> connections = getValues(gameID);
        if (connections != null) {
            synchronized (connections) {
                connections.removeIf(connection -> connection.playerName.equals(player));
                if (connections.isEmpty()) {
                    connections.remove(gameID);
                }
            }
        }
    }

    public Session getSessionByPlayer(String playerName) {
        for (List<Connection> connections : map.values()) {
            synchronized (connections) {
                for (Connection connection : connections) {
                    if (connection.playerName.equals(playerName)) {
                        return connection.session;
                    }
                }
            }
        }
        return null;
    }

    public void broadcast(String excludePlayer, ServerMessage notification, Integer gameID) throws IOException {
        List<Connection> connections = this.getValues(gameID);
        for (Connection connection : connections) {
            if (connection.session != null && connection.session.isOpen()
                    && !connection.playerName.equals(excludePlayer)) {
                connection.send(new Gson().toJson(notification));
            }
        }
    }

    public <T> void loadGameToAllPlayers(Integer gameID, T mess, ServerMessage.ServerMessageType type) throws IOException {
        try{
            for (Connection connection : getValues(gameID)){
                Session sesh = getSessionByPlayer(connection.playerName);
                sendLoadGame(type, mess, sesh);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void sendLoadGame(ServerMessage.ServerMessageType type, T mess, Session session) throws IOException {
        if (session == null){
            return;
        }
        if (!session.isOpen()){
            return;
        }
        var messageJson = switch (type) {
            case LOAD_GAME -> new Gson().toJson(new LoadGameMessage(type, mess));
            case ERROR -> new Gson().toJson(new ErrorMessage(type, (String) mess));
            default -> throw new IllegalArgumentException("Unsupported message type: " + type);
        };
        session.getRemote().sendString(messageJson);
    }
}

package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String playerName;
    public Session session;
    public Integer gameID;

    public Connection(String playerName, Session session, Integer gameID) {
        this.playerName = playerName;
        this.session = session;
        this.gameID = gameID;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }

    public Integer getGameID() {
        return gameID;
    }
}

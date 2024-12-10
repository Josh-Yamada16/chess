package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String playerName;
    public Session session;

    public Connection(String playerName, Session session) {
        this.playerName = playerName;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }


    public Session getSession() {
        return session;
    }
}

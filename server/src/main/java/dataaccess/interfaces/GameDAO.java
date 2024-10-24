package dataaccess.interfaces;

import model.GameData;
import server.requests.JoinGameRequest;

import java.util.Collection;

public interface GameDAO {
    void clear();

    int createGame(String gameName);

    GameData getGame(int gameID);

    boolean addPlayer(int gameID, JoinGameRequest.playerColor teamColor, String userName);

    Collection<GameData> listGames();
}

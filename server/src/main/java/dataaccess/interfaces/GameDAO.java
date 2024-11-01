package dataaccess.interfaces;

import exception.DataAccessException;
import model.GameData;
import server.requests.JoinGameRequest;

import java.util.Collection;

public interface GameDAO {
    void clear() throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    boolean addPlayer(int gameID, JoinGameRequest.playerColor teamColor, String userName) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;
}

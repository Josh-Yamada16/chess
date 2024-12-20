package dataaccess.interfaces;

import chess.ChessGame;
import exception.DataAccessException;
import model.GameData;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.Collection;

public interface GameDAO {
    void clear() throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    boolean addPlayer(int gameID, JoinGameRequest.PlayerColor teamColor, String userName) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;

    ArrayList<Integer> onlyGames() throws DataAccessException;

    ChessGame updateGame(int gameID, ChessGame game) throws DataAccessException;

    boolean removePlayer(int gameID, String userName) throws DataAccessException;
}

package dataaccess.implementations;

import chess.ChessGame;
import dataaccess.interfaces.GameDAO;
import model.GameData;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    final private HashMap<Integer, GameData> games = new HashMap<>();
    private int gameID = 1;


    @Override
    public void clear() {
        games.clear();
    }

    @Override
    public int createGame(String gameName) {
        GameData newGame;
        newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, newGame);
        gameID++;
        return newGame.gameID();
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public boolean addPlayer(int gameID, JoinGameRequest.playerColor teamColor, String userName) {
        GameData game = games.get(gameID);
        if (teamColor == JoinGameRequest.playerColor.WHITE){
            if (game.whiteUsername() != null){
                return false;
            }
            GameData newGame = new GameData(game.gameID(), userName, game.blackUsername(), game.gameName(), game.game());
            games.remove(gameID);
            games.put(newGame.gameID(), newGame);
        }
        else{
            if (game.blackUsername() != null){
                return false;
            }
            GameData newGame = new GameData(game.gameID(), game.whiteUsername(), userName, game.gameName(), game.game());
            games.remove(gameID);
            games.put(newGame.gameID(), newGame);
        }
        return true;
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    public ArrayList<Integer> onlyGames() {
        return new ArrayList<>(games.keySet());
    }
}

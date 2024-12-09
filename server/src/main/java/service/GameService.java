package service;

import chess.ChessGame;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import exception.DataAccessException;
import model.*;
import requests.JoinGameRequest;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {

    private final GameDAO gameDao;
    private final AuthDAO authDao;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDao = gameDAO;
        this.authDao = authDAO;
    }

    public int createGame(String gameName, String authToken) throws DataAccessException {
        if (!authDao.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        if (gameName == null) {
            throw new DataAccessException(400, "Error: bad request");
        }
        return gameDao.createGame(gameName);
    }

    public void joinGame(JoinGameRequest game, String authToken) throws DataAccessException {
        if (!authDao.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        if (game.getGameID() == 0 || game.getPlayerColor() == null){
            throw new DataAccessException(400, "Error: bad request");
        }
        if (game.getPlayerColor() == JoinGameRequest.PlayerColor.WHITE){
            if (gameDao.getGame(game.getGameID()).whiteUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
        }
        else{
            if (gameDao.getGame(game.getGameID()).blackUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
        }
        String userName = authDao.getAuth(authToken).username();
        gameDao.addPlayer(game.getGameID(), game.getPlayerColor(), userName);
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        if (!authDao.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        return gameDao.listGames();
    }

    public boolean updateGame(int gameID, ChessGame game) throws DataAccessException{
        return gameDao.updateGame(gameID, game);
    }

    public void clear() throws DataAccessException {
        gameDao.clear();
        authDao.clear();
    }

    public ArrayList<Integer> onlyGames() throws DataAccessException{
        return gameDao.onlyGames();
    }

    public GameData getGame(int gameID) throws DataAccessException {
        if (gameDao.getGame(gameID) == null){
            throw new DataAccessException(404, "Error: Game not found");
        }
        return gameDao.getGame(gameID);
    }

}
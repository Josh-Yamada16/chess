package service;

import dataaccess.implementations.MemoryAuthDAO;
import dataaccess.implementations.MemoryGameDAO;
import exception.DataAccessException;
import model.*;
import server.requests.JoinGameRequest;

import java.util.Collection;

public class GameService {

    private final MemoryGameDAO gameDao;
    private final MemoryAuthDAO authDao;

    public GameService(MemoryGameDAO gameDAO, MemoryAuthDAO authDAO) {
        this.gameDao = gameDAO;
        this.authDao = authDAO;
    }

    public int createGame(String gameName) throws DataAccessException {
        return gameDao.createGame(gameName);
    }

    public void joinGame(JoinGameRequest game) throws DataAccessException {
        gameDao.addPlayer(game.getGameID(), game.getPlayerColor(), null);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return gameDao.listGames();
    }

    public void clear() throws DataAccessException {
        gameDao.clear();
        authDao.clear();
    }


}
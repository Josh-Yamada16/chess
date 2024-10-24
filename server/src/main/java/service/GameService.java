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
        if (game.getPlayerColor() == JoinGameRequest.playerColor.WHITE){
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

    public void clear() throws DataAccessException {
        gameDao.clear();
        authDao.clear();
    }


}
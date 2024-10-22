package service;

import dataaccess.DataAccess;
import chess.ChessGame;
import exception.ResponseException;
import model.*;

import java.util.Collection;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // Pet Shop is very simple.
    // A more complicated application would do the business logic in this
    // service.

    public UserData addUser(UserData userData) throws ResponseException {
        if (pet.type() == PetType.DOG && pet.name().equals("fleas")) {
            throw new ResponseException(200, "no dogs with fleas");
        }
        return dataAccess.addUser(userData);
    }

    public Collection<GameData> listGames() throws ResponseException {
        return dataAccess.listGames();
    }

    public UserData getUser(int id) throws ResponseException {
        return dataAccess.getUser(id);
    }

    public void deleteUser(Integer id) throws ResponseException {
        dataAccess.deleteUser(id);
    }

    public void deleteAllData() throws ResponseException {
        dataAccess.deleteAllData();
    }
}
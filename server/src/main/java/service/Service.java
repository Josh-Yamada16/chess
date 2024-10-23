package service;

import dataaccess.DataAccess;
import chess.ChessGame;
import exception.ResponseException;
import model.*;

import java.util.Collection;
import java.util.UUID;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // Pet Shop is very simple.
    // A more complicated application would do the business logic in this

    // service.
    public AuthData loginUser(UserData user) throws ResponseException {
        if (dataAccess.matchUsername(user.getUsername()) == 0){
            throw new ResponseException(500, "Error: User not found");
        }
        if (dataAccess.matchPassword(user) == 0){
            throw new ResponseException(500, "Error: unauthorized");
        }
        return dataAccess.createAuth(user);
    }

    public AuthData addUser(UserData userData) throws ResponseException {
        if (pet.type() == PetType.DOG && pet.name().equals("fleas")) {
            throw new ResponseException(200, "no dogs with fleas");
        }
        return dataAccess.addUser(userData);
    }

    public Collection<GameData> listGames() throws ResponseException {
        return dataAccess.listGames();
    }

    public void deleteAuth(String UUID) throws ResponseException {
        dataAccess.deleteAuth(UUID);
    }

    public void deleteAllData() throws ResponseException {
        dataAccess.deleteAllData();
    }


}
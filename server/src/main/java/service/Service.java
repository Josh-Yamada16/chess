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
        if (!dataAccess.matchUsername(user.username())){
            throw new ResponseException(401, "Error: User not found");
        }
        if (!dataAccess.matchPassword(user)){
            throw new ResponseException(500, "Error: unauthorized");
        }
        return dataAccess.createAuth(user);
    }

    public AuthData registerUser(UserData userData) throws ResponseException {
        if (dataAccess.matchUsername(userData.username())) {
            throw new ResponseException(402, "Error: already taken");
        }
        // if no database setup -> 400 error bad request
        dataAccess.addUser(userData);
        return dataAccess.createAuth(userData);
    }

    public UserData getUser(String username) throws ResponseException {}

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
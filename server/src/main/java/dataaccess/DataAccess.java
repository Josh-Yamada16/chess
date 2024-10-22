package dataaccess;

import exception.ResponseException;
import model.*;

import java.util.Collection;
import java.util.UUID;

public interface DataAccess {
    AuthData addUser(UserData user) throws ResponseException;

    Collection<GameData> listGames() throws ResponseException;

    AuthData getUser(UserData user) throws ResponseException;

    void deleteAuth(String UUID) throws ResponseException;

    void deleteAllData() throws ResponseException;

}
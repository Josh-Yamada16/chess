package dataaccess;

import exception.ResponseException;
import model.*;

import java.util.Collection;

public interface DataAccess {
    int matchUsername(String username) throws ResponseException;

    int matchPassword(UserData user) throws ResponseException;

    AuthData createAuth(UserData user) throws ResponseException;

    Collection<GameData> listGames() throws ResponseException;

    void deleteAuth(String UUID) throws ResponseException;

    void deleteAllData() throws ResponseException;

}
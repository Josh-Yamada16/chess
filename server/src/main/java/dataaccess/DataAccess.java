package dataaccess;

import exception.ResponseException;
import model.*;

import java.util.Collection;

public interface DataAccess {
    boolean matchUsername(String username) throws ResponseException;

    boolean matchPassword(UserData user) throws ResponseException;

    void addUser(UserData user) throws ResponseException;

    AuthData createAuth(UserData user) throws ResponseException;

    Collection<GameData> listGames() throws ResponseException;

    void deleteAuth(String UUID) throws ResponseException;

    void deleteAllData() throws ResponseException;

}
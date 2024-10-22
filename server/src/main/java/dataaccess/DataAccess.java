package dataaccess;

import exception.ResponseException;
import model.*;

import java.util.Collection;
import java.util.UUID;

public interface DataAccess {
    UserData addUser(UserData user) throws ResponseException;

    Collection<GameData> listGames() throws ResponseException;

    UserData getUser(int id) throws ResponseException;

    void deleteUser(Integer id) throws ResponseException;

    void deleteAllData() throws ResponseException;

    String generateToken();
}
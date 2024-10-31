package dataaccess.implementations;

import dataaccess.interfaces.AuthDAO;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    final private HashMap<String, UserData> auths = new HashMap<>();
    @Override
    public void clear() throws DataAccessException {
        auths.clear();
    }

    @Override
    public AuthData createAuth(UserData user) {
        String token = generateToken();
        auths.put(token, user);
        return new AuthData(token, user.username());
    }

    @Override
    public boolean verifyAuth(String authToken) {
        return auths.containsKey(authToken);
    }

    public UserData getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public boolean deleteAuth(String UUID) {
        return auths.remove(UUID) != null;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public HashMap<String, UserData> getAuthList(){
        return auths;
    }
}

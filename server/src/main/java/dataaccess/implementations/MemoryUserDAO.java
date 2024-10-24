package dataaccess.implementations;

import dataaccess.interfaces.UserDAO;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<String, UserData> users = new HashMap<>();

    public MemoryUserDAO() {
    }

    @Override
    public void clear() {
        users.clear();
    }

    public UserData getUser(String username) {
        if (users.get(username) != null){
            return users.get(username);
        }
        return null;
    }

    public boolean matchPassword(UserData user) {
        return users.get(user.username()).password().equals(user.password());
    }

    public void addUser(UserData user) {
        users.put(user.username(), user);
    }
}

package dataaccess.interfaces;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    void clear();

    AuthData createAuth(UserData user);

    boolean verifyAuth(String authToken);

    boolean deleteAuth(String UUID);

    String generateToken();
}

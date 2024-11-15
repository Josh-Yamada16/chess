package clients;

import com.google.gson.Gson;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;
import server.ServerFacade;

import java.util.Arrays;

public class UiClient {
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.PRESIGNIN;

    public UiClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames(params);
                case "play" -> playGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (state == State.PRESIGNIN) {
            return """
                    - 'login' <username> <password>
                    - 'register' <username> <password> <email>
                    - quit''
                    """;
        }
        else if (state == State.POSTSIGNIN) {
            return """
                    - 'logout'
                    - 'create' game
                    - 'list' games
                    - play'' game
                    - 'observe' game
                    - 'quit'
                    """;
        }
        else {
            return """
                    """;
        }
    }

    public String login(String... params) throws DataAccessException {
        if (params.length == 2) {
            AuthData result = server.login(params[0], params[1]);
            if (result != null) {
                this.username = result.username();
                this.authToken = result.authToken();
                state = State.POSTSIGNIN;
                return String.format("You signed in as %s.", this.username);
            }
            throw new DataAccessException(400, "Invalid Login");
        }
        throw new DataAccessException(400, "Expected: <username> <password>");
    }

    public String register(String... params) throws DataAccessException {
        if (params.length == 3) {
            UserData user = new UserData(params[0], params[1], params[2]);
            AuthData result = server.registerUser(user);
            if (result != null) {
                this.username = result.username();
                this.authToken = result.authToken();
                state = State.POSTSIGNIN;
                return String.format("You signed in as %s.", this.username);
            }
        }
        throw new DataAccessException(400, "Expected: <username> <password> <email>");
    }

    public String logout() throws DataAccessException {
        assertLoggedIn();
        server.logout(this.authToken);
        state = State.PRESIGNIN;
        return String.format("See you next time %s!", username);
    }

    public String rescuePet(String... params) throws DataAccessException {
        assertLoggedIn();
        if (params.length >= 2) {
            var name = params[0];
            var type = PetType.valueOf(params[1].toUpperCase());
            var pet = new Pet(0, name, type);
            pet = server.addPet(pet);
            return String.format("You rescued %s. Assigned ID: %d", pet.name(), pet.id());
        }
        throw new DataAccessException(400, "Expected: <name> <CAT|DOG|FROG>");
    }

    public String listGames() throws DataAccessException {
        assertSignedIn();
        var pets = server.listPets();
        var result = new StringBuilder();
        var gson = new Gson();
        for (var pet : pets) {
            result.append(gson.toJson(pet)).append('\n');
        }
        return result.toString();
    }

    public String adoptPet(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 1) {
            try {
                var id = Integer.parseInt(params[0]);
                var pet = getPet(id);
                if (pet != null) {
                    server.deletePet(id);
                    return String.format("%s says %s", pet.name(), pet.sound());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new DataAccessException(400, "Expected: <pet id>");
    }

    public String adoptAllPets() throws DataAccessException {
        assertSignedIn();
        var buffer = new StringBuilder();
        for (var pet : server.listPets()) {
            buffer.append(String.format("%s says %s%n", pet.name(), pet.sound()));
        }

        server.deleteAllPets();
        return buffer.toString();
    }

    private Pet getPet(int id) throws DataAccessException {
        for (var pet : server.listPets()) {
            if (pet.id() == id) {
                return pet;
            }
        }
        return null;
    }

    private void assertLoggedIn() throws DataAccessException {
        if (this.state == State.PRESIGNIN) {
            throw new DataAccessException(400, "You must sign in");
        }
    }
}
package clients;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.JoinGameRequest;
import server.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

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
                case "logout" -> logout(params);
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
                    - 'create' game <gameName>
                    - 'list' games
                    - 'play' game <game#> <playerColor>
                    - 'observe' game <gameId>
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

    public String logout(String... params) throws DataAccessException {
        if (params.length == 0) {
            assertLoggedIn();
            if (server.logout(this.authToken)){
                state = State.PRESIGNIN;
                return String.format("See you next time %s!", username);
            }
            else{
                return "Unauthorized";
            }
        }
        throw new DataAccessException(400, "Expected: *JUST LOGOUT*");
    }

    public String createGame(String... params) throws DataAccessException {
        if (params.length == 1) {
            assertLoggedIn();
            if (server.createGame(params[0], this.authToken)){
                return "Your game has been created!";
            }
            else{
                return "Unauthorized";
            }
        }
        throw new DataAccessException(400, "Expected: <gameName>");

    }

    public String listGames(String... params) throws DataAccessException {
        if (params.length == 0) {
            assertLoggedIn();
            var games = server.listGames(this.authToken);
            var result = new StringBuilder();
            int counter = 1;
            for (var game : games) {
                result.append(counter).append(". ");
                result.append(game.gameID()).append(" ");
                result.append(game.gameName());
                result.append('\n');
            }
            return result.toString();
        }
        throw new DataAccessException(400, "Expected: *JUST LIST*");
    }

    public String playGame(String... params) throws DataAccessException {
        var games = server.listGames(this.authToken);
        if (params.length == 2) {
            JoinGameRequest.PlayerColor color = null;
            if (params[1].equalsIgnoreCase("white")){
                color = JoinGameRequest.PlayerColor.WHITE;
            }
            else if (params[1].equalsIgnoreCase("black")){
                color = JoinGameRequest.PlayerColor.BLACK;
            }
            else{
                throw new DataAccessException(400, "Expected: white OR black");
            }
            int num = Integer.parseInt(params[0]);
            if (num > 0 & num <= games.size()){
                JoinGameRequest req = new JoinGameRequest(color, games.get(num).gameID());
                if (server.joinGame(req)){
                    state = State.INGAME;
                    System.out.print(SET_TEXT_COLOR_BLUE + String.format("Game %d Successfully joined!", num));
                    printBoardWhite(games.get(num).game());
                    printBoardBlack(games.get(num).game());
                }
            }
            else{
                throw new DataAccessException(400, "Game not available");
            }
        }
        throw new DataAccessException(400, "Expected: <game#> <playerColor>");
    }

    private void printBoardWhite(ChessGame game) {
        return;
    }

    private void printBoardBlack(ChessGame game) {
        return;
    }

    private void assertLoggedIn() throws DataAccessException {
        if (this.state == State.PRESIGNIN) {
            throw new DataAccessException(400, "You must sign in");
        }
    }
}
package ui;

import exception.DataAccessException;
import model.AuthData;
import model.UserData;
import requests.JoinGameRequest;
import server.ServerFacade;
import websocket.*;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class UiClient {
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    public State state = State.PRESIGNIN;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;

    public UiClient(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
        this.notificationHandler = notificationHandler;
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
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                case "clear" -> clear();
                case "help" -> "";
                default -> SET_TEXT_COLOR_RED + "**Command Not Recognized**\n";
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String clear() throws DataAccessException {
        server.clear();
        return SET_TEXT_COLOR_RED + "**Database Cleared**\n";
    }

    public String help() {
        if (state == State.PRESIGNIN) {
            return """
                    - 'login' <username> <password>
                    - 'register' <username> <password> <email>
                    - 'help'
                    - 'quit'
                    """;
        }
        else if (state == State.POSTSIGNIN) {
            return """
                    - 'logout'
                    - 'create' game <gameName>
                    - 'list' games
                    - 'join' game <game#> <playerColor>
                    - 'observe' game <game#>
                    - 'help'
                    """;
        }
        else {
            return """
                    """;
        }
    }

    public String login(String... params) throws DataAccessException {
        try {
            if (params.length == 2) {
                AuthData result = server.login(params[0], params[1]);
                if (result != null) {
                    this.username = result.username();
                    this.authToken = result.authToken();
                    state = State.POSTSIGNIN;
                    return String.format("**Welcome Back %s!**\n", this.username);
                }
            }
            return SET_TEXT_COLOR_RED + "**Expected: <username> <password>**\n";
        } catch (DataAccessException ex) {
            if (ex.statusCode() == 500){
                return SET_TEXT_COLOR_RED + "**Invalid Login**\n";
            }
        }
        return "";
    }

    public String register(String... params) throws DataAccessException {
        try{
            if (params.length == 3) {
                UserData user = new UserData(params[0], params[1], params[2]);
                AuthData result = server.registerUser(user);
                if (result != null) {
                    this.username = result.username();
                    this.authToken = result.authToken();
                    state = State.POSTSIGNIN;
                    return String.format("**Welcome %s!**\n", this.username);
                }
            }
            return SET_TEXT_COLOR_RED + "**Expected: <username> <password> <email>**\n";
        } catch (DataAccessException ex) {
            if (ex.statusCode() == 500){
                return SET_TEXT_COLOR_RED + "**Login already in use!**\n";
            }
        }
        return "";
    }

    public String logout(String... params) throws DataAccessException {
        if (params.length == 0) {
            assertLoggedIn();
            if (server.logout(this.authToken)){
                state = State.PRESIGNIN;
                return String.format("**See you next time %s!**\n", username);
            }
            else{
                return SET_TEXT_COLOR_RED + "**Unauthorized**\n";
            }
        }
        return SET_TEXT_COLOR_RED + "Expected: *JUST LOGOUT*\n";
    }

    public String createGame(String... params) throws DataAccessException {
        if (params.length == 1) {
            assertLoggedIn();
            if (server.createGame(params[0], this.authToken)){
                return "**Your game has been created!**\n";
            }
            else{
                return SET_TEXT_COLOR_RED + "**Unauthorized**\n";
            }
        }
        return SET_TEXT_COLOR_RED + "**Expected: <gameName>**\n";

    }

    public String listGames(String... params) throws DataAccessException {
        if (params.length == 0) {
            assertLoggedIn();
            var games = server.listGames(this.authToken);
            if (games.isEmpty()){
                return "**No Games Yet**\n";
            }
            var result = new StringBuilder();
            int counter = 1;
            for (var game : games) {
                result.append(counter).append(". ");
                result.append("Game Name: ").append(game.gameName()).append(",\t");
                result.append("White: ").append(game.whiteUsername() == null ? "*Available*" : game.whiteUsername()).append(",\t");
                result.append("Black: ").append(game.blackUsername() == null ? "*Available*" : game.blackUsername());
                result.append('\n');
                counter++;
            }
            return result.append("\n").toString();
        }
        return SET_TEXT_COLOR_RED + "Expected: *JUST LIST*\n";
    }

    public String joinGame(String... params) throws DataAccessException {
        var games = server.listGames(this.authToken);
        if (params.length == 2) {
            assertLoggedIn();
            JoinGameRequest.PlayerColor color;
            if (params[1].equalsIgnoreCase("white")){
                color = JoinGameRequest.PlayerColor.WHITE;
            }
            else if (params[1].equalsIgnoreCase("black")){
                color = JoinGameRequest.PlayerColor.BLACK;
            }
            else{
                return SET_TEXT_COLOR_RED + "**Expected: <white> OR <black>**\n";
            }
            int num;
            try{
                num = Integer.parseInt(params[0]) - 1;
            } catch (NumberFormatException ex){
                return SET_TEXT_COLOR_RED + "**Expected: Game Number**\n";
            }
            if (num > -1 & num <= games.size()-1){
                JoinGameRequest req = new JoinGameRequest(color, games.get(num).gameID());
                if (server.joinGame(req, this.authToken)){
//                    state = State.INGAME;
                    System.out.print(SET_TEXT_COLOR_BLUE + String.format("**Game %d Successfully joined!**\n", num+1));
                    BoardPrinter.printWhitePov(games.get(num).game().getBoard());
                    System.out.println();
                    BoardPrinter.printBlackPov(games.get(num).game().getBoard());
                }
                else{
                    return SET_TEXT_COLOR_RED + "**Team Already Taken**\n";
                }
            }
            else{
                return SET_TEXT_COLOR_RED + "**Game not available**\n";
            }
        }
        else{
            return SET_TEXT_COLOR_RED + "**Expected: <game#> <playerColor>**\n";
        }
        return "\n";
    }

    public String observeGame(String... params) throws DataAccessException{
        var games = server.listGames(this.authToken);
        int num;
        if (params.length == 1) {
            try {
                num = Integer.parseInt(params[0]) - 1;
            } catch (NumberFormatException ex) {
                return SET_TEXT_COLOR_RED + "**Expected: Game Number**\n";
            }
            if (num > -1 & num <= games.size() - 1) {
                System.out.printf("**Currently Observing game %d**\n", num + 1);
                BoardPrinter.printWhitePov(games.get(num).game().getBoard());
                System.out.println();
                BoardPrinter.printBlackPov(games.get(num).game().getBoard());
            } else {
                return SET_TEXT_COLOR_RED + "**Game not available**\n";
            }
            return "\n";
        }
        else{
            return SET_TEXT_COLOR_RED + "Expected: *JUST GAME#*\n";
        }
    }

    private void assertLoggedIn() throws DataAccessException {
        if (this.state == State.PRESIGNIN) {
            throw new DataAccessException(400, SET_TEXT_COLOR_RED + "**You must sign in**");
        }
    }

    private void assertInGame() throws DataAccessException {
        if (this.state == State.INGAME) {
            throw new DataAccessException(400, SET_TEXT_COLOR_RED + "**You be in game**");
        }
    }
}
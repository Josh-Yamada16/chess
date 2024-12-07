package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;
import requests.JoinGameRequest;
import server.ServerFacade;
import websocket.*;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static ui.EscapeSequences.*;

public class UiClient {
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    public State state = State.PRESIGNIN;
    private final String serverUrl;
    private final WebSocketHandler notificationHandler;
    private Session session;
    private ChessGame activeGame;
    private JoinGameRequest.PlayerColor playerColor;

    public UiClient(String serverUrl, WebSocketHandler notificationHandler) throws URISyntaxException, DeploymentException, IOException {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        URI socketURI = new URI(serverUrl.replace("http", "ws") + "/ws");
        this.session = ContainerProvider.getWebSocketContainer().connectToServer(this, socketURI);
        server = new ServerFacade(serverUrl, session);
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
                case "redraw" -> redraw(params);
                case "leave" -> leave(params);
                case "make" -> make(params);
                case "resign" -> resign(params);
                case "highlight" -> highlight(params);
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
                    - 'redraw' chess board
                    - 'leave'
                    - 'move' <piece position> <end position>
                    - 'resign'
                    - 'highlight' legal moves <piece position>
                    - 'help'
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
                    state = State.INGAME;
                    System.out.print(SET_TEXT_COLOR_BLUE + String.format("**Game %d Successfully joined!**\n", num+1));
                    activeGame = games.get(num).game();
                    playerColor = color;
                    server.connect(username, color, games.get(num).gameID(), authToken);
                    BoardPrinter.printBasedOnPov(color, games.get(num).game().getBoard());
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
                server.connectObserver(username, games.get(num).gameID(), authToken);
                BoardPrinter.printWhitePov(games.get(num).game().getBoard());
            } else {
                return SET_TEXT_COLOR_RED + "**Game not available**\n";
            }
            return "\n";
        }
        else{
            return SET_TEXT_COLOR_RED + "Expected: *JUST GAME#*\n";
        }
    }


    public String redraw(String... params) throws DataAccessException {
        try {
            assertInGame();
            if (params.length == 0) {
                BoardPrinter.printBasedOnPov(playerColor, activeGame.getBoard());
            }
            return SET_TEXT_COLOR_RED + "Expected: *JUST REDRAW*\n";
        } catch (DataAccessException ex) {
            if (ex.statusCode() == 500){
                return SET_TEXT_COLOR_RED + "Expected: *JUST REDRAW*\n";
            }
        }
        return "";
    }

    public String leave(String... params) throws DataAccessException {
        try{
            assertInGame();
            if (params.length == 0) {
                server.leaveGame(this.username, 0, this.authToken);
                // remember to set activeGame to null and playercolor
            }
            return SET_TEXT_COLOR_RED + "Expected: *JUST LEAVE*\n";
        } catch (DataAccessException ex) {
            if (ex.statusCode() == 500){
                return SET_TEXT_COLOR_RED + "**Login already in use!**\n";
            }
        }
        return "";
    }

    public String make(String... params) throws DataAccessException {
        try {
            if (params.length == 2) {
                // need to parse the numbers and letters to convert them to coordinates
                assertInGame();
                var result = BoardPrinter.validateAndParseCoordinates(params[0]);
                var result1 = BoardPrinter.validateAndParseCoordinates(params[1]);
                ChessPosition start = new ChessPosition(result.getFirst(), result.getSecond());
                ChessPosition end = new ChessPosition(result1.getFirst(), result1.getSecond());
                // make a route where the pawn is going to be promoted
                ChessMove thingy = new ChessMove(start, end, null);
                activeGame.makeMove(thingy);
            }
            return SET_TEXT_COLOR_RED + "**Expected: <start position(A-G/1-8)> <end position(A-G/1-8)>**\n";
        } catch (DataAccessException|InvalidMoveException e) {
            return SET_TEXT_COLOR_RED + e.getMessage();
        }
    }

    public String resign(String... params) throws DataAccessException {
        try{
            if (params.length == 0) {
                assertInGame();

            }
            return SET_TEXT_COLOR_RED + "Expected: *JUST RESIGN*\n";
        } catch (DataAccessException ex){
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }

    }

    public String highlight(String... params) throws DataAccessException {
        if (params.length == 0) {
            assertInGame();
            // print the board again based on the POV and the available piece moves
//                BoardPrinter.printWhitePov(games.get(num).game().getBoard());
        }
        else{
            return SET_TEXT_COLOR_RED + "** Expected: <piece position>**\n";
        }
        return "";
    }

    private void assertLoggedIn() throws DataAccessException {
        if (this.state == State.PRESIGNIN) {
            throw new DataAccessException(400, SET_TEXT_COLOR_RED + "**You must sign in**");
        }
    }

    private void assertInGame() throws DataAccessException {
        if (this.state == State.INGAME) {
            throw new DataAccessException(400, SET_TEXT_COLOR_RED + "**You must be in game**");
        }
    }
}
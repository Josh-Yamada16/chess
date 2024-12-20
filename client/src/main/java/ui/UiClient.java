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
import utility.Utility;
import websocket.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class UiClient {
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    public State state = State.PRESIGNIN;
    private ChessGame activeGame;
    private Integer activeGameId;
    private JoinGameRequest.PlayerColor playerColor;

    public UiClient(String serverUrl, NotificationHandler notificationHandler) throws DeploymentException, URISyntaxException, IOException {
        server = new ServerFacade(serverUrl, notificationHandler);
    }

    public JoinGameRequest.PlayerColor getPlayerColor() {
        return playerColor;
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
                case "move" -> move(params);
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
        else if (state == State.INGAME) {
            return """
                    - 'redraw' chess board
                    - 'move' <piece position(A-G/1-8)> <end position(A-G/1-8)>
                    - 'highlight' legal moves <piece position(A-G/1-8)>
                    - 'help'
                    - 'leave'
                    - 'resign'
                    """;
        }

        else{
            return """
                    - 'redraw' chess board
                    - 'highlight' legal moves <piece position(A-G/1-8)>
                    - 'help'
                    - 'leave'
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
            if (!assertLoggedIn()){
                return SET_TEXT_COLOR_RED + "**You must sign in**\n";
            }

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
            if (!assertLoggedIn() ){
                return SET_TEXT_COLOR_RED + "**You must sign in**\n";
            }
            else if (assertInGame() || assertObserving()){
                return SET_TEXT_COLOR_RED + "**You must be in the main menu**\n";
            }
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
            if (!assertLoggedIn()){
                return SET_TEXT_COLOR_RED + "**You must sign in**\n";
            }
            else if (assertInGame() || assertObserving()){
                return SET_TEXT_COLOR_RED + "**You must be in the main menu**\n";
            }
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
        if (params.length != 2) {
            return SET_TEXT_COLOR_RED + "**Expected: <game#> <playerColor>**\n";
        }
        if (!assertLoggedIn()){
            return SET_TEXT_COLOR_RED + "**You must sign in**\n";
        }
        else if (assertInGame() || assertObserving()){
            return SET_TEXT_COLOR_RED + "**You must be in the main menu**\n";
        }
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
        if (num <= -1 || num > games.size()) {
            return SET_TEXT_COLOR_RED + "**Game not available**\n";
        }
        JoinGameRequest req = new JoinGameRequest(color, games.get(num).gameID());
        if (!server.joinGame(req, this.authToken)) {
            return SET_TEXT_COLOR_RED + "**Team Already Taken**\n";
        }
        state = State.INGAME;
        System.out.print(SET_TEXT_COLOR_BLUE + String.format("**Game %d Successfully joined!**\n", num+1));
        activeGame = games.get(num).game();
        activeGameId = games.get(num).gameID();
        playerColor = color;
        server.connect(games.get(num).gameID(), authToken);
        BoardPrinter.printBasedOnPov(color, games.get(num).game().getBoard(), new ArrayList<ChessPosition>());
        return "\n";
    }

    public String observeGame(String... params) throws DataAccessException{
        var games = server.listGames(this.authToken);
        int num;
        if (params.length == 1) {
            if (!assertLoggedIn()){
                return SET_TEXT_COLOR_RED + "**You must sign in**\n";
            }
            try {
                num = Integer.parseInt(params[0]) - 1;
            } catch (NumberFormatException ex) {
                return SET_TEXT_COLOR_RED + "**Expected: Game Number**\n";
            }
            if (num > -1 & num <= games.size() - 1) {
                System.out.printf("**Currently Observing game %d**\n", num + 1);
                server.connect(games.get(num).gameID(), authToken);
                state = State.OBSERVE;
                activeGame = games.get(num).game();
                activeGameId = games.get(num).gameID();
                playerColor = JoinGameRequest.PlayerColor.WHITE;
                BoardPrinter.printBasedOnPov(playerColor, games.get(num).game().getBoard(), new ArrayList<ChessPosition>());
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
            if (assertLoggedIn()){
                return SET_TEXT_COLOR_RED + "**You must be in game**\n";
            }
            if (params.length == 0) {
                BoardPrinter.printBasedOnPov(playerColor, activeGame.getBoard(), new ArrayList<ChessPosition>());
                return "\n";
            }
            return SET_TEXT_COLOR_RED + "Expected: *JUST REDRAW*\n";
        } catch (DataAccessException ex) {
            if (ex.statusCode() == 500){
                return SET_TEXT_COLOR_RED + "Expected: *JUST REDRAW*\n";
            }
        }
        return "\n";
    }

    public String leave(String... params) throws DataAccessException {
        try{
            if (assertLoggedIn()){
                return SET_TEXT_COLOR_RED + "**You must be in game**\n";
            }
            if (params.length == 0) {
                server.leaveGame(activeGameId, this.authToken);
                // remember to set activeGame to null and playercolor
                state = State.POSTSIGNIN;
                activeGame = null;
                playerColor = null;
                return "\n";
            }
            return SET_TEXT_COLOR_RED + "Expected: *JUST LEAVE*\n";
        } catch (DataAccessException ex) {
            if (ex.statusCode() == 500){
                return SET_TEXT_COLOR_RED + "**Login already in use!**\n";
            }
        }
        return "\n";
    }

    public String move(String... params) throws DataAccessException {
        try {
            if (params.length == 2) {
                // need to parse the numbers and letters to convert them to coordinates
                if (!assertInGame()){
                    return SET_TEXT_COLOR_RED + "**You must be a player in a game**\n";
                }
                var result = Utility.validateAndParseCoordinates(params[0]);
                var result1 = Utility.validateAndParseCoordinates(params[1]);
                ChessPosition start = new ChessPosition((int) result.getFirst(), (int) result.getSecond());
                ChessPosition end = new ChessPosition((int) result1.getFirst(), (int) result1.getSecond());
                // make a route where the pawn is going to be promoted
                ChessMove move = new ChessMove(start, end, null);
                activeGame.makeMove(move);
                try{
                    server.makeMove(activeGameId, authToken, move);
                } catch (DataAccessException ex) {
                    this.leave();
                    return SET_TEXT_COLOR_RED + "**Connection Timed Out**\n";
                }
                BoardPrinter.printBasedOnPov(playerColor, activeGame.getBoard(), new ArrayList<ChessPosition>());
                return "\n";
            }
            else{
                return SET_TEXT_COLOR_RED + "**Expected: <start position(A-G/1-8)> <end position(A-G/1-8)>**\n";
            }
        } catch (DataAccessException | InvalidMoveException | RuntimeException e) {
            return SET_TEXT_COLOR_RED + e.getMessage();
        }
    }

    public String resign(String... params) throws DataAccessException {
        try{
            // make sure to verify they want to resign
            if (params.length == 0) {
                if (!assertInGame()){
                    return SET_TEXT_COLOR_RED + "**You must be a player in a game**\n";
                }
                if (!verifyResign()){
                    return "";
                }
                server.resignGame(activeGameId, authToken);
                return "\n";
            }
            return SET_TEXT_COLOR_RED + "Expected: *JUST RESIGN*\n";
        } catch (DataAccessException ex){
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }
    }

    private boolean verifyResign() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(RESET_BG_COLOR);
            System.out.print(SET_TEXT_COLOR_BLUE + "Are you sure you want to resign? (y)es/(n)o: ");
            String line = scanner.nextLine();
            try {
                return switch (line.toLowerCase()) {
                    case "y", "yes" -> true;
                    case "n", "no" -> false;
                    default -> throw new DataAccessException(500, "ERROR: Invalid Input");
                };
            } catch (Throwable e) {
                System.out.print(e.getMessage());
            }
        }
    }

    public String highlight(String... params) throws DataAccessException {
        if (params.length == 1) {
            if (assertLoggedIn()){
                return SET_TEXT_COLOR_RED + "**You must be in game**\n";
            }
            try{
                var result = Utility.validateAndParseCoordinates(params[0]);
                ChessPosition start = new ChessPosition((int) result.getFirst(), (int) result.getSecond());
                // print the board again based on the POV and the available piece moves
                Collection<ChessMove> moves = activeGame.validMoves(start);
                ArrayList<ChessPosition> positions = new ArrayList<>();
                for (ChessMove move : moves) {
                    positions.add(move.getEndPosition());
                }
                BoardPrinter.printBasedOnPov(playerColor, activeGame.getBoard(), positions);
            } catch (DataAccessException ex) {
                return SET_TEXT_COLOR_RED + ex.getMessage();
            }
        }
        else{
            return SET_TEXT_COLOR_RED + "** Expected: <piece position>**\n";
        }
        return "";
    }

    public void updateActiveGame(ChessGame game){
        activeGame = game;
    }

    private boolean assertLoggedIn() throws DataAccessException {
        return this.state == State.POSTSIGNIN;
    }

    private boolean assertInGame() throws DataAccessException {
        return this.state == State.INGAME;
    }

    private boolean assertObserving() throws DataAccessException {
        return this.state == State.OBSERVE;
    }

    public ChessGame getActiveGame() {
        return activeGame;
    }
}
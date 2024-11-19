package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import exception.DataAccessException;
import model.AuthData;
import model.UserData;
import requests.JoinGameRequest;
import server.ServerFacade;

import javax.xml.crypto.Data;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ui.EscapeSequences.*;

public class UiClient {
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    public State state = State.PRESIGNIN;

    public UiClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
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
//                result.append("ID: ").append(game.gameID()).append(",\t");
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
                    printWhitePov(games.get(num).game().getBoard());
                    System.out.println();
                    printBlackPov(games.get(num).game().getBoard());
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
        try{
            num = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException ex){
            return SET_TEXT_COLOR_RED + "**Expected: Game Number**\n";
        }
        if (num > -1 & num <= games.size() - 1){
            System.out.printf("**Currently Observing game %d**\n", num+1);
            printWhitePov(games.get(num).game().getBoard());
            System.out.println();
            printBlackPov(games.get(num).game().getBoard());
        }
        else{
            return SET_TEXT_COLOR_RED + "**Game not available**\n";
        }
        return "\n";
    }

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String EMPTY = " ";

    private void printWhitePov(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
//        out.print(ERASE_SCREEN);
        String[] headers = { "a", "b", "c", "d", "e", "f", "g", "h" };
        drawHeaders(out, headers);
        drawChessBoard(out, board, true, 0);
        drawHeaders(out, headers);
    }

    private void printBlackPov(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
//        out.print(ERASE_SCREEN);
        String[] headers = { "h", "g", "f", "e", "d", "c", "b", "a" };
        drawHeaders(out, headers);
        drawChessBoard(out, board, false, 7);
        drawHeaders(out, headers);
    }

    private static void printStart(PrintStream out, int rowNum) {
        setLightGrey(out);
        out.print(EMPTY);
        printHeaderText(out, Integer.toString(rowNum));
        setLightGrey(out);
        out.print(EMPTY);
    }

    private static void drawHeaders(PrintStream out, String[] headers) {
        setLightGrey(out);
        out.print(EMPTY.repeat(3));
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            out.print(EMPTY);
            printHeaderText(out, headers[boardCol]);
            setLightGrey(out);
            out.print(EMPTY);
        }
        out.print(EMPTY.repeat(3));
        setBlack(out);
        out.println();
    }

    private static void printHeaderText(PrintStream out, String text) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(text);
    }

    private static void drawChessBoard(PrintStream out, ChessBoard board, boolean increment, int start) {
        interface FuncInter{
            void execute();
        }
        for (int squareRow = start; (increment ? squareRow < BOARD_SIZE_IN_SQUARES : squareRow >= 0);
             squareRow += (increment ? 1 : -1)) {
            printStart(out, 8 - squareRow);
            for (int boardCol = start; (increment ? boardCol < BOARD_SIZE_IN_SQUARES : boardCol >= 0);
                 boardCol += (increment ? 1 : -1)) {
                // set white or black beforehand
                FuncInter func;
                if (squareRow % 2 == 0 & boardCol % 2 == 0){
                    func = () -> setWhite(out);
                }
                else if (squareRow % 2 == 0 & boardCol % 2 == 1){
                    func = () -> setBlack(out);
                }
                else if (squareRow % 2 == 1 & boardCol % 2 == 0){
                    func = () -> setBlack(out);
                }
                else{
                    func = () -> setWhite(out);
                }
                func.execute();
                out.print(EMPTY);
                // print based on what is next on the chessboard
                if (board.getPiece(new ChessPosition(8-squareRow, 1+boardCol)) != null){
                    if (board.getPiece(new ChessPosition(8-squareRow, 1+boardCol)).getTeamColor() == ChessGame.TeamColor.WHITE){
                        out.print(SET_TEXT_COLOR_RED);
                        out.print(pieceChar(board, 8-squareRow, 1+boardCol));
                    }
                    else{
                        out.print(SET_TEXT_COLOR_BLUE);
                        out.print(pieceChar(board, 8-squareRow, 1+boardCol));
                    }
                }
                else{
                    out.print(" ");
                }
                func.execute();
                out.print(EMPTY);
                setLightGrey(out);
            }
            printStart(out, 8 - squareRow);
            setBlack(out);
            out.println();
        }
    }

    private static String pieceChar(ChessBoard board, int row, int col) {
        return switch (board.getPiece(new ChessPosition(row, col)).getPieceType()) {
            case PAWN -> "P";
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case ROOK -> "R";
            case QUEEN -> "Q";
            case KING -> "K";
            default -> "";
        };
    }

    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_BLUE);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setLightGrey(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_LIGHT_GREY);
    }

    private void assertLoggedIn() throws DataAccessException {
        if (this.state == State.PRESIGNIN) {
            throw new DataAccessException(400, "You must sign in");
        }
    }
}
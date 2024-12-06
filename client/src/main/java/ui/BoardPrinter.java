package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import exception.DataAccessException;
import org.glassfish.grizzly.utils.Pair;
import requests.JoinGameRequest;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class BoardPrinter {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String EMPTY = " ";

    public BoardPrinter(){}

    public static void printBasedOnPov(JoinGameRequest.PlayerColor color, ChessBoard board){
        switch(color){
            case WHITE -> printWhitePov(board);
            case BLACK -> printBlackPov(board);
        }
    }

    public static void printWhitePov(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        String[] headers = { "a", "b", "c", "d", "e", "f", "g", "h" };
        drawHeaders(out, headers);
        drawChessBoard(out, board, true, 0);
        drawHeaders(out, headers);
    }

    public static void printBlackPov(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
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

    public static Pair<Integer, Integer> validateAndParseCoordinates(String coordinates) throws DataAccessException{
        if (coordinates.length() == 2) {
            char firstChar = coordinates.charAt(0);
            char secondChar = coordinates.charAt(1);

            if (Character.isLetter(firstChar) && Character.isDigit(secondChar)) {
                int letterValue = firstChar - 'a' + 1;
                int numberValue = Character.getNumericValue(secondChar);
                return new Pair<>(letterValue, numberValue);
            }
            else {
                return null;
            }
        }
        else {
            throw new DataAccessException(500, "");
        }
    }
}

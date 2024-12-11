package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import requests.JoinGameRequest;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class BoardPrinter {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String EMPTY = " ";

    public BoardPrinter(){}

    public static void printBasedOnPov(JoinGameRequest.PlayerColor color, ChessBoard board, ArrayList<ChessPosition> moves){
        switch(color){
            case WHITE -> printWhitePov(board, moves);
            case BLACK -> printBlackPov(board, moves);
        }
    }

    public static void printWhitePov(ChessBoard board, ArrayList<ChessPosition> moves) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        String[] headers = { "a", "b", "c", "d", "e", "f", "g", "h" };
        drawHeaders(out, headers);
        drawChessBoard(out, board, true, 0, moves);
        drawHeaders(out, headers);
    }

    public static void printBlackPov(ChessBoard board, ArrayList<ChessPosition> moves) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        String[] headers = { "h", "g", "f", "e", "d", "c", "b", "a" };
        drawHeaders(out, headers);
        drawChessBoard(out, board, false, 7, moves);
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
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        out.print(text);
    }

    private static void drawChessBoard(PrintStream out, ChessBoard board, boolean increment, int start, ArrayList<ChessPosition> highlights) {
        for (int squareRow = start; (increment ? squareRow < BOARD_SIZE_IN_SQUARES : squareRow >= 0);
             squareRow += (increment ? 1 : -1)) {
            printStart(out, 8 - squareRow);
            for (int boardCol = start; (increment ? boardCol < BOARD_SIZE_IN_SQUARES : boardCol >= 0);
                 boardCol += (increment ? 1 : -1)) {
                ChessPosition currentPosition = new ChessPosition(8 - squareRow, 1 + boardCol);
                boolean isHighlighted = highlights.contains(currentPosition);

                if (isHighlighted) {
                    setHighlight(out, (squareRow + boardCol) % 2 == 0);
                } else {
                    setSquareColor(out, (squareRow + boardCol) % 2 == 0);
                }

                out.print(EMPTY);
                if (board.getPiece(currentPosition) != null) {
                    var piece = board.getPiece(currentPosition);
                    out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? EscapeSequences.SET_TEXT_COLOR_RED
                            : EscapeSequences.SET_TEXT_COLOR_BLUE);
                    out.print(pieceChar(board, 8 - squareRow, 1 + boardCol));
                } else {
                    out.print(EMPTY);
                }

                if (isHighlighted) {
                    setHighlight(out, (squareRow + boardCol) % 2 == 0);
                } else {
                    setSquareColor(out, (squareRow + boardCol) % 2 == 0);
                }
                out.print(EMPTY);
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
        };
    }

    private static void setSquareColor(PrintStream out, boolean isWhite) {
        if (isWhite) {
            setWhite(out);
        } else {
            setBlack(out);
        }
    }

    private static void setHighlight(PrintStream out, boolean isWhite){
        if (isWhite){
            setGreen(out);
        }
        else{
            setDarkGreen(out);
        }
    }

    private static void setWhite(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
    }

    private static void setBlack(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
    }

    private static void setLightGrey(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY);
    }

    private static void setDarkGreen(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
    }

    private static void setGreen(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_GREEN);
    }
}

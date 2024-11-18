import chess.*;
import ui.UiRepl;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        UiRepl repl = new UiRepl(String.format("http://localhost:%s", "8080"));
        repl.run();
    }
}
import chess.*;
import ui.UiRepl;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws DeploymentException, URISyntaxException, IOException {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        UiRepl repl = new UiRepl(String.format("http://localhost:%s", "8080"));
        repl.run();
    }
}
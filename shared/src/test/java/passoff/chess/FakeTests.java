package passoff.chess;

import chess.ChessGame;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

public class FakeTests {

    @Test
    public void test() {
        ChessGame game = new ChessGame();
        Gson gson = new Gson();
        System.out.println(gson.toJson(game));
    }
}

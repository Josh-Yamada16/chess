package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.PieceCalculators.BaseMovement;

import java.util.ArrayList;
import java.util.Collection;

public class BishopRule extends BaseMovement {
    public BishopRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    public Collection<ChessMove> moves(){
        Collection<ChessMove> moveSet = new ArrayList<ChessMove>();
        moveCalculator(moveSet, 1, -1, true);
        moveCalculator(moveSet, 1, 1, true);
        moveCalculator(moveSet, -1, 1, true);
        moveCalculator(moveSet, -1, -1, true);
        return moveSet;
    }
}

package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class KingRule extends BaseMovementRule{


    public KingRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        calculateMoves(board, position, -1, -1, moves, false);
        calculateMoves(board, position, -1, 0, moves, false);
        calculateMoves(board, position, -1, 1, moves, false);
        calculateMoves(board, position, 0, -1, moves, false);
        calculateMoves(board, position, 0, 1, moves, false);
        calculateMoves(board, position, 1, -1, moves, false);
        calculateMoves(board, position, 1,0 , moves, false);
        calculateMoves(board, position, 1, 1, moves, false);

        return moves;
    }
    // override the
}

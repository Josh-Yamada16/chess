package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface PieceMovesInterface {
    Collection<ChessMove> moves(ChessBoard board, ChessPosition pos);

    boolean isValid(ChessBoard board, ChessPosition position, ChessPosition ogPosition);
}

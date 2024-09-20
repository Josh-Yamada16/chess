package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;

public class BaseMovementRule implements PieceMovesInterface {
    private ChessBoard board;
    private ChessPosition position;

    public BaseMovementRule(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int rowInc, int colInc, Collection<ChessMove> moves, boolean allowDistance) {
        int row = pos.getRow();
        int column = pos.getColumn();
        int multiplier = 1;
        do {
            ChessPosition possiblePos = new ChessPosition(Math.abs(8-row)+(rowInc * multiplier), Math.abs(1+column)+(colInc * multiplier));
            if (isValid(board, possiblePos, position)){
                moves.add(new ChessMove(position, possiblePos, null));
            }
            multiplier += 1;
        } while (allowDistance && (Math.abs(Math.abs(8-row)+(rowInc * multiplier)) > 8 || Math.abs(Math.abs(1+column)+(colInc * multiplier)) > 8));
    }

    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition pos) {
        return List.of();
    }

    @Override
    public boolean isValid(ChessBoard board, ChessPosition position, ChessPosition ogPosition) {
        // check if it's not going to fall off the board
        if (position.getRow() > 7 || position.getColumn() > 7){
            return false;
        }
        if ((board.getPiece(position)) == null) {
            return true;
        }
        // check if it's not going to move into the same team
        if ((board.getPiece(position)).getTeamColor() == board.getPiece(ogPosition).getTeamColor()) {
            return false;
        }
        return true;
    }
}
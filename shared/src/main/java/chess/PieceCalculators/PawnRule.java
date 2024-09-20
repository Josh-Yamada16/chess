package chess.PieceCalculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnRule extends BaseMovementRule{
    public PawnRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        int row = position.getRow();
        int col = position.getColumn();
        // white pawn
        if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE){
            // in promotion spot
            if (row == 1){
                promote(board, position, moves);
            }
            // in starting position
            else if (row == 6){
                calculateMoves(board, position, 2, 0, moves, false);
                calculateMoves(board, position, 1, 0, moves, false);
            }
            else{
                calculateMoves(board, position, 1, 0, moves, false);
            }
            // diag right check
            if (board.getPiece(new ChessPosition(8-row+1, 1+col+1)) != null){
                calculateMoves(board, position, 1, 1, moves, false);
            }
            //diag left check
            if (board.getPiece(new ChessPosition(8-row+1, 1+col-1)) != null){
                calculateMoves(board, position, 1, -1, moves, false);
            }
        }

        // black pawn
        else{
            // in promotion spot
            if (row == 6){
                promote(board, position, moves);
            }
            // in starting position
            else if (row == 1){
                calculateMoves(board, position, -2, 0, moves, false);
                calculateMoves(board, position, -1, 0, moves, false);
            }
            else{
                calculateMoves(board, position, -1, 0, moves, false);
            }
            // diag right check
            if (board.getPiece(new ChessPosition(8-row-1, 1+col+1)) != null){
                calculateMoves(board, position, -1, 1, moves, false);
            }
            //diag left check
            if (board.getPiece(new ChessPosition(8-row-1, 1+col-1)) != null){
                calculateMoves(board, position, -1, -1, moves, false);
            }
        }
        return moves;
    }

    public void promote(ChessBoard board, ChessPosition position, HashSet<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();
        ChessPosition end;
        if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE){
            end = new ChessPosition(8-row+1, 1+col);
        }
        else{
            end = new ChessPosition(8-row-1, 1+col);
        }
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.KNIGHT));
    }
}

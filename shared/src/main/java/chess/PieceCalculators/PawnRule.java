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
            if (row == 1 && isValidPawn(board, new ChessPosition(8-row+1, 1+col), position, false)){
                promote(position, moves, board.getPiece(position).getTeamColor(), false, false);
            }
            else if (isValidPawn(board, new ChessPosition(8-row+1, 1+col), position, false)){
                moves.add(new ChessMove(position, new ChessPosition(8-row+1, 1+col), null));
                // in starting position
                if (row == 6 && isValidPawn(board, new ChessPosition(8-row+2, 1+col), position, false)){
                    moves.add(new ChessMove(position, new ChessPosition(8-row+2, 1+col), null));
                }
            }
            // diag right check
            if (isValidPawn(board, new ChessPosition(8-row+1, 1+col+1), position, true)){
                if (row - 1 == 0){
                    promote(position, moves, board.getPiece(position).getTeamColor(), true, true);
                }
                else{
                    moves.add(new ChessMove(position, new ChessPosition(8-row+1, 1+col+1), null));
                }
            }
            //diag left check
            if (isValidPawn(board, new ChessPosition(8-row+1, 1+col-1), position, true)){
                if (row - 1 == 0){
                    promote(position, moves, board.getPiece(position).getTeamColor(), false, false);
                }
                else{
                    moves.add(new ChessMove(position, new ChessPosition(8-row+1, 1+col-1), null));
                }
            }
        }

        // black pawn
        else{
            // in promotion spot
            if (row == 6 && isValidPawn(board, new ChessPosition(8-row-1, 1+col), position, false)){
                promote(position, moves, board.getPiece(position).getTeamColor(), false, false);
            }
            else if (isValidPawn(board, new ChessPosition(8-row-1, 1+col), position, false)){
                moves.add(new ChessMove(position, new ChessPosition(8-row-1, 1+col), null));
                // in starting position
                if (row == 1 && isValidPawn(board, new ChessPosition(8-row-2, 1+col), position, false)){
                    moves.add(new ChessMove(position, new ChessPosition(8-row-2, 1+col), null));
                }
            }
            // diag right check
            if (isValidPawn(board, new ChessPosition(8-row-1, 1+col+1), position, true)) {
                if (row + 1 == 7) {
                    promote(position, moves, board.getPiece(position).getTeamColor(), true, true);
                }
                else{
                    moves.add(new ChessMove(position, new ChessPosition(8 - row - 1, 1 + col + 1), null));
                }
            }
            //diag left check
            if (isValidPawn(board, new ChessPosition(8-row-1, 1+col-1), position, true)){
                if (row + 1 == 7) {
                    promote(position, moves, board.getPiece(position).getTeamColor(), true, false);
                }
                else{
                    moves.add(new ChessMove(position, new ChessPosition(8 - row - 1, 1 + col - 1), null));
                }
            }
        }
        return moves;
    }

    public void promote(ChessPosition position, HashSet<ChessMove> moves, ChessGame.TeamColor color, boolean attacking, boolean right) {
        // maybe make this more general for straight and diagonal promotions
        int row = position.getRow();
        int col = position.getColumn();
        ChessPosition end;
        if (attacking){
            if (right){
                if (color == ChessGame.TeamColor.WHITE){
                    end = new ChessPosition(8-row+1, 1+col+1);
                }
                else{
                    end = new ChessPosition(8-row-1, 1+col+1);
                }
            }
            else{
                if (color == ChessGame.TeamColor.WHITE){
                    end = new ChessPosition(8-row+1, 1+col-1);
                }
                else{
                    end = new ChessPosition(8-row-1, 1+col-1);
                }
            }
        }
        else{
            if (color == ChessGame.TeamColor.WHITE){
                end = new ChessPosition(8-row+1, 1+col);
            }
            else{
                end = new ChessPosition(8-row-1, 1+col);
            }
        }
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(position, end, ChessPiece.PieceType.KNIGHT));
    }

    public boolean isValidPawn(ChessBoard board, ChessPosition position, ChessPosition ogPosition, boolean diagMove) {
        // check if it's not going to fall off the board
        if (position.getRow() > 7 || position.getColumn() > 7 || position.getRow() < 0 || position.getColumn() < 0){
            return false;
        }
        if (diagMove){
            if ((board.getPiece(position)) == null) {
                return false;
            }
            else if (board.getPiece(position).getTeamColor() == board.getPiece(ogPosition).getTeamColor()){
                return false;
            }
            return true;
        }
        if (board.getPiece(position) == null){
            return true;
        }
        return false;
    }}

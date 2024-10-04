package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private int round = 0;
    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        if (round % 2 == 0){
            return TeamColor.WHITE;
        }
        return TeamColor.BLACK;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        var moveSet = piece.pieceMoves(board, startPosition);
        ChessBoard testBoard = new ChessBoard(this.board);


        return null;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // check if move is in the collection from validmoves otherwise throws exception
        throw new InvalidMoveException();
        this.round++;
    }

    public boolean kingGettingAttacked(TeamColor team, ChessBoard testBoard){
        ChessPosition kingPos = board.getKingPos(team);
        if (testBoard != null) {
            kingPos = testBoard.getKingPos(team);
        }
        // loop through every piece on the board and find the opposite teams pieces
        for (int i = 0; i < 7; i++){
            for (int j = 0; j < 7; j++){
                ChessPiece queryPiece = board.getPiece(new ChessPosition(8-i, j+1));
                if (queryPiece.getTeamColor() != team){
                    // loop through their moveset and see if the king's position is in their getEndposition
                    var moveSet = queryPiece.pieceMoves(board, new ChessPosition(8-i, j+1));
                    for (ChessMove item : moveSet) {
                        // if getEndPosition() is == to king position then return true
                        if (item.getEndPosition() == kingPos){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // if kingGettingAttacked() then return true
        return kingGettingAttacked(teamColor, null);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // if isInCheck() and validMoves() is empty for all pieces then return true
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if isInCheck() is false and validMoves() is empty for all pieces then return true
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "ChessGame{}";
    }
}

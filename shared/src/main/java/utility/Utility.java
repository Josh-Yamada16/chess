package utility;

import chess.ChessMove;
import chess.ChessPosition;
import exception.DataAccessException;
import org.glassfish.grizzly.utils.Pair;

public class Utility {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String EMPTY = " ";

    public Utility(){}

    public static Pair<Integer, Integer> validateAndParseCoordinates(String coordinates) throws DataAccessException{
        char firstChar = coordinates.charAt(0);
        char secondChar = coordinates.charAt(1);

        if (Character.isLetter(firstChar) && Character.isDigit(secondChar)) {
            int letterValue = Character.toLowerCase(firstChar) - 'a' + 1;
            if (letterValue > 8 || letterValue < 1) {
                throw new DataAccessException(500, "**Expected: a letter A-G**");
            }
            int numberValue = Character.getNumericValue(secondChar);
            if (numberValue > 8 || numberValue < 1) {
                throw new DataAccessException(500, "**Expected: a number 1-8**");
            }
            return new Pair<>(letterValue, numberValue);
        }
        else {
            throw new DataAccessException(500, "**Expected: A-G/1-8**");
        }
    }

    public static Pair<String, String> convertMoveToString(ChessMove move){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        String first = (char) ((start.getColumn()+1) + 'a' - 1) + Integer.toString(8-start.getRow());
        String sec = (char) ((end.getColumn()+1) + 'a' - 1) + Integer.toString(8-end.getRow()) ;
        return new Pair<>(first, sec);
    }
}

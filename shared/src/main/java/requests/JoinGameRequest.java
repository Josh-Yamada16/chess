package requests;

public class JoinGameRequest {
    private final PlayerColor playerColor;
    private final int gameID;

    public JoinGameRequest(PlayerColor playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public enum PlayerColor { WHITE, BLACK }

    public int getGameID() {
        return gameID;
    }

    public JoinGameRequest.PlayerColor getPlayerColor() {
        return playerColor;
    }
}

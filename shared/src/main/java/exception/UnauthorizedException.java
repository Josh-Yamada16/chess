package exception;

/**
 * Indicates there was an error connecting to the database
 */
public class UnauthorizedException extends Exception{
    final private int statusCode;

    public UnauthorizedException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int StatusCode() {
        return statusCode;
    }
}

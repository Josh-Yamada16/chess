package exception;

/**
 * Indicates there was an error connecting to the database
 */
public class BadRequestException extends Exception{
    final private int statusCode;

    public BadRequestException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int StatusCode() {
        return statusCode;
    }
}

package exceptions;

public class BadFormatMessageException extends Exception {
    public BadFormatMessageException() {
        super();
    }

    public BadFormatMessageException(String message) {
        super(message);
    }

    public BadFormatMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadFormatMessageException(Throwable cause) {
        super(cause);
    }

    protected BadFormatMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package awsfuzz.generators;


public class ServerlessGeneratorException extends Exception {
    public ServerlessGeneratorException(String message) {
        super(message);
    }

    public ServerlessGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}

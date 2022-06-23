package exception;

public class ObjectCannotBeClonedException extends RuntimeException {

    public ObjectCannotBeClonedException(String message) {
        super(message);
    }

    public ObjectCannotBeClonedException(Exception ex) {
        super(ex);
    }

}

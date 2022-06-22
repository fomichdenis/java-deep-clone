package exception;

public class ObjectCannotBeClonedException extends RuntimeException {

    public ObjectCannotBeClonedException(Exception ex) {
        super(ex);
    }

}

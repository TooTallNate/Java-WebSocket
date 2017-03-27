package org.java_websocket.exceptions;

/**
 * exception which indicates the frame payload is not sendable
 */
public class NotSendableException extends RuntimeException {

    /**
     * Serializable
     */
    private static final long serialVersionUID = -6468967874576651628L;

    /**
     * constructor for a NotSendableException
     */
    public NotSendableException() {
        super();
    }

    /**
     * constructor for a NotSendableException
     *
     * @param s the detail message.
     */
    public NotSendableException(String s) {
        super(s);
    }

    /**
     * constructor for a NotSendableException
     *
     * @param t the throwable causing this exception.
     */
    public NotSendableException(Throwable t) {
        super(t);
    }

    /**
     * constructor for a NotSendableException
     *
     * @param s the detail message.
     * @param t the throwable causing this exception.
     */
    public NotSendableException(String s, Throwable t) {
        super(s, t);
    }

}

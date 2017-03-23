package org.java_websocket.exceptions;

/**
 * exception which indicates that a invalid data was recieved
 */
public class InvalidDataException extends Exception {

    /**
     * Serializable
     */
    private static final long serialVersionUID = 3731842424390998726L;

    /**
     * attribut which closecode will be returned
     */
    private int closecode;

    /**
     * constructor for a InvalidDataException
     *
     * @param closecode the closecode which will be returned
     */
    public InvalidDataException(int closecode) {
        this.closecode = closecode;
    }

    /**
     * constructor for a InvalidDataException.
     *
     * @param closecode the closecode which will be returned.
     * @param s         the detail message.
     */
    public InvalidDataException(int closecode, String s) {
        super(s);
        this.closecode = closecode;
    }

    /**
     * constructor for a InvalidDataException.
     *
     * @param closecode the closecode which will be returned.
     * @param t         the throwable causing this exception.
     */
    public InvalidDataException(int closecode, Throwable t) {
        super(t);
        this.closecode = closecode;
    }

    /**
     * constructor for a InvalidDataException.
     *
     * @param closecode the closecode which will be returned.
     * @param s         the detail message.
     * @param t         the throwable causing this exception.
     */
    public InvalidDataException(int closecode, String s, Throwable t) {
        super(s, t);
        this.closecode = closecode;
    }

    /**
     * Getter closecode
     *
     * @return the closecode
     */
    public int getCloseCode() {
        return closecode;
    }

}

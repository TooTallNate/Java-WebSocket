package org.java_websocket.exceptions;

/**
 * exception which indicates that a incomplete handshake was recieved
 */
public class IncompleteHandshakeException extends RuntimeException {

    /**
     * Serializable
     */
    private static final long serialVersionUID = 7906596804233893092L;

    /**
     * attribut which size of handshake would have been prefered
     */
    private int preferedSize;

    /**
     * constructor for a IncompleteHandshakeException
     * <p>
     * @param preferedSize the prefered size
     */
    public IncompleteHandshakeException(int preferedSize) {
        this.preferedSize = preferedSize;
    }

    /**
     * constructor for a IncompleteHandshakeException
     * <p>
     * preferedSize will be 0
     */
    public IncompleteHandshakeException() {
        this.preferedSize = 0;
    }

    /**
     * Getter preferedSize
     *
     * @return the preferedSize
     */
    public int getPreferedSize() {
        return preferedSize;
    }

}

package com.github.jearls.SPRaceTracker.data;

/**
 * This exception indicates that a requested object was not able found in the
 * data store.
 * 
 * @author jearls
 */
public class DataStoreNotFoundException extends DataStoreException {
    public static final long serialVersionUID = 1L;

    public DataStoreNotFoundException() {
        super();
    }

    public DataStoreNotFoundException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataStoreNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataStoreNotFoundException(String message) {
        super(message);
    }

    public DataStoreNotFoundException(Throwable cause) {
        super(cause);
    }
}

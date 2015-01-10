package com.github.jearls.SPRaceTracker.data.importexport;

public class ImporterExporterException extends Exception {
    public static final long serialVersionUID = 1L;

    public ImporterExporterException() {
    }

    public ImporterExporterException(String message) {
        super(message);
    }

    public ImporterExporterException(Throwable cause) {
        super(cause);
    }

    public ImporterExporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImporterExporterException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

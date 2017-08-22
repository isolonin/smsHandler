package ru.autohelp.models;

/**
 *
 * @author Ivan
 */
public class MegaFonSms {
    private final String from;
    private final Long to;
    private final String message;

    public MegaFonSms(String from, Long to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }
}

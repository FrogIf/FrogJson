package sch.frog.frogjson.json;

public class JsonToken {

    private int start;

    private String literal;

    private Type type;

    private boolean error;

    public JsonToken(int start, String literal, Type type) {
        this.start = start;
        this.literal = literal;
        this.type = type;
        this.error = false;
    }

    public JsonToken(int start, String literal, Type type, boolean error) {
        this.start = start;
        this.literal = literal;
        this.type = type;
        this.error = error;
    }

    public int getStart() {
        return start;
    }

    public String getLiteral() {
        return literal;
    }

    public Type getType() {
        return type;
    }

    public boolean isError() {
        return error;
    }

    public enum Type{
        KEY,
        STR_VALUE,
        BOOL,
        NUMBER,
        NULL,
        STRUCTURE,
        BLANK,
        UNKNOWN;
    }
}

package sch.frog.frogjson.json;

public class JsonToken {

    private final int start;

    private final String literal;

    private final Type type;

    private final boolean error;

    public JsonToken(int start, String literal, Type type) {
        this(start, literal, type, false);
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

    public String getOrigin(){
        if(Type.KEY == this.type || Type.STR_VALUE == this.type){
            return "\"" + JsonEscapeUtils.unescape(this.literal.substring(1, this.literal.length() - 1)) + "\"";
        }else{
            return this.literal;
        }
    }

    public enum Type{
        /**
         * key, 字符串
         */
        KEY,
        /**
         * 值, 字符串
         */
        STR_VALUE,
        /**
         * 值, 布尔类型
         */
        BOOL,
        /**
         * 值, 数值类型, 支持科学计数法
         */
        NUMBER,
        /**
         * 值, null
         */
        NULL,
        /**
         * 结构token: {}[],:
         */
        STRUCTURE,
        /**
         * 空格, 换行, 制表符等
         */
        BLANK,
        /**
         * 未识别的
         */
        UNKNOWN;
    }
}

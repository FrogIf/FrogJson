package sch.frog.frogjson.json;

public interface JsonValue<V> {
    void write(IJsonValueWriter writer);

    V getValue();
}

class StringJsonValue implements JsonValue<String> {

    private final String val;

    public StringJsonValue(String val) {
        this.val = fixVal(val);
    }

    public String getValue() {
        return val;
    }

    @Override
    public void write(IJsonValueWriter writer) {
        writer.writeString(this.val);
    }

    private String fixVal(String val){
        if(val != null){
            StringBuilder sb = new StringBuilder();
            for(int i = 0, len = val.length(); i < len; i++){
                char ch = val.charAt(i);
                if(ch == '\n'){
                    sb.append("\\n");
                }else{
                    sb.append(ch);
                }
            }
            val = sb.toString();
        }
        return val;
    }

}

class ConstJsonValue implements JsonValue<String> {

    private final String value;

    public ConstJsonValue(String value) {
        this.value = value;
    }

    @Override
    public void write(IJsonValueWriter writer) {
        writer.writeOrigin(value);
    }

    @Override
    public String getValue() {
        return value;
    }
}

class NumberJsonValue implements JsonValue<String> {

    private final String number;

    public NumberJsonValue(String number) {
        this.number = number;
    }

    public String getValue() {
        return this.number;
    }

    @Override
    public void write(IJsonValueWriter writer) {
        writer.writeOrigin(this.number);
    }
}

class ObjectJsonValue implements JsonValue<JsonObject> {

    private final JsonObject jsonObject;

    public ObjectJsonValue(JsonObject jsonObject) {
        if (jsonObject == null) {
            throw new IllegalArgumentException("jsonObject is null");
        }
        this.jsonObject = jsonObject;
    }

    public JsonObject getValue() {
        return this.jsonObject;
    }

    @Override
    public void write(IJsonValueWriter writer) {
        writer.writeObject(this.jsonObject);
    }
}

class ArrayJsonValue implements JsonValue<JsonArray> {

    private final JsonArray jsonArray;

    public ArrayJsonValue(JsonArray jsonArray) {
        if (jsonArray == null) {
            throw new IllegalArgumentException("jsonArray is null");
        }
        this.jsonArray = jsonArray;
    }

    @Override
    public void write(IJsonValueWriter writer) {
        writer.writeArray(this.jsonArray);
    }

    @Override
    public JsonArray getValue() {
        return this.jsonArray;
    }
}



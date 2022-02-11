package sch.frog.frogjson.json;

import java.util.ArrayList;
import java.util.Iterator;

public final class JsonArray implements JsonElement {

    private final ArrayList<JsonValue<?>> list = new ArrayList<>();

    void addJsonValue(JsonValue<?> jsonValue){
        list.add(jsonValue);
    }

    void addObject(JsonObject jsonObject)
    {
        list.add(new ObjectJsonValue(jsonObject));
    }

    void addArray(JsonArray jsonArray)
    {
        list.add(new ArrayJsonValue(jsonArray));
    }

    public Iterator<JsonValue<?>> getIterator(){
        Iterator<JsonValue<?>> iterator = list.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public JsonValue<?> next() {
                return iterator.next();
            }
        };
    }

    @Override
    public String toCompressString() {
        CompactJsonWriter writer = new CompactJsonWriter();
        writer.writeArray(this);
        return writer.toJson();
    }

    @Override
    public String toPrettyString() {
        PrettyJsonWriter writer = new PrettyJsonWriter();
        writer.writeArray(this);
        return writer.toJson();
    }

    @Override
    public void customWrite(IJsonWriter jsonWriter) {
        jsonWriter.writeArray(this);
    }
}

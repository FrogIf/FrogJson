package sch.frog.frogjson.json;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JsonObject implements JsonElement {

    private final LinkedHashMap<String, JsonValue<?>> kvMap = new LinkedHashMap<>();

    void putJsonValue(String key, JsonValue<?> jsonValue){
        kvMap.put(key, jsonValue);
    }

    void putObject(String key, JsonObject jsonObject) {
        kvMap.put(key, new ObjectJsonValue(jsonObject));
    }

    void putArray(String key, JsonArray jsonArray) {
        kvMap.put(key, new ArrayJsonValue(jsonArray));
    }

    public Iterator<Map.Entry<String, JsonValue<?>>> getIterator() {
        Set<Map.Entry<String, JsonValue<?>>> entries = kvMap.entrySet();
        Iterator<Map.Entry<String, JsonValue<?>>> iterator = entries.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Map.Entry<String, JsonValue<?>> next() {
                Map.Entry<String, JsonValue<?>> entry = iterator.next();
                return new Map.Entry<>() {
                    @Override
                    public String getKey() {
                        return entry.getKey();
                    }

                    @Override
                    public JsonValue<?> getValue() {
                        return entry.getValue();
                    }

                    @Override
                    public JsonValue<?> setValue(JsonValue<?> value) {
                        throw new UnsupportedOperationException("setValue");
                    }
                };
            }
        };
    }

    @Override
    public String toCompressString() {
        CompactJsonWriter writer = new CompactJsonWriter();
        writer.writeObject(this);
        return writer.toJson();
    }

    @Override
    public String toPrettyString() {
        PrettyJsonWriter writer = new PrettyJsonWriter();
        writer.writeObject(this);
        return writer.toJson();
    }

    @Override
    public void customWrite(IJsonWriter jsonWriter) {
        jsonWriter.writeObject(this);
    }
}

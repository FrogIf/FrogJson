package sch.frog.frogjson.json;

import java.util.Iterator;
import java.util.Map;

class CompactJsonWriter implements IJsonWriter, IJsonValueWriter {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public void writeString(String str) {
        sb.append(JsonWord.QUOTATION).append(str).append(JsonWord.QUOTATION);
    }

    @Override
    public void writeOrigin(String origin) {
        sb.append(origin);
    }

    @Override
    public void writeObject(JsonObject jsonObject) {
        sb.append(JsonWord.OBJECT_BEGIN);
        Iterator<Map.Entry<String, JsonValue<?>>> iterator = jsonObject.getIterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                sb.append(JsonWord.COMMA);
            }
            Map.Entry<String, JsonValue<?>> entry = iterator.next();
            sb.append(JsonWord.QUOTATION).append(entry.getKey()).append(JsonWord.QUOTATION).append(JsonWord.COLON);
            entry.getValue().write(this);
        }
        sb.append(JsonWord.OBJECT_END);
    }

    @Override
    public void writeArray(JsonArray array) {
        sb.append(JsonWord.ARRAY_BEGIN);
        Iterator<JsonValue<?>> iterator = array.getIterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                sb.append(JsonWord.COMMA);
            }
            JsonValue<?> jsonValue = iterator.next();
            jsonValue.write(this);

        }
        sb.append(JsonWord.ARRAY_END);
    }

    @Override
    public String toJson() {
        return sb.toString();
    }

}

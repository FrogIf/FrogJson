package sch.frog.frogjson.json;

public interface IJsonWriter{

    void writeObject(JsonObject jsonObject);

    void writeArray(JsonArray array);

    String toJson();

}

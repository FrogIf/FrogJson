package sch.frog.frogjson.json;

public interface JsonElement {

    String toCompressString();

    String toPrettyString();

    void customWrite(IJsonWriter jsonWriter);
}

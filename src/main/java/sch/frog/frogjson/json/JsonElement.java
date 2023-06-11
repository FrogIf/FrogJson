package sch.frog.frogjson.json;

public interface JsonElement {

    String toCompressString();

    String toPrettyString();

    String toCompressString(SerializerConfiguration configuration);

    String toPrettyString(SerializerConfiguration configuration);

    void customWrite(IJsonWriter jsonWriter);
}

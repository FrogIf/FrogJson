package sch.frog.frogjson.json;

import java.util.List;

public class JsonOperator {

    public static JsonElement parse(String json) throws JsonParseException {
        return parse(json, DeserializerFeature.Escape, DeserializerFeature.AbortWhenIncorrect);
    }

    public static JsonElement parse(String json, DeserializerFeature... features) throws JsonParseException {
        // 词法分析
        List<JsonToken> tokens = JsonLexicalAnalyzer.lexicalAnalysis(json, features);
        if(!tokens.isEmpty()){
            JsonToken last = tokens.get(tokens.size() - 1);
            if(last.isError()){
                JsonParseUtil.triggerException(last, json);
            }
        }

        // 语法分析
        return JsonSyntacticAnalysis.syntacticAnalysis(tokens, json);
    }

    public static JsonElement load(Object obj){
        if(obj instanceof Iterable){
            return JsonArray.load((Iterable)obj);
        }else{
            return JsonObject.load(obj);
        }
    }

}

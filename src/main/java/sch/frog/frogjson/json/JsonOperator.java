package sch.frog.frogjson.json;

import java.util.List;

public class JsonOperator {

    public static JsonElement parse(String json) throws JsonParseException {
        // 词法分析
        List<JsonToken> tokens = JsonLexicalAnalyzer.lexicalAnalysis(json, true);
        if(!tokens.isEmpty()){
            JsonToken last = tokens.get(tokens.size() - 1);
            if(last.isError()){
                JsonParseUtil.triggerException(last, json);
            }
        }

        // 语法分析
        return JsonSyntacticAnalysis.syntacticAnalysis(tokens, json);
    }

}

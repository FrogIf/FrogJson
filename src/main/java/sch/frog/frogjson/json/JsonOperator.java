package sch.frog.frogjson.json;

import java.util.List;

public class JsonOperator {

    public static JsonElement parse(String json) throws JsonParseException {
        // 词法分析
        List<Token> tokens = JsonLexicalAnalyzer.lexicalAnalysis(json);

        // 语法分析
        return JsonSyntacticAnalysis.syntacticAnalysis(tokens);
    }

}

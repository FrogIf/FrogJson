package sch.frog.frogjson.json;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class JsonLexicalAnalyzer {

    private JsonLexicalAnalyzer() {
        // do nothing
    }

    public static List<JsonToken> lexicalAnalysis(String json, boolean abortWhenIncorrect){
        LinkedList<JsonToken> tokens = new LinkedList<>();
        StringBuilder illegalStr = new StringBuilder();
        boolean waitValue = false;
        Stack<Integer> objOrArr = new Stack<>(); // 0 - obj, 1 - array
        String lastNormalLiteral = "";    // 上一个正确的非blank token
        JsonToken.Type preNormalType = null;
        boolean finish = false;
        int i = 0;
        for(int len = json.length(); i < len; i++){
            char ch = json.charAt(i);
            JsonToken t = null;
            switch (ch){
                case '{':
                    if((!finish && objOrArr.isEmpty()) || waitValue){
                        t = new JsonToken(i, "{", JsonToken.Type.STRUCTURE);
                    }else{
                        t = new JsonToken(i, "{", JsonToken.Type.STRUCTURE, true);
                    }
                    objOrArr.push(0);
                    break;
                case '}':
                    if(stackPeek(objOrArr) == 0 && !waitValue && preNormalType != JsonToken.Type.KEY && !",".equals(lastNormalLiteral)){
                        t = new JsonToken(i, "}", JsonToken.Type.STRUCTURE);
                        objOrArr.pop();
                    }else{
                        t = new JsonToken(i, "}", JsonToken.Type.STRUCTURE, true);
                    }
                    break;
                case ':':
                    if(stackPeek(objOrArr) == 0 && preNormalType == JsonToken.Type.KEY){
                        t = new JsonToken(i, ":", JsonToken.Type.STRUCTURE);
                    }else{
                        t = new JsonToken(i, ":", JsonToken.Type.STRUCTURE, true);
                    }
                    break;
                case '[':
                    if(waitValue || (!finish && objOrArr.isEmpty())){
                        t = new JsonToken(i, "[", JsonToken.Type.STRUCTURE);
                    }else{
                        t = new JsonToken(i, "[", JsonToken.Type.STRUCTURE, true);
                    }
                    objOrArr.push(1);
                    break;
                case ']':
                    if(stackPeek(objOrArr) == 1){
                        if(!",".equals(lastNormalLiteral)){
                            t = new JsonToken(i, "]", JsonToken.Type.STRUCTURE);
                        }
                        objOrArr.pop();
                    }
                    if(t == null){
                        t = new JsonToken(i, "]", JsonToken.Type.STRUCTURE, true);
                    }
                    break;
                case ',':
                    if(!waitValue){
                        t = new JsonToken(i, ",", JsonToken.Type.STRUCTURE);
                    }else{
                        t = new JsonToken(i, ",", JsonToken.Type.STRUCTURE, true);
                    }
                    break;
                case 'n':
                    String subStr = trySubString(json, i, 4);
                    if("null".equals(subStr)){
                        if(waitValue){
                            t = new JsonToken(i, "null", JsonToken.Type.NULL);
                        }else{
                            t = new JsonToken(i, "null", JsonToken.Type.NULL, true);
                        }
                        i += 3;
                    }
                    break;
                case 't':
                    subStr = trySubString(json, i, 4);
                    if("true".equals(subStr)){
                        if(waitValue){
                            t = new JsonToken(i, "true", JsonToken.Type.BOOL);
                        }else{
                            t = new JsonToken(i, "true", JsonToken.Type.BOOL, true);
                        }
                        i += 3;
                    }
                    break;
                case 'f':
                    subStr = trySubString(json, i, 5);
                    if("false".equals(subStr)){
                        if(waitValue){
                            t = new JsonToken(i, "false", JsonToken.Type.BOOL);
                        }else{
                            t = new JsonToken(i, "false", JsonToken.Type.BOOL, true);
                        }
                        i += 4;
                    }
                    break;
                case '"':
                    subStr = matchString(json, i);
                    if(subStr != null){
                        if(waitValue){
                            t = new JsonToken(i, subStr, JsonToken.Type.STR_VALUE);
                        }else {
                            if("{".equals(lastNormalLiteral) || ",".equals(lastNormalLiteral)){
                                t = new JsonToken(i, subStr, JsonToken.Type.KEY);
                            }else{
                                t = new JsonToken(i, subStr, JsonToken.Type.KEY, true);
                            }
                        }
                        i += subStr.length() - 1;
                    }
                    break;
                default:
                    if(isWhitespace(ch)){
                        String s = matchWhitespace(json, i);
                        t = new JsonToken(i, s, JsonToken.Type.BLANK);
                        i += s.length() - 1;
                    }else{
                        String number = matchNumber(json, i);
                        if(number != null){
                            if(waitValue){
                                t = new JsonToken(i, number, JsonToken.Type.NUMBER);
                            }else{
                                t = new JsonToken(i, number, JsonToken.Type.NUMBER, true);
                            }
                            i += number.length() - 1;
                        }
                    }
                    break;
            }
            if(ch == '[' || (ch == ':' && stackPeek(objOrArr) == 0) || (ch == ','&& stackPeek(objOrArr) == 1)){
                waitValue = true;
            }else if(!isWhitespace(ch) && t != null){
                waitValue = false;
            }
            if(t == null){
                illegalStr.append(ch);
            }else {
                if(illegalStr.length() > 0){
                    tokens.add(new JsonToken(i - illegalStr.length(), illegalStr.toString(), JsonToken.Type.UNKNOWN));
                    illegalStr = new StringBuilder();
                    if(abortWhenIncorrect){ break; }
                }
                if(!t.isError() && t.getType() != JsonToken.Type.BLANK){
                    lastNormalLiteral = t.getLiteral();
                    preNormalType = t.getType();
                    if(objOrArr.isEmpty()){
                        finish = true;
                    }
                }
                tokens.add(t);
                if(t.isError() && abortWhenIncorrect){
                    break;
                }
            }
        }
        if(illegalStr.length() > 0){
            tokens.add(new JsonToken(i - illegalStr.length(), illegalStr.toString(), JsonToken.Type.UNKNOWN));
        }
        if(!objOrArr.isEmpty()){
            JsonToken last = tokens.removeLast();
            tokens.add(new JsonToken(last.getStart(), last.getLiteral(), last.getType(), true));
        }
        return tokens;
    }

    private static int stackPeek(Stack<Integer> stack){
        return stack.isEmpty() ? -1 : stack.peek();
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    private static String matchWhitespace(String str, int start){
        StringBuilder sb = new StringBuilder();
        for(int i = start, len = str.length(); i < len; i++){
            char ch = str.charAt(i);
            if(!isWhitespace(ch)){
                break;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private static String matchString(String str, int start){
        StringBuilder result = new StringBuilder();
        int i = start;
        int len = str.length();
        char ch = str.charAt(i);
        if (ch != '"') { return result.toString(); }
        i++;
        result.append('"');
        while (i < len) {
            ch = str.charAt(i);
            if (ch == '"') {
                result.append('"');
                break;
            } else if (ch == '\\') {
                i++;
                if (i >= len) {
                    return result.toString();
                }
                ch = str.charAt(i);
                if (isEscape(ch)) {
                    result.append('\\').append(ch);
                    i++;
                } else if (ch == 'u')  // 4 hex digits
                {
                    i++;
                    if (i + 4 > len) {
                        return result.toString();
                    }
                    result.append("\\u");
                    for (int j = 0; j < 4; j++) {
                        ch = str.charAt(i + j);
                        if (isHex(ch)) {
                            result.append(ch);
                        } else {
                            return result.toString();
                        }
                    }
                    i += 4;
                } else {
                    return result.toString();
                }
            } else if(ch == '\n'){
                return null;
            }else{
                result.append(ch);
                i++;
            }
        }
        return result.toString();
    }

    private static boolean isNotDigit(char ch) {
        return ch < '0' || ch > '9';
    }

    private static String matchNumber(String str, int start){
        StringBuilder number = new StringBuilder();
        int i = start;
        char ch = str.charAt(i);
        if(ch == '-'){
            number.append(ch);
            i++;
        }

        ch = str.charAt(i);
        if(isNotDigit(ch)){
            return null;
        }

        number.append(ch);
        i++;

        int len = str.length();
        if(ch != '0'){
            for(; i < len; i++){
                ch = str.charAt(i);
                if(isNotDigit(ch)){ break; }
                number.append(ch);
            }
        }

        if(ch == '.'){
            i++;
            ch = str.charAt(i);
            if(isNotDigit(ch)){
                return number.toString();
            }
            number.append('.');
            for(; i < len; i++){
                ch = str.charAt(i);
                if(isNotDigit(ch)){ break; }
                number.append(ch);
            }
        }

        if(ch == 'e' || ch == 'E'){
            i++;
            char p = str.charAt(i);
            if(isNotDigit(p)){
                if(p == '+' || p == '-'){
                    i++;
                    char p2 = str.charAt(i);
                    if(isNotDigit(p2)){
                        return number.toString();
                    }else{
                        number.append(ch).append(p);
                    }
                }else{
                    return number.toString();
                }
            }else{
                number.append(ch);
            }

            for(; i < len; i++){
                ch = str.charAt(i);
                if(isNotDigit(ch)){ break; }
                number.append(ch);
            }
        }

        return number.toString();
    }

    private static String trySubString(String str, int start, int len){
        if(str.length() >= start + len){
            return str.substring(start, start + len);
        }else{
            return str.substring(start);
        }
    }

    private static boolean isEscape(char ch) {
        return ch == '"' || ch == '\\' || ch == '/' || ch == 'b' || ch == 'f' || ch == 'r' || ch == 'n' || ch == 't';
    }

    private static boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }
}

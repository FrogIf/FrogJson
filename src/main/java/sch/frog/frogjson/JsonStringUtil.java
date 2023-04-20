package sch.frog.frogjson;

import sch.frog.frogjson.json.JsonElement;
import sch.frog.frogjson.json.JsonOperator;
import sch.frog.frogjson.json.JsonParseException;

class JsonStringUtil {

    public static String toString(JsonElement jsonElement){
        String compressString = jsonElement.toCompressString();
        int len = compressString.length();
        StringBuilder sb = new StringBuilder(len);
        sb.append('\"');
        for(int i = 0; i < len; ){
            char ch = compressString.charAt(i);

            if(ch == '\\'){
                char nch = compressString.charAt(i + 1);
                if(nch == '"'){
                    sb.append("\\\\\\\"");
                    i++;
                }else{
                    sb.append("\\\\");
                }
            }else if(ch == '"'){
                sb.append("\\\"");
            }else{
                sb.append(ch);
            }
            i++;
        }
        sb.append('\"');
        return sb.toString();
    }

    // "{\n\t\"aaa\":\"a\\n\",\n\t\"ccc\":\"c\\r\",\n\t\"bbb\":\"b\\t\"\n}"
    // "{\n\t\"aaa\":\"a\t\\n\",\n\t\"ccc\":\"c\\r\",\n\t\"bbb\":\"b\\t\"\n}"
    // "{\n\t\"aaa\":\"a	\\n\",\n\t\"ccc\":\"c\\r\",\n\t\"bbb\":\"b\\t\"\n}"
    public static JsonElement fromString(String string) throws JsonParseException {
        int len = string.length();
        StringBuilder json = new StringBuilder(len);
        for(int i = 1; i < len - 1; ){
            char ch = string.charAt(i);
            if(ch == '\\'){
                if(match(string, "\\\"", i)){
                    json.append('"');
                    i += 2;
                }else if(match(string, "\\\\\\\"", i)){
                    json.append("\\\"");
                    i += 4;
                }else if(match(string, "\\\\", i)){
                    json.append('\\');
                    i += 2;
                }else if(match(string, "\\n", i) || match(string, "\\r", i)){
                    i += 2;
                }else if(match(string, "\\t", i)){
                    json.append('\t');
                    i += 2;
                }else{
                    json.append(ch);
                    i++;
                }
            }else{
                json.append(ch);
                i++;
            }
        }
        return JsonOperator.parse(json.toString());
    }

    private static boolean match(String str, String match, int start) {
        if (str.length() < match.length() + start) {
            return false;
        }
        for (int i = 0, len = match.length(); i < len; i++) {
            if (str.charAt(i + start) != match.charAt(i)) {
                return false;
            }
        }
        return true;
    }

}

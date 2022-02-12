package sch.frog.frogjson.json;

import java.util.ArrayList;
import java.util.List;

class JsonLexicalAnalyzer {

    private JsonLexicalAnalyzer() {
        // do nothing
    }

    public static List<Token> lexicalAnalysis(String json) throws JsonParseException {
        List<Token> tokens = new ArrayList<>();
        int rowIndex = 0;
        int colIndex = 0;

        for (int i = 0, len = json.length(); i < len; ) {
            char ch = json.charAt(i);
            int step = 0; // 记录本次遍历char的步长

            // parse token
            if (isWhitespace(ch)) {
                step = 1;
            } else if (ch == '{') {
                tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.OBJECT_BEGIN).setTokenType(TokenType.structure).setRowIndex(rowIndex).setColIndex(colIndex).build());
                step = 1;
            } else if (ch == '}') {
                tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.OBJECT_END).setTokenType(TokenType.structure).setRowIndex(rowIndex).setColIndex(colIndex).build());
                step = 1;
            } else if (ch == '[') {
                tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.ARRAY_BEGIN).setTokenType(TokenType.structure).setRowIndex(rowIndex).setColIndex(colIndex).build());
                step = 1;
            } else if (ch == ']') {
                tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.ARRAY_END).setTokenType(TokenType.structure).setRowIndex(rowIndex).setColIndex(colIndex).build());
                step = 1;
            } else if (ch == ':') {
                tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.COLON).setTokenType(TokenType.structure).setRowIndex(rowIndex).setColIndex(colIndex).build());
                step = 1;
            } else if (ch == ',') {
                tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.COMMA).setTokenType(TokenType.structure).setRowIndex(rowIndex).setColIndex(colIndex).build());
                step = 1;
            } else if (ch == '"') // string
            {
                RefObj<Integer> strLen = new RefObj<>();
                String str = parseString(json, i, strLen);
                if (str == null) {
                    if (i + 1 < len) {
                        triggerParseException("can't parse as string, token start with : '" + json.charAt(i + 1) + "'", rowIndex, colIndex);
                    } else {
                        triggerParseException("can't parse as string, token start with : EOF", rowIndex, colIndex);
                    }
                } else {
                    tokens.add(TokenBuilder.newBuilder().setLiteral(str).setTokenType(TokenType.t_string).setRowIndex(rowIndex).setColIndex(colIndex).build());
                    step = strLen.value;
                }
            } else if (ch == '-' || isDigit(ch)) // number
            {
                String num = parseNumber(json, i);
                if (num == null) {
                    triggerParseException("can't parse as number, token start with : '" + ch + "'", rowIndex, colIndex);
                } else {
                    tokens.add(TokenBuilder.newBuilder().setLiteral(num).setTokenType(TokenType.number).setRowIndex(rowIndex).setColIndex(colIndex).build());
                    step = num.length();
                }
            } else if (ch == 'f') // false
            {
                if (match(json, "false", i)) {
                    tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.T_FALSE).setTokenType(TokenType.t_const).setRowIndex(rowIndex).setColIndex(colIndex).build());
                    step = 5;
                } else {
                    triggerParseException("unknown token start with : 'f'", rowIndex, colIndex);
                }
            } else if (ch == 't') // true
            {
                if (match(json, JsonWord.T_TRUE, i)) {
                    tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.T_TRUE).setTokenType(TokenType.t_const).setRowIndex(rowIndex).setColIndex(colIndex).build());
                    step = 4;
                } else {
                    triggerParseException("unknown token start with : 't'", rowIndex, colIndex);
                }
            } else if (ch == 'n') // null
            {
                if (match(json, JsonWord.T_NULL, i)) {
                    tokens.add(TokenBuilder.newBuilder().setLiteral(JsonWord.T_NULL).setTokenType(TokenType.t_const).setRowIndex(rowIndex).setColIndex(colIndex).build());
                    step = 4;
                } else {
                    triggerParseException("unknown token start with : 'n'", rowIndex, colIndex);
                }
            } else {
                // error unexpect char
                triggerParseException("unknown token start with : '" + ch + "'", rowIndex, colIndex);
            }

            i += step;
            colIndex += step;

            if (isLineFeed(ch)) // 如果是换行, 刷新位置信息
            {
                rowIndex++;
                colIndex = 0;
            }
        }

        return tokens;
    }

    private static void triggerParseException(String msg, int rowIndex, int colIndex) throws JsonParseException {
        throw new JsonParseException(msg + ", row : " + (rowIndex + 1) + ", col : " + (colIndex + 1));
    }

    private static String parseString(String json, int start, RefObj<Integer> strLen) {
        StringBuilder result = new StringBuilder();
        strLen.value = 0;

        int i = start;
        int len = json.length();
        if (i >= len) {
            return null;
        }

        char ch = json.charAt(i);
        if (ch != '"') {
            return null;
        }
        i++;
        while (i < len) {
            ch = json.charAt(i);
            if (ch == '"') {
                break;
            } else if (ch == '\\') {
                i++;
                if (i >= len) {
                    return null;
                }
                ch = json.charAt(i);
                if (JsonUtil.isEscape(ch)) {
                    result.append('\\').append(ch);
                    i++;
                } else if (ch == 'u')  // 4 hex digits
                {
                    i++;
                    if (i + 4 > len) {
                        return null;
                    }
                    result.append("\\u");
                    for (int j = 0; j < 4; j++) {
                        ch = json.charAt(i + j);
                        if (JsonUtil.isHex(ch)) {
                            result.append(ch);
                        } else {
                            return null;
                        }
                    }
                    i += 4;
                } else {
                    return null;
                }
            } else {
                result.append(ch);
                i++;
            }
        }

        if (ch != '"') {
            return null;
        }

        strLen.value = i + 1 - start;

        return result.toString();
    }

    private static String parseNumber(String json, int start) {
        StringBuilder result = new StringBuilder();
        int index = start;
        int len = json.length();

        if (index >= len) {
            return null;
        }
        char ch = json.charAt(index);

        // sign
        if (ch == '-') {
            result.append('-');
            index++;
        }

        // integer part
        if (index >= len) {
            return null;
        }
        ch = json.charAt(index);

        if (!isDigit(ch)) {
            return null;
        } else if (ch == '0') {
            result.append('0');
            index++;
            ch = json.charAt(index);
            if(ch != '.' && ch != 'e' && ch != 'E'){
                if(!isDigit(ch)){
                    return result.toString();
                }else {
                    return null;
                }
            }
        } else {
            result.append(ch);
            index++;
            for (; index < len; index++) {
                ch = json.charAt(index);
                if (isDigit(ch)) {
                    result.append(ch);
                } else if (ch == '.' || ch == 'e' || ch == 'E') {
                    break;
                } else {
                    return result.toString();
                }
            }
        }

        // fraction part
        if (ch == '.') {
            result.append('.');
            index++;
            if (index >= len) {
                return null;
            }
            for (; index < len; index++) {
                ch = json.charAt(index);
                if (isDigit(ch)) {
                    result.append(ch);
                } else if (ch == 'e' || ch == 'E') {
                    break;
                } else {
                    return result.toString();
                }
            }
        }

        // scientific notation
        if (ch == 'e' || ch == 'E') {
            result.append(ch);
            index++;
            if (index >= len) {
                return null;
            }
            ch = json.charAt(index);
            if (ch == '-' || ch == '+') {
                result.append(ch);
                index++;
            }
            if (index >= len) {
                return null;
            }
            for (; index < len; index++) {
                ch = json.charAt(index);
                if (isDigit(ch)) {
                    result.append(ch);
                } else {
                    return result.toString();
                }
            }
        }

        return null;
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean match(String json, String match, int start) {
        if (json.length() < match.length() + start) {
            return false;
        }
        for (int i = 0, len = match.length(); i < len; i++) {
            if (json.charAt(i + start) != match.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    private static boolean isLineFeed(char ch) {
        return ch == '\n';
    }

    private static class TokenBuilder {

        private String literal;

        private TokenType type;

        private int rowIndex = -1;

        private int colIndex = -1;

        public static TokenBuilder newBuilder() {
            return new TokenBuilder();
        }

        public TokenBuilder setLiteral(String literal) {
            this.literal = literal;
            return this;
        }

        public TokenBuilder setTokenType(TokenType type) {
            this.type = type;
            return this;
        }

        public TokenBuilder setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
            return this;
        }

        public TokenBuilder setColIndex(int colIndex) {
            this.colIndex = colIndex;
            return this;
        }

        public Token build() {
            return new Token(this.literal, this.type, this.rowIndex, this.colIndex);
        }
    }

}

enum TokenType {
    /// <summary>
    /// 结构token, 只有以下这些: { } [ ] : ,
    /// </summary>
    structure,
    /// <summary>
    /// 字符串类型, 可以作为json的key和value
    /// </summary>
    t_string,
    /// <summary>
    /// 数字类型, 只能作为json的value
    /// </summary>
    number,
    /// <summary>
    /// 常量类型, 有以下几种: true, false, null
    /// </summary>
    t_const
}

final class Token {
    public final String literal;

    public final TokenType type;

    public final int colIndex;

    public final int rowIndex;

    public Token(String literal, TokenType type, int rowIndex, int colIndex) {
        if (literal == null) {
            throw new IllegalArgumentException("literal is null.");
        }
        if (rowIndex < 0 || colIndex < 0) {
            throw new IllegalArgumentException("row or col must be positive.");
        }
        if(type == null){
            throw new IllegalArgumentException("token type can't null.");
        }
        this.literal = literal;
        this.type = type;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    @Override
    public String toString() {
        return this.literal;
    }
}

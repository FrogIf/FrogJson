package sch.frog.frogjson.json;

public class JsonUtil {

    public static boolean isEscape(char ch) {
        return ch == '"' || ch == '\\' || ch == '/' || ch == 'b' || ch == 'f' || ch == 'r' || ch == 'n' || ch == 't';
    }

    public static boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    public static String escapeJsonString(String str) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = str.length();
        char ch;
        while (i < len) {
            ch = str.charAt(i);
            if (ch == '"') {
                throw new IllegalArgumentException("format not right at : " + i + ", str : " + str);
            } else if (ch == '\\') {
                i++;
                if (i >= len) {
                    throw new IllegalArgumentException("format not right at end, str : " + str);
                }
                ch = str.charAt(i);
                if (isEscape(ch)) {
                    result.append(ch);
                    i++;
                } else if (ch == 'u')  // 4 hex digits
                {
                    i++;
                    if (i + 4 > len) {
                        return null;
                    }
                    result.append("\\u");
                    StringBuilder hexStr = new StringBuilder(4);
                    for (int j = 0; j < 4; j++) {
                        ch = str.charAt(i + j);
                        if (isHex(ch)) {
                            hexStr.append(ch);
                        } else {
                            throw new IllegalArgumentException("format not right at " + (i + j) + ", str : " + str);
                        }
                    }
                    int hexNum = Integer.parseInt(hexStr.toString(), 16);
                    result.append((char) hexNum);
                    i += 4;
                } else {
                    return null;
                }
            } else {
                result.append(ch);
                i++;
            }
        }

        return result.toString();
    }

}

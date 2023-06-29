package sch.frog.frogjson;

import sch.frog.frogjson.json.JsonElement;
import sch.frog.frogjson.json.JsonOperator;
import sch.frog.frogjson.util.FileUtil;

public class JsonTest {

    public static void main(String[] args) throws Exception {
//        String origin = "{\"aaa\na\": \"3\na\ts\ba\fa\ra\"}";
//        String origin = "{\"aaa\\na\":\"3\\na\\ts\\ba\\fa\\ra\"}";
        String origin = FileUtil.readFromFile("win/stringjson.json");
//        System.out.println(origin);
        JsonElement element = JsonOperator.parse(origin);
        System.out.println(element.toPrettyString());
//        HashMap<String, Object> param = new HashMap<>();
//        param.put("aaa\na", "3\na\ts\ba\fa\ra\"\'\\");
//        String json = JsonOperator.load(param).toCompressString();
//        System.out.println(json);
    }



}

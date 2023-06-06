package sch.frog.frogjson.util;

import org.yaml.snakeyaml.Yaml;
import sch.frog.frogjson.json.JsonElement;
import sch.frog.frogjson.json.JsonOperator;

import java.util.ArrayList;
import java.util.List;

public class JsonYamlUtils {

    public static List<String> yaml2Json(String yml){
        Yaml yaml = new Yaml();
        Iterable<Object> objects = yaml.loadAll(yml);
        ArrayList<String> result = new ArrayList<>();
        for (Object object : objects) {
            if(object != null){
                JsonElement json = JsonOperator.load(object);
                result.add(json.toPrettyString());
            }
        }
        return result;
    }

    public static String json2Yaml(String json){
        Yaml yaml = new Yaml();
        Object load = yaml.load(json);
        return yaml.dump(load);
    }

}

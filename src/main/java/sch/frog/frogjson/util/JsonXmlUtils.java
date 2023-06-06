package sch.frog.frogjson.util;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import sch.frog.frogjson.json.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonXmlUtils {

    public static String json2Xml(String json){
        try {
            JsonElement jsonObj = JsonOperator.parse(json);
            if(jsonObj instanceof JsonObject){
                Document doc = DocumentHelper.createDocument();
                JsonObject jo = (JsonObject) jsonObj;
                List<String> keys = jo.keys();
                if(keys.size() != 1){
                    throw new IllegalArgumentException("json format incorrect, xml if and only if one root node");
                }
                String key = keys.get(0);
                if(key.startsWith("@")){
                    throw new IllegalArgumentException("json format incorrect, root node name can't start with '@'");
                }
                Element element = doc.addElement(key);
                Object o = jo.get(key);
                if(o instanceof JsonObject){
                    json2Dom4j((JsonObject) o, element);
                }else if(o instanceof JsonArray){
                    json2Dom4J(key, (JsonArray) o, element);
                }else{
                    Element e = element.addElement(key);
                    e.setText(o == null ? "" : o.toString());
                }

                OutputFormat prettyPrint = OutputFormat.createPrettyPrint();
                prettyPrint.setEncoding(StandardCharsets.UTF_8.name());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    XMLWriter xmlWriter = new XMLWriter(outputStream, prettyPrint);
                    xmlWriter.write(doc);
                    xmlWriter.flush();
                    xmlWriter.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return outputStream.toString();
            }else{
                throw new IllegalArgumentException("json format is incorrect, must object type");
            }
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static void json2Dom4j(JsonObject jsonObject, Element element){
        List<String> keys = jsonObject.keys();
        for (String key : keys) {
            Object o = jsonObject.get(key);
            if(o instanceof JsonArray){
                json2Dom4J(key, (JsonArray) o, element);
            }else if(o instanceof JsonObject){
                Element e = element.addElement(key);
                json2Dom4j((JsonObject) o, e);
            }else{
                String val = o == null ? "" : o.toString();
                if(key.startsWith("@")){
                    element.addAttribute(key.substring(1), val);
                }else{
                    Element e = element.addElement(key);
                    e.setText(o == null ? "" : o.toString());
                }
            }
        }
    }

    private static void json2Dom4J(String key, JsonArray jsonArray, Element element){
        for (Object o : jsonArray) {
            if(o instanceof JsonObject){
                Element e = element.addElement(key);
                json2Dom4j((JsonObject) o, e);
            }else {
                throw new IllegalArgumentException("json can't convert to xml, format is incorrect");
            }
        }
    }

    public static String xml2Json(String xml) {
        if(StringUtils.isBlank(xml)){
            throw new IllegalArgumentException("xml is blank");
        }
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        Element rootElement = doc.getRootElement();
        JsonObject jsonObject = new JsonObject();
        dom4j2Json(rootElement, jsonObject);
        if(!rootElement.elements().isEmpty()){
            JsonObject obj = new JsonObject();
            obj.put(rootElement.getName(), jsonObject);
            jsonObject = obj;
        }
        return jsonObject.toPrettyString();
    }

    private static void dom4j2Json(Element element, JsonObject json){
        for (Attribute attr : element.attributes()) {
            if(StringUtils.isNotBlank(attr.getValue())){
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> elements = element.elements();
        if(elements.isEmpty()){
            json.put(element.getName(), element.getText());
        }else{
            for (Element ele : elements) {
                List<Element> cList = ele.elements();
                String name = ele.getName();
                if(cList.isEmpty()){    // 说明这个ele是一个属性
                    for (Attribute attr : ele.attributes()) {
                        if(StringUtils.isNotBlank(attr.getValue())){
                            json.put("@" + attr.getName(), attr.getValue());
                        }
                    }
                    json.put(name, ele.getText());
                }else{
                    JsonObject o = new JsonObject();
                    dom4j2Json(ele, o);
                    Object v = json.get(name);
                    if(v == null){
                        json.put(name, o);
                    }else{
                        if(v instanceof JsonArray){
                            ((JsonArray) v).add(o);
                        }else if(v instanceof JsonObject){
                            JsonArray a = new JsonArray();
                            a.add(o);
                            a.add(v);
                            json.put(name, a);
                        }
                    }
                }
            }
        }
    }

}

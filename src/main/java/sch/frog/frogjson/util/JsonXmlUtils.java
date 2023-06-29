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
                if(key.startsWith("@@")){
                    if(key.equals("@@text")){
                        element.setText(val);
                    }else if(key.startsWith("@@xmlns")){
                        element.addNamespace(key.substring("@@xmlns:".length()), val);
                    }
                } else if(key.startsWith("@")){
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
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        Element rootElement = doc.getRootElement();
        JsonObject jsonObject = dom4j2Json(rootElement);
        JsonObject result = new JsonObject();
        if(jsonObject.keys().size() == 1 && jsonObject.containsKey("@@text")){
            result.put(rootElement.getQualifiedName(), jsonObject.get("@@text"));
        }else{
            result.put(rootElement.getQualifiedName(), jsonObject);
        }
        return result.toPrettyString();
    }

    private static JsonObject dom4j2Json(Element element){
        JsonObject obj = new JsonObject();
        List<Namespace> namespaces = element.declaredNamespaces();
        if(namespaces != null && !namespaces.isEmpty()){
            for (Namespace namespace : namespaces) {
                obj.put("@@xmlns:" + namespace.getPrefix(), namespace.getStringValue());
            }
        }
        for (Attribute attr : element.attributes()) {
            obj.put("@" + attr.getQualifiedName(), attr.getValue());
        }
        List<Element> elements = element.elements();
        if(elements.isEmpty() && StringUtils.isNotBlank(element.getText())){
            obj.put("@@text", element.getText());
        }else{
            for (Element ele : elements) {
                String name = ele.getQualifiedName();
                JsonObject subObj = dom4j2Json(ele);
                Object o = obj.get(name);
                if(o == null){
                    if(subObj.keys().size() == 1 && subObj.containsKey("@@text")){
                        obj.put(name, subObj.get("@@text"));
                    }else{
                        obj.put(name, subObj);
                    }
                }else if(o instanceof JsonArray){
                    ((JsonArray) o).add(subObj);
                }else{
                    JsonArray a = new JsonArray();
                    a.add(o);
                    a.add(subObj);
                    obj.put(name, a);
                }
            }
        }
        return obj;
    }

}

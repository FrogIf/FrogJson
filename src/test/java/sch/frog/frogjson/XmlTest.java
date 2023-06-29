package sch.frog.frogjson;

import sch.frog.frogjson.util.FileUtil;
import sch.frog.frogjson.util.JsonXmlUtils;

public class XmlTest {

    public static void main(String[] args) throws Exception {
        String json = JsonXmlUtils.xml2Json(FileUtil.readFromFile("win/xmltest2.xml"));
        System.out.println(json);
    }

}

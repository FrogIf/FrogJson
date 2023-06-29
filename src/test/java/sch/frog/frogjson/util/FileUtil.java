package sch.frog.frogjson.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

    public static String readFromFile(String fileName) throws Exception {
        InputStream stream = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while((line = r.readLine()) != null){
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

}

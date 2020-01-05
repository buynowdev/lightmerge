package cn.zynworld.lightmerge.common;

import java.io.*;

/**
 * @author zhaoyuening
 */
public class FileUtils {
    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        StringBuilder sbf = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sbf.toString();
    }
}

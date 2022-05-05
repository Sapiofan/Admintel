package Services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class FileHandler {

    private final String engPath = "src/main/resources/obscenities.txt";
    private final String ruPath = "src/main/resources/rusobs.txt";

    public String readEnglishObs(){
        return readFile(engPath);
    }

    public String readRussianObs(){
        return readFile(ruPath);
    }

    public String readFile(String path) {
        String temp, text = "";
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((temp = br.readLine()) != null){
                text += temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}

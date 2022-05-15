package Services;

import java.io.*;

public class FileHandler {

    private static final String engPath = "src/main/resources/obscenities.txt";
    private static final String ruPath = "src/main/resources/rusobs.txt";
    private static final String addedWords = "src/main/resources/additional.txt";

    public String readEnglishObs(){
        return readFile(engPath);
    }

    public String readRussianObs(){
        return readFile(ruPath);
    }

    public String readAdditional(){
        return readFile(addedWords);
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

    public void addBadWordToTxt(String badWord){
        String temp;
        boolean flag = false;
        try(BufferedReader reader = new BufferedReader(new FileReader(addedWords))) {
            while ((temp = reader.readLine()) != null){
                if(temp.contains(badWord)){
                    flag = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!flag){
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(addedWords, true))) {
                writer.append(badWord).append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

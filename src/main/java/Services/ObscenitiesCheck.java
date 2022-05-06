package Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class ObscenitiesCheck {

    private static final Logger log = LoggerFactory.getLogger("log");

    public boolean obscenitiesCheck(String message){
        FileHandler fileHandler = new FileHandler();
        String engText = fileHandler.readEnglishObs();
        String rusText = fileHandler.readRussianObs();
        String additional = fileHandler.readAdditional();
        String[] eng = engText.split(",");
        for (String s : eng) {
            if(message.toLowerCase().contains(s)){
                return true;
            }
        }
        String[] ru = rusText.split(",");
        for (String s : ru) {
            if(message.toLowerCase().contains(s)){
                return true;
            }
        }
        String[] add = additional.split(",");
        for (String s : add) {
            if(message.toLowerCase().contains(s)){
                return true;
            }
        }
        return false;
    }
}

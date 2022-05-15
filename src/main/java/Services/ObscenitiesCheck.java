package Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ObscenitiesCheck {

    private static final Logger log = LoggerFactory.getLogger("log");

    private static final Pattern reg = Pattern.compile("(\\s+|^)[пПnрРp]?[3ЗзВBвПnпрРpPАaAаОoO0о]?" +
            "[сСcCиИuUОoO0оАaAаыЫуУyтТT]?[Ппn][иИuUeEеЕ][зЗ3][ДдDd]\\w*[\\?\\,\\.\\;\\-]*|(\\s+|^)[рРpPпПn]?" +
            "[рРpPоОoO0аАaAзЗ3]?[оОoO0иИuUаАaAcCсСзЗ3тТTуУy]?" +
            "[XxХх][уУy][йЙеЕeEeяЯ9юЮ]\\w*[\\?\\,\\.\\;\\-]*|" +
            "(\\s+|^)[бпПnБ6][лЛ][яЯ9]([дтДТDT]\\w*)?[\\?\\,\\.\\;\\-]*|(\\s+|^)(([зЗоОoO03]?" +
            "[аАaAтТT]?[ъЪ]?)|(\\w+[оОOo0еЕeE]))?[еЕeEиИuUёЁ][бБ6пП]([аАaAиИuUуУy]\\w*)?[\\?\\,\\.\\;\\-]*");

    public boolean obscenitiesCheck(String message) {
        FileHandler fileHandler = new FileHandler();
        String engText = fileHandler.readEnglishObs();
        String rusText = fileHandler.readRussianObs();
        String additional = fileHandler.readAdditional();
        String[] eng = engText.split(",");
        for (String s : eng) {
            if (message.toLowerCase().contains(s)) {
                return true;
            }
        }
        String[] ru = rusText.split(",");
        for (String s : ru) {
            if (message.toLowerCase().contains(s)) {
                return true;
            }
        }
        String[] add = additional.split(",");
        for (String s : add) {
            if (message.toLowerCase().contains(s)) {
                return true;
            }
        }

        return reg.matcher(message).find();
    }
}

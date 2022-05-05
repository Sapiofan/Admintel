package Services;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarningHandler {
    public int dateDifference(String csvDate, String now){
        try {
            Date now1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(now);
            Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(csvDate);
            long diff = now1.getTime() - date.getTime();
            int days = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            return days;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public String convertDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public void cleanWarnings(String channel){
        CSVHandler csvHandler = new CSVHandler();
        csvHandler.checkFile(channel);
//        try(CSVReader csvReader = new CSVReader(new FileReader(channel + USERS))) {
//            List<String[]> rows = csvReader.readAll();
//        }
    }

//    public int CalculateWarnings(int difference, int count){
//        if(difference >= 30 && count == 3){
//            count = 2;
//        }
//        else if(difference >= 7 && count == 2) {
//            count = 1;
//        }
//        return count;
//    }
}

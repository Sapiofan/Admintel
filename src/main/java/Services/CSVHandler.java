package Services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.List;

public class CSVHandler {
    private final static String USERS = "users.csv";

    private static final Logger log = LoggerFactory.getLogger("log");

    public int giveWarn(String username, String channel){
        checkFile(channel);
        int count = 0;
        WarningHandler warningHandler = new WarningHandler();
        String date = warningHandler.convertDate();
        try(CSVReader csvReader = new CSVReader(new FileReader(channel + USERS))) {
            List<String[]> rows = csvReader.readAll();
            boolean flag = false;
            for (String[] row : rows) {
                if(row[0].equals(username)){
                    count = Integer.parseInt(row[1]) + 1;
                    if(count == 3){
                        row[2] = "banned";
                    }
                    flag = true;
                    int difference = warningHandler.dateDifference(row[3], date);
//                    row[1] = String.valueOf(warningHandler.CalculateWarnings(difference, count));
                    break;
                }
            }
            if(!flag){
                String[] row = {username, "1", "not banned", date};
                count = 1;
                rows.add(row);
            }
            FileWriter fileWriter = new FileWriter(channel + USERS);
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(rows);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public void checkFile(String channel){
        if(new File(channel + USERS).exists()){
            log.info("File already exists.");
        }
        else{
            String[] row = {"Usernames", "Warns", "Last warning", "Is banned"};
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(channel + USERS);
                CSVWriter writer = new CSVWriter(fileWriter);
                writer.writeNext(row);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.warn("File was created: " + channel + USERS);
        }
    }
}

package Services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVHandler {
    private final static String USERS = "users.csv";

    private static final Logger log = LoggerFactory.getLogger("log");

    public int giveWarn(User user, String channel){
        checkFile(channel);
        int count = 0;
        WarningHandler warningHandler = new WarningHandler();
        String date = warningHandler.convertDate();
        try(CSVReader csvReader = new CSVReader(new FileReader(channel + USERS))) {
            List<String[]> rows = csvReader.readAll();
            boolean flag = false;
            for (int i = 1; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(user.getUserName())) {
                    count = Integer.parseInt(rows.get(i)[1]) + 1;
                    if (count == 3) {
                        rows.get(i)[3] = "banned";
                        rows.get(i)[1] = "3";
                    }
                    else {
                        rows.get(i)[1] = String.valueOf(count);
                    }
                    flag = true;
                    break;
                }
            }
            if(!flag){
                String[] row = {user.getUserName(), "1", date, "not banned", user.getId().toString()};
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
            String[] row = {"Usernames", "Warns", "Last warning", "Is banned", "User id"};
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

    public void cleanWarnings(){
        List<String> files = null;
        try {
            files = findFiles(Paths.get(""), "users.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        WarningHandler warningHandler = new WarningHandler();
        String date = warningHandler.convertDate();
        for (String file : files) {
            try(CSVReader csvReader = new CSVReader(new FileReader(file))) {
                List<String[]> rows = csvReader.readAll();
                for (int i = 1; i < rows.size(); i++) {
                    int diff = warningHandler.dateDifference(rows.get(i)[2], date);
                    if(diff >= 30 && rows.get(i)[1].equals("2")){
                        rows.get(i)[1] = "0";
                        rows.get(i)[2] = date;
                    }
                    else if(diff >= 7 && rows.get(i)[1].equals("1")){
                        rows.get(i)[1] = "0";
                        rows.get(i)[2] = date;
                    }
                }
                FileWriter fileWriter = new FileWriter(file);
                CSVWriter writer = new CSVWriter(fileWriter);
                writer.writeAll(rows);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.warn("Warnings was cleaned");
    }

    private List<String> findFiles(Path path, String fileExtension)
            throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }

        return result;
    }
}

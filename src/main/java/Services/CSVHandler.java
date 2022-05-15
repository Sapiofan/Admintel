package Services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVHandler {
    public final static String USERS = "users.csv";

    private final static String BAD_WORDS = "badwords.csv";

    private final static String POLLS = "polls.csv";

    private static final Logger log = LoggerFactory.getLogger("log");

    public void writeUser(Chat chat, User user){
        checkFile(chat.getTitle());
        WarningHandler warningHandler = new WarningHandler();
        String date = warningHandler.convertDate();
        try(CSVReader csvReader = new CSVReader(new FileReader(chat.getTitle() + USERS))) {
            List<String[]> rows = csvReader.readAll();
            boolean flag = false;
            for (int i = 1; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(user.getUserName())) {
                    flag = true;
                    break;
                }
            }
            if(!flag){
                String[] row = {user.getUserName(), "0", date, "not banned", user.getId().toString()};
                rows.add(row);
            }
            writeToCSV(rows, chat.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            writeToCSV(rows, channel);
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
            String[] row = {"Usernames", "Warns", "Last change", "Is banned", "User id"};
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

    public void cleanPolls(){
        String[] firstRow = {"Close date", "Usernames", "Channel id", "Channel"};
        checkCSV(POLLS, firstRow);
        try(CSVReader csvReader = new CSVReader(new FileReader(POLLS))) {
            List<String[]> rows = csvReader.readAll();
            rows.removeIf(row -> Long.parseLong(row[0]) <= (System.currentTimeMillis() / 1000));
            FileWriter fileWriter = new FileWriter(POLLS);
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(rows);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> getBannedUnbanned(String channel, String param){
        checkFile(channel);
        Map<String, Long> users = new HashMap<>();
        try(CSVReader csvReader = new CSVReader(new FileReader(channel + USERS))) {
            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows) {
                if(row[3].equals(param)){
                    users.put(row[0],Long.valueOf(row[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void unbannedAndAcquitUsers(List<String> names, String channel){
        checkFile(channel);
        WarningHandler warningHandler = new WarningHandler();
        String date = warningHandler.convertDate();
        try(CSVReader csvReader = new CSVReader(new FileReader(channel + USERS))) {
            List<String[]> rows = csvReader.readAll();
            for (int i = 1; i < rows.size(); i++) {
                for (String name : names) {
                    if (rows.get(i)[0].equals(name)) {
                        rows.get(i)[3] = "not banned";
                        rows.get(i)[1] = "0";
                        rows.get(i)[2] = date;
                    }
                }
            }
            writeToCSV(rows, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bannedUsers(List<String> names, String channel){
        checkFile(channel);
        WarningHandler warningHandler = new WarningHandler();
        String date = warningHandler.convertDate();
        try(CSVReader csvReader = new CSVReader(new FileReader(channel + USERS))) {
            List<String[]> rows = csvReader.readAll();
            for (int i = 1; i < rows.size(); i++) {
                for (String name : names) {
                    if (rows.get(i)[0].equals(name)) {
                        rows.get(i)[3] = "banned";
                        rows.get(i)[2] = date;
                    }
                }
            }
            writeToCSV(rows, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listOfBadWords(Map<String, Long> bad){
        String[] firstRow = {"Bad word", "List of id", "Quantity"};
        checkCSV(BAD_WORDS, firstRow);
        try(CSVReader csvReader = new CSVReader(new FileReader(BAD_WORDS))) {
            List<String[]> rows = csvReader.readAll();
            for (String s : bad.keySet()) {
                boolean flag = false;
                for (int i = 1; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (row[0].equals(s.toLowerCase()) && !row[1].contains(String.valueOf(bad.get(s)))) {
                        int value = Integer.valueOf(row[2]) + 1;
                        if (value == 20) {
                            FileHandler fileHandler = new FileHandler();
                            fileHandler.addBadWordToTxt(row[0]);
                            rows.remove(i);
                            flag = true;
                            break;
                        }
                        row[2] = String.valueOf(value);
                        row[1] += bad.get(s) + ",";
                    } else if (row[0].equals(s.toLowerCase()) && row[1].contains(String.valueOf(bad.get(s)))) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    String[] row = {s.toLowerCase(), bad.get(s) + ",", "1"};
                    rows.add(row);
                }
            }
            FileWriter fileWriter = new FileWriter(BAD_WORDS);
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(rows);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPoll(Integer closeDate, String names, String channelId, String channel){
        String[] firstRow = {"Close date", "Usernames", "Channel id", "Channel"};
        checkCSV(POLLS, firstRow);
        try(CSVReader csvReader = new CSVReader(new FileReader(POLLS))) {
            List<String[]> rows = csvReader.readAll();
            String[] row = {String.valueOf(closeDate), names, channelId, channel};
            rows.add(row);
            FileWriter fileWriter = new FileWriter(POLLS);
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(rows);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getPolls(){
        String[] firstRow = {"Close date", "Usernames", "Channel id", "Channel"};
        checkCSV(POLLS, firstRow);
        try(CSVReader csvReader = new CSVReader(new FileReader(POLLS))) {
            return csvReader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Long> getUsersByNickname(String[] usernames, String channel){
        List<Long> ids = new ArrayList<>();
        checkFile(channel);
        try(CSVReader csvReader = new CSVReader(new FileReader(channel+USERS))) {
            List<String[]> rows = csvReader.readAll();
            for (String username : usernames) {
                for (String[] row : rows) {
                    if (username.equals(row[0]) && !row[3].equals("banned")) {
                        ids.add(Long.valueOf(row[4]));
                        row[3] = "banned";
                        break;
                    }
                }
            }
            FileWriter fileWriter = new FileWriter(channel+USERS);
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(rows);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
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

    private void writeToCSV(List<String[]> rows, String channel) throws IOException {
        FileWriter fileWriter = new FileWriter(channel + USERS);
        CSVWriter writer = new CSVWriter(fileWriter);
        writer.writeAll(rows);
        writer.close();
    }

    private void checkCSV(String path, String[] firstRow){
        if(new File(path).exists()){
            log.info("File already exists.");
        }
        else{
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(path);
                CSVWriter writer = new CSVWriter(fileWriter);
                writer.writeNext(firstRow);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.warn("File was created: " + path);
        }
    }
}

import Services.CSVHandler;
import Services.ObscenitiesCheck;
import Services.WarningHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.io.File;
import java.util.*;

import static Services.CSVHandler.USERS;

public class Bot extends TelegramLongPollingBot {

    private final String BOT_NAME;
    private final String BOT_TOKEN;

    private static final Logger log = LoggerFactory.getLogger("log");

    private CSVHandler csvHandler = new CSVHandler();

    private Timer timer = new Timer();

    public Bot(String bot_name, String bot_token) {
        super();
        this.BOT_NAME = bot_name;
        this.BOT_TOKEN = bot_token;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String command = update.getMessage().getText();
        User user = update.getMessage().getFrom();
        String channel = update.getMessage().getChat().getTitle();

        String name = user.getUserName();
        log.info(name);
        log.info(channel);

        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(update.getMessage().getChatId().toString());

        if(!update.getMessage().getChat().isUserChat()){
            checkMessage(command, name, channel, chatId, user, message);
        }
        String[] words = command.split(" ");
        if(update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat()) {
            if (words[0].equals("/ban")) {
                Map<String, Long> notBannedUsers = csvHandler.getBannedUnbanned(channel, "not banned");
                List<String> names = new ArrayList<>();
                for (int i = 1; i < words.length; i++) {
                    if (words[i].charAt(0) == '@') {
                        BanChatMember banChatMember = new BanChatMember(String.valueOf(chatId), notBannedUsers.get(words[i]));
                        try {
                            execute(banChatMember);
                            names.add(words[i].substring(1));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
                csvHandler.bannedUsers(names, channel);
                String m = "Such users was baned: ";
                for (String s : names) {
                    m += "@" + s + " ";
                }
                message.setText(m);
                try {
                    execute(message);
                    log.info(m);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (words[0].equals("/unban")) {
                Map<String, Long> bannedUsers = csvHandler.getBannedUnbanned(channel, "banned");
                List<String> names = new ArrayList<>();
                for (int i = 1; i < words.length; i++) {
                    if (words[i].charAt(0) == '@' && bannedUsers.containsKey(words[i].substring(1))) {
                        UnbanChatMember unbanChatMember = new UnbanChatMember(String.valueOf(chatId), bannedUsers.get(words[i]));
                        try {
                            execute(unbanChatMember);
                            names.add(words[i].substring(1));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
                csvHandler.unbannedAndAcquitUsers(names, channel);
                String m = "Such users was unbaned: ";
                for (String s : names) {
                    m += "@" + s + " ";
                }
                message.setText(m);
                try {
                    execute(message);
                    log.info(m);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (words[0].equals("/acquit")) {
                List<String> names = new ArrayList<>();
                for (int i = 1; i < words.length; i++) {
                    if (words[i].charAt(0) == '@') {
                        names.add(words[i].substring(1));
                    }
                }
                csvHandler.unbannedAndAcquitUsers(names, channel);

                String m = "Such users was acquitted: ";
                for (String s : names) {
                    m += "@" + s + " ";
                }
                message.setText(m);
                try {
                    execute(message);
                    log.info(m);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (words[0].equals("/status")) {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(String.valueOf(chatId));
                File file = new File(channel + USERS);
                InputFile inputFile = new InputFile(file);
                sendDocument.setDocument(inputFile);
                try {
                    execute(sendDocument);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (words[0].equals("/report")) {

            }
        }

        if(words[0].equals("/badword")){
            Map<String, Long> badWords = new HashMap<>();
            for (int i = 1; i < words.length; i++) {
                badWords.put(words[i], user.getId());
            }
            csvHandler.listOfBadWords(badWords);
        }
        else if(words[0].equals("/help")){

        }
    }

    public void checkMessage(String command, String name, String channel, Long chatId, User user, SendMessage message){
        ObscenitiesCheck obscenitiesCheck = new ObscenitiesCheck();
        if(obscenitiesCheck.obscenitiesCheck(command)){
            int count = csvHandler.giveWarn(user, channel);
            if(count == 3){
                BanChatMember banChatMember = new BanChatMember(String.valueOf(chatId), user.getId());
                banChatMember.setUntilDate(0);
                try {
                    this.execute(banChatMember);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                log.warn("Member was banned");
                message.setText("User @"+ user.getUserName() +" get last ("+count+") warning. User was banned");
            }
            else
                message.setText("Don't swear, @"+ user.getUserName() +". It's your "+ count +" warning. On 3rd one you you will be banned");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}

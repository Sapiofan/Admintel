import Services.CSVHandler;
import Services.ObscenitiesCheck;
import Services.WarningHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.util.Timer;
import java.util.TimerTask;

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

        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                WarningHandler warningHandler = new WarningHandler();
                warningHandler.cleanWarnings(channel);
            }
        },0,86400000);

        String name = user.getUserName();
        log.info(name);
        log.info(channel);

        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(update.getMessage().getChatId().toString());

        ObscenitiesCheck obscenitiesCheck = new ObscenitiesCheck();
        if(obscenitiesCheck.obscenitiesCheck(command)){
            int count = csvHandler.giveWarn(name, channel);
            if(count == 3){
                BanChatMember banChatMember = new BanChatMember(String.valueOf(chatId), user.getId());
                banChatMember.setUntilDate(0);
                try {
                    this.execute(banChatMember);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                log.warn("Member was banned");
            }
            else
                message.setText("Don't swear. It's your "+ count +" warning. After 3rd one you you will be banned");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if(command.equals("/test")){
            message.setText("Bot is working");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}

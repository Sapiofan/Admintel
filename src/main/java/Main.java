import Services.CSVHandler;
import Services.WarningHandler;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            Dotenv dotenv = Dotenv.load();
            api.registerBot(new Bot(dotenv.get("BOT_NAME"), dotenv.get("BOT_TOKEN")));
            new Timer().scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    CSVHandler csvHandler = new CSVHandler();
                    csvHandler.cleanWarnings();
                }
            },0,86400000);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}

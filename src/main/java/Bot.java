import Services.CSVHandler;
import Services.ObscenitiesCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

import static Services.CSVHandler.USERS;
import static Services.Help.helpCommand;

public class Bot extends TelegramLongPollingBot {

    private final String BOT_NAME;
    private final String BOT_TOKEN;

    private static final Logger log = LoggerFactory.getLogger("log");

    private final CSVHandler csvHandler = new CSVHandler();

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
        try {
            if (!handlePoll(update)) {
                log.info("actions with poll was successfully ended");
                return;
            }
        } catch (TelegramApiException e) {
            log.error("something went wrong with poll");
            e.printStackTrace();
        }
        if (update.getChatJoinRequest() != null) {
            User joinedUser = update.getChatJoinRequest().getUser();
            Chat chat1 = update.getChatJoinRequest().getChat();
            log.info("New user " + joinedUser.getUserName() + " joined to group: " + chat1.getTitle());
            csvHandler.writeUser(chat1, joinedUser);
            return;
        }
        String command = update.getMessage().getText();
        User user = update.getMessage().getFrom();
        Chat channel = update.getMessage().getChat();

        String name = user.getUserName();
        log.info(name);
        log.info(channel.getTitle());
        log.info("" + channel.getId());
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(update.getMessage().getChatId().toString());
        if (!update.getMessage().getChat().isUserChat()) {
            csvHandler.writeUser(channel, user);
            checkMessage(command, channel.getTitle(), chatId, user, message);
        }
        String[] words = command.split(" ");
        if (update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat()) {
            switch (words[0]) {
                case "/ban" -> {
                    Map<String, Long> notBannedUsers = csvHandler.getBannedUnbanned(channel.getTitle(), "not banned");
                    List<String> names = new ArrayList<>();
                    for (int i = 1; i < words.length; i++) {
                        if (words[i].charAt(0) == '@') {
                            BanChatMember banChatMember = new BanChatMember(String.valueOf(chatId), notBannedUsers.get(words[i]));
                            try {
                                execute(banChatMember);
                                names.add(words[i].substring(1));
                            } catch (TelegramApiException e) {
                                log.error("bot can't ban a user");
                                e.printStackTrace();
                            }
                        }
                    }
                    csvHandler.bannedUsers(names, channel.getTitle());
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
                }
                case "/unban" -> {
                    Map<String, Long> bannedUsers = csvHandler.getBannedUnbanned(channel.getTitle(), "banned");
                    List<String> names = new ArrayList<>();
                    for (int i = 1; i < words.length; i++) {
                        if (words[i].charAt(0) == '@' && bannedUsers.containsKey(words[i].substring(1))) {
                            UnbanChatMember unbanChatMember = new UnbanChatMember(String.valueOf(chatId), bannedUsers.get(words[i]));
                            try {
                                execute(unbanChatMember);
                                names.add(words[i].substring(1));
                            } catch (TelegramApiException e) {
                                log.error("bot can't unban users.");
                                e.printStackTrace();
                            }
                        }
                    }
                    csvHandler.unbannedAndAcquitUsers(names, channel.getTitle());
                    String m = "Such users was unbaned: ";
                    for (String s : names) {
                        m += "@" + s + " ";
                    }
                    message.setText(m);
                    try {
                        execute(message);
                        log.info(m);
                    } catch (TelegramApiException e) {
                        log.error("something went wrong when tried to send message: " + message.getText());
                        e.printStackTrace();
                    }
                }
                case "/acquit" -> {
                    List<String> names = getMessageUsernames(words);
                    csvHandler.unbannedAndAcquitUsers(names, channel.getTitle());

                    String m = "Such users was acquitted: ";
                    for (String s : names) {
                        m += "@" + s + " ";
                    }
                    message.setText(m);
                    try {
                        execute(message);
                        log.info(m);
                    } catch (TelegramApiException e) {
                        log.error("something went wrong when tried to send message: " + message.getText());
                        e.printStackTrace();
                    }
                }
                case "/status" -> {
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
                }
                case "/report" -> {
                    List<String> options = new ArrayList<>();
                    options.add("yes");
                    options.add("no");
                    SendPoll sendPoll = new SendPoll();
                    sendPoll.setChatId(String.valueOf(chatId));
                    Long time = System.currentTimeMillis() / 1000L;
                    sendPoll.setCloseDate(time.intValue() + 86400);
                    log.info(sendPoll.getCloseDate() + "");
                    sendPoll.setIsAnonymous(true);
                    sendPoll.setOptions(options);
                    String forFile = "", m = "Do you want to ban user(s) on 3 days? ";
                    for (String messageUsername : getMessageUsernames(words)) {
                        m += "@" + messageUsername + " ";
                        forFile += messageUsername + ",";
                    }
                    sendPoll.setQuestion(m);
                    try {
                        execute(sendPoll);
                        csvHandler.addPoll(sendPoll.getCloseDate(), forFile, String.valueOf(channel.getId()), channel.getTitle());
                    } catch (TelegramApiException e) {
                        log.error("can't send poll");
                        e.printStackTrace();
                    }
                }
            }
        }

        if (words[0].equals("/badword")) {
            Map<String, Long> badWords = new HashMap<>();
            for (int i = 1; i < words.length; i++) {
                badWords.put(words[i], user.getId());
            }
            csvHandler.listOfBadWords(badWords);
        } else if (words[0].equals("/help")) {
            message.setText(helpCommand(user));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("something went wrong when tried to send message: " + message.getText());
                e.printStackTrace();
            }
        }
    }

    public void checkMessage(String command, String channel, Long chatId, User user, SendMessage message) {
        ObscenitiesCheck obscenitiesCheck = new ObscenitiesCheck();
        if (obscenitiesCheck.obscenitiesCheck(command)) {
            int count = csvHandler.giveWarn(user, channel);
            if (count == 3) {
                BanChatMember banChatMember = new BanChatMember(String.valueOf(chatId), user.getId());
                banChatMember.setUntilDate(0);
                try {
                    this.execute(banChatMember);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                log.warn("Member was banned");
                message.setText("User @" + user.getUserName() + " get last (" + count + ") warning. User was banned");
            } else
                message.setText("Don't swear, @" + user.getUserName() + ". It's your " + count + " warning. On 3rd one you you will be banned");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("something went wrong when tried to send message: " + message.getText());
                e.printStackTrace();
            }
        }
    }

    private List<String> getMessageUsernames(String[] words) {
        List<String> names = new ArrayList<>();
        for (int i = 1; i < words.length; i++) {
            if (words[i].charAt(0) == '@') {
                names.add(words[i].substring(1));
            }
        }
        return names;
    }

    private boolean handlePoll(Update update) throws TelegramApiException {
        Poll poll = update.getPoll();
        if (poll == null) {
            return true;
        }
        List<String[]> polls = csvHandler.getPolls();
        List<PollOption> options = poll.getOptions();
        GetChatMemberCount memberCount = new GetChatMemberCount(getChatId(polls, poll.getQuestion()));
        Integer count;
        count = execute(memberCount);
        count -= 1;
        if (count <= 30 && options.get(0).getVoterCount() >= count * 0.7) {
            banUsers(poll, polls);
        } else if (count <= 100 && options.get(0).getVoterCount() >= count * 0.5) {
            banUsers(poll, polls);
        } else if (count > 100 && options.get(0).getVoterCount() >= count * 0.1) {
            banUsers(poll, polls);
        }
        return false;
    }

    private void banUsers(Poll poll, List<String[]> polls) throws TelegramApiException {
        String usernames = getUsernames(poll.getQuestion());
        for (String[] strings : polls) {
            boolean flag = false;
            String[] usernamesFromFile = strings[1].split(",");
            for (String s : usernamesFromFile) {
                if (!usernames.contains(s)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                List<Long> usersId = csvHandler.getUsersByNickname(usernamesFromFile, strings[3]);
                for (Long userId : usersId) {
                    BanChatMember banChatMember = new BanChatMember(strings[2], userId);
                    execute(banChatMember);
                }
            }
        }
    }

    private String getUsernames(String message) {
        String[] words = message.split(" ");
        String names = "";
        for (String word : words) {
            if (word.charAt(0) == '@') {
                names += word.substring(1) + ",";
            }
        }
        return names;
    }

    private String getChatId(List<String[]> polls, String question) {
        String[] usernames = getUsernames(question).split(",");
        String chatId = "";
        for (String[] poll : polls) {
            boolean flag = false;
            for (int i = 0; i < usernames.length; i++) {
                if (!poll[1].contains(usernames[i])) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                chatId = poll[2];
                break;
            }
        }
        log.info("chatId: " + chatId);
        return chatId;
    }
}

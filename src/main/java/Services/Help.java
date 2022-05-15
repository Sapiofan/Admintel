package Services;

import org.telegram.telegrambots.meta.api.objects.User;

public class Help {

    public static String helpCommand(User user){
        String explanation = "Hey @"+user.getUserName()+". This bot was created to help admins of groups keep order. " +
                "It automatically detects any obscenities in chat and warn users when they write bad words. " +
                "On 3rd warn user will be banned. But the bot is loyal when user doesn't break rules some time and remove warns" +
                "(7 days - 1 warn, 30 days - 2 warn).\n\n" +
                "Bot commands:\n\n" +
                "/ban @Someone @AnotherUser\n" +
                "Bans user(s) in chat (if user(s) wrote something in it).\n\n" +
                "/unban @Someone @AnotherUser\n" +
                "Unbans user(s) in chat.\n\n" +
                "/acquit @Someone @AnotherUser\n" +
                "If user has 1 or 2 warns, owner or admin can acquit the violator(s) and \"give\" him 0 warns.\n\n" +
                "/status\n" +
                "User will get file of all users of chat with warns.\n\n" +
                "/badword word anotherword\n" +
                "Unfortunately, bad words filter is not excellent, because tricky users always find out " +
                "new ways to get around system and write what they actually want. But this command help to " +
                "prevent such situations. Just type this command and copy obscenity (it's important to copy) and " +
                "send to bot(it can be done in personal char with the bot). If many people will send such word, " +
                "system will get that such word is bad and begin to give warns.\n\n" +
                "/report @Somebody @AnotherUser\n" +
                "If someone was annoying many people, someone can begin poll via this command and if needed % of people " +
                "votes \"yes\", reported users will be banned on 3 days.\n\n" +
                "% for approving the poll:\n" +
                "up to 30 people -> 70%\n" +
                "30-100 -> 50%\n" +
                "100+ -> 10%\n\n" +
                "/help\n" +
                "See bot commands";
        return explanation;
    }
}

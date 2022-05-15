package Services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateHandler {
    public int dateDifference(String csvDate, String now) {
        try {
            Date now1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(now);
            Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(csvDate);
            long diff = now1.getTime() - date.getTime();
            return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String convertDate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }
}

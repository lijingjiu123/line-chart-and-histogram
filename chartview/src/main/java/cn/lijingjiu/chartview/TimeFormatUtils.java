package cn.lijingjiu.chartview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lijingjiu123 on 2017/3/22.
 */
public class TimeFormatUtils {
    public  static SimpleDateFormat eventFormat = new SimpleDateFormat("dd.MM.yyyy");
    public  static SimpleDateFormat ptsHistoryFormat = new SimpleDateFormat("yyyyMMdd");

    public static String getPtsTime(String time){
        try {
            Date date = ptsHistoryFormat.parse(time);
            return eventFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

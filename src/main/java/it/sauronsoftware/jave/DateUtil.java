package it.sauronsoftware.jave;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    //格林威治时间转换
    public static Date UTCGMT2Date(String date){
        //设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //将输入时间转换为ms
        Calendar cal = Calendar.getInstance();
        try {
            sdf.parse(date).getTime();
            cal.setTimeInMillis(sdf.parse(date).getTime());
        }catch (ParseException e) {
            e.printStackTrace();
        }
        cal.add(Calendar.HOUR, +8);
        return cal.getTime();
    }
}

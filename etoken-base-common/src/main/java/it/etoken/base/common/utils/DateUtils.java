package it.etoken.base.common.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	public static String forSecond(long time){
		SimpleDateFormat df = new SimpleDateFormat("hh:mm");
		return df.format(new Date(time));
	}
	
	public static String forDay(long time){
		SimpleDateFormat df = new SimpleDateFormat("MM-dd");
		return df.format(new Date(time));
	}
	
	/**
	 * 10位int型的时间戳转换为String(MM-dd)
	 * @param time
	 * @return
	 */
	public static String timestampToStringForDay(long time){
		//int转long时，先进行转型再进行计算，否则会是计算结束后在转型
		long temp = (long)time*1000;
		Timestamp ts = new Timestamp(temp);  
        String tsStr = "";  
        DateFormat dateFormat = new SimpleDateFormat("MM-dd");  
        try {  
            //方法一  
            tsStr = dateFormat.format(ts);  
            System.out.println(tsStr);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
		return tsStr;  
	}
	
	
	/**
	 * 10位的时间戳转换为String(hh:mm)
	 * @param time
	 * @return
	 */
	public static String timestampToStringForSecond(long time){
		//int转long时，先进行转型再进行计算，否则会是计算结束后在转型
		long temp = (long)time*1000;
		Timestamp ts = new Timestamp(temp);  
        String tsStr = "";  
        DateFormat dateFormat = new SimpleDateFormat("hh:mm");  
        try {  
            //方法一  
            tsStr = dateFormat.format(ts);  
            System.out.println(tsStr);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
		return tsStr;  
	}
	
	//获取utc时间戳
	public static long getUtcTimes() {
		Calendar cal = Calendar.getInstance();
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        long mills = cal.getTimeInMillis();
		return mills;
	}
	
	//获取utc时间戳
	public static Date getUtcDate() {
		Calendar cal = Calendar.getInstance();
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        long mills = cal.getTimeInMillis();
        return new Date(mills);
	}
	
	public static Date formateDate(String date) {
		  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
		  format.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		  try {
			Date d = format.parse(date );
			return d;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String formateDate(Date date) {
		  SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
		  sd.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		  try {
			String d = sd.format(date);
			return d;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

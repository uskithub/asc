package jp.ascendia.Taschel.jspUtil;

import java.util.Calendar;
import java.util.Date;

public class DisplayUtils {

	 /**
     * 文字列がnullのとき、ハイフンに置き換えます。
     */
    public static String display(Long tmp) {
    	if ( tmp == null){
    		return "－"; 
    	} else {
    		return tmp.toString(); 
    	}
    }
    
    /**
     *  期限を表示用に整形して返します。
     */
    public static String diplayDeadline(Date deadline) {

        // nullの場合、以降処理しない
        if (deadline == null) {
            return "－";
        }

        // 今日の日付を取得
        Calendar t = Calendar.getInstance();
        // 表示をテストするときに使う（※MONTHは0から数えるので注意）
        //t.set(2013,10,19,12,0);

        // 期限をCalendar型へ変換
        Calendar d = Calendar.getInstance();
        d.setTime(deadline);
        
        int dYear = d.get(Calendar.YEAR);
        int tYear = t.get(Calendar.YEAR);
        
        // 期限が過ぎていないか
        if ( t.getTimeInMillis() <= d.getTimeInMillis() ){               
        	if ( 1 < dYear-tYear) {
                return String.format("%d年後", dYear-tYear);
            } else {                   
                int dMonth = d.get(Calendar.MONTH);
                int tMonth = t.get(Calendar.MONTH);
                if ( 1 == dYear-tYear && tMonth <= dMonth) {                        
                    return "1年後";
                } else {
                    if ( 1 < dMonth+(dYear-tYear)*12-tMonth) {
                        return String.format("%dヶ月後", dMonth+12-tMonth);
                    } else {
                        int dDay = d.get(Calendar.DAY_OF_MONTH);
                        int tDay = t.get(Calendar.DAY_OF_MONTH);
                        if ( 1 == dMonth-tMonth && tDay <= dDay) {                        
                            return "1ヶ月後";
                        } else {
                            Calendar c = Calendar.getInstance();
                            c.set(tYear, tMonth, 1);                            
                            int days=c.getActualMaximum(Calendar.DAY_OF_MONTH);
                            
                            if ( 1 < dDay+(dMonth-tMonth)*days - tDay) {
                                return String.format("%d日後", dDay+(dMonth-tMonth)*days-tDay);
                            } else {                                                                
                                int dHour = d.get(Calendar.HOUR_OF_DAY);
                                int tHour = t.get(Calendar.HOUR_OF_DAY);
                                int dMinute = d.get(Calendar.MINUTE);
                                if ( 1 == dDay-tDay ){
                                	if ( tHour <= dHour) { 
                                	    return "1日後";
                                	} else {                                		
                                        return String.format("翌　%d:%02d", dHour, dMinute);
                                	}
                                } else {                                	
                                    return String.format("%d:%02d", dHour, dMinute);
                                }                            
                            }
                        }       
                    }
                }               
            }          	
        } else {
        	if ( 1 < tYear-dYear) {
                return String.format("<span class=\"warn\">%d年経過</span>", tYear-dYear);
            } else {                   
            	int dMonth = d.get(Calendar.MONTH);
                int tMonth = t.get(Calendar.MONTH);
                if ( 1 == tYear-dYear && dMonth <= tMonth) {                        
                    return "<span class=\"warn\">1年経過</span>";
                } else {                	
                	if ( 1 < tMonth+(tYear-dYear)*12-dMonth) {
                        return String.format("<span class=\"warn\">%dヶ月経過</span>", tMonth+12-dMonth);
                    } else {
                    	int dDay = d.get(Calendar.DAY_OF_MONTH);
                        int tDay = t.get(Calendar.DAY_OF_MONTH);
                    	if ( 1 == tMonth-dMonth && dDay <= tDay) {                        
                            return "<span class=\"warn\">1ヶ月経過</span>";
                        } else {
                        	Calendar c = Calendar.getInstance();
                        	c.set(dYear, dMonth, 1);
                        	int days=c.getActualMaximum(Calendar.DAY_OF_MONTH);
                        	
                            if ( 1 < tDay+(tMonth-dMonth)*days - dDay) {
                                return String.format("<span class=\"warn\">%d日経過</span>", tDay+(tMonth-dMonth)*days-dDay);
                            } else {                            	                            	
                            	int dHour = d.get(Calendar.HOUR_OF_DAY);
                                int tHour = t.get(Calendar.HOUR_OF_DAY);
                                int dMinute = d.get(Calendar.MINUTE);
                                if ( 1 == tDay-dDay ){
                                    if ( dHour <= tHour) { 
                                        return "<span class=\"warn\">1日経過</span>";
                                    } else {                                        
                                        return String.format("<span class=\"warn\">昨日　%d:%02d</span>", dHour, dMinute);
                                    }
                                } else {                                    
                                    return String.format("<span class=\"warn\">%d:%02d</span>", dHour, dMinute);
                                }                             
                            }
                        }   	
                    }
                }               
            } 
        }
    }    
}

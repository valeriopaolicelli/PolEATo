package com.mad.poleato;


/**
 * This class is used in the AboutUs.class to search for the most popular times
 */
public class TimeSlot {
    private String start;
    private String end;

    public TimeSlot(String start, String end){
        this.start= start;
        this.end= end;
    }

    public boolean inSlot(String time){
        return time.compareTo(start) >= 0 && time.compareTo(end) <= 0;
    }

    public String getSlot(){
        return start+" - "+end;
    }
}

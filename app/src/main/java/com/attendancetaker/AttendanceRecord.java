package com.attendancetaker;

import java.sql.Date;
import java.sql.Time;

/**
 * A class to encalsulate data from a database record, for used in history view
 */
public class AttendanceRecord {
    public int course_number;
    public int course_section;
    public Date date;
    public Time time;
    public String status;
}

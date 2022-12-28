package com.attendancetaker;

import static java.lang.Thread.sleep;

import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * A class to deal A asynchronous tasks and communicating with the database, querying and updating
 */
public class Database {
    private static Connection _conn = null; // This is our database connection, ever open one at a time
    private static Integer _currentUserAccountId = -1; // student.account_id from a successful login or -1 if null

    public static Connection getConnection() { return _conn; }
    public static int getActiveUser() {return _currentUserAccountId; }
    public static void logoutActiveUser() { _currentUserAccountId = -1; }

    /**
     * Wrapper function to start the task to connect to database
     */
    protected static void openConnection() {
        new Database.Connect().execute();
    }

    /**
     * Wrapper function to close the database connection and handle errors
     */
    protected static void closeConnection() {
        try {
            Database.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wrapper function to stall a worker thread until the connection is established
     */
    protected static void waitForConnection() {
        while(Database.getConnection() == null) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * An asynchronous task used to connect to mariaDB and open a connection
     */
    public static class Connect extends AsyncTask<String, String, String> {
        String url = "jdbc:mariadb://173.76.23.95:3306/attendance-taker?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
        String user = "AndroidClient";
        String pass = "password";

        @Override
        protected String doInBackground(String... strings) {
            try {
                _conn = DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }

            return "Database connected successfully...";
        }
    }

    /**
     *  These Asynchronous tasks use classes to encapsulate networking on a worker thread
     *  other than the main thread.
     */
    public static class Register extends AsyncTask<String, String, String> {
        @Override protected void onPreExecute() { openConnection(); }
        @Override protected void onPostExecute(String result) { closeConnection(); }

        /**
         * Worker thread method
         * @param strings
         *      [0] wit id
         *      [1] email
         *      [2] password
         *      [3] first name
         *      [4] last name
         *      [5] password confirmation
         * @return Registration status
         */
        @Override protected String doInBackground(String... strings) {
            // some input validation
            if(!strings[0].matches("^[W][0-9]{8}$"))
                return "Invalid WIT ID!";
            if(!strings[1].matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
                return "Invalid Email!";
            if(strings[2].length() < 5)
                return "Passcode must be at least 4 digits!";
            if(!strings[2].equals(strings[5]))
                return "Passcodes do not match!";
            if(strings[3].length() == 0 || strings[4].length() == 0)
                return "Fist and Last Name must be filled out.";

            String query = "INSERT INTO student (`wit_id`, `email`, `password`, `fname`, `lname`) VALUES (?, ?, ?, ?, ?)";

            // Wait for our connection then start our statement
            waitForConnection();
            try (PreparedStatement pstmt = _conn.prepareStatement(query)) {
                // Set our params
                for(int i = 0; i < 5; i++) {
                   pstmt.setString(i + 1,strings[i]);
                }
                pstmt.executeUpdate(); // run the update statement
                return "success";
            } catch (SQLException e) {
                // Handle errors
                String error_msg = e.getLocalizedMessage();
                if(error_msg != null && error_msg.matches("(.*wit_id_UNIQUE.*)")) {
                    error_msg = "WIT ID already used!";
                }
                else if(error_msg != null && error_msg.matches("(.*email_UNIQUE.*)")) {
                    error_msg = "Email already used!";
                }
                return error_msg;
            }
        }
    }
    public static class Login extends AsyncTask<String, String, Integer> {
        @Override protected void onPreExecute() { openConnection(); }
        @Override protected void onPostExecute(Integer result) { closeConnection(); }

        /**
         * Worker thread method
         * @param strings
         *      [0] username
         *      [1] password
         * @return Associated account id if successful or -1 if failure occurs.
         */
        @Override protected Integer doInBackground(String... strings) {
            // Prepares query string
            String query = "SELECT `account_id` FROM `student` WHERE `email` = ? AND `password` = ?";
            // Wait for our connection then start our statement
            waitForConnection();
            try (PreparedStatement pstmt = _conn.prepareStatement(query)) {
                // execute login query
                pstmt.setString(1, strings[0]);
                pstmt.setInt(2, Integer.parseInt(strings[1]));
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                // Set our newly logged in user
                _currentUserAccountId = rs.getInt("account_id");

                return _currentUserAccountId;
            } catch (SQLException e) {
                // If we have a failure make sure we dont log anyone in
                System.out.println(e.getLocalizedMessage());
                return -1;
            }
        }
    }
    public static class CheckIn extends AsyncTask<Integer, String, String> {
        @Override protected void onPreExecute() { openConnection(); }
        @Override protected void onPostExecute(String result) { closeConnection(); }

        /**
         * Worker thread method
         * @param class_id [0] int value associated with class id in the database
         * @return Check in result
         */
        @Override protected String doInBackground(Integer... class_id) {
            //preparing string
            String insert = "INSERT INTO attendance_record (`class_id`, `student_id`, `date`, `MAC`, `lat`, `long`) VALUES " +
                    "(?, ?, NOW(), \"00:00:00:00:00:00\", 0, 0)";
            // Wait for our connection then start our statement
            waitForConnection();
            try (PreparedStatement pstmt = _conn.prepareStatement(insert)) {
                // execute insert
                pstmt.setInt(1, class_id[0]);
                pstmt.setInt(2, Database.getActiveUser());
                pstmt.executeUpdate();
                return "success";
            } catch (SQLException e) {
                // Handle errors
                String error_msg = e.getLocalizedMessage();
                if(error_msg != null && error_msg.matches("^.*value count at row 1$")) {
                    error_msg = "You have already check into this class...";
                }
                else if(error_msg != null && error_msg.matches("^.*'course_id' cannot be null$")) {
                    error_msg = "Invalid Class Code";
                }
                return error_msg;
            }
        }
    }
    public static class UpdateHistory extends AsyncTask<String, String, Vector<AttendanceRecord>> {
        @Override protected void onPreExecute() { openConnection(); }
        @Override protected void onPostExecute(Vector<AttendanceRecord> result) { closeConnection(); }

        /**
         * Worker thread method
         * @return List of objects containing information about a given attendance record
         */
        @Override protected Vector<AttendanceRecord> doInBackground(String... strings) {
            // Create a place to store our new cards,  make sure it is of variable size
            Vector<AttendanceRecord> cards = new Vector<>();
            String query = "SELECT `number`, `section`,`date`, `status` " +
                    "FROM attendance_record, class, course " +
                    "WHERE attendance_record.class_id = class.class_id AND " +
                    "course.course_id = class.course_id AND " +
                    "attendance_record.student_id = ?";
            // Wait for our connection then start our statement
            waitForConnection();
            try (PreparedStatement pstmt = _conn.prepareStatement(query)) {
                // execute login query
                pstmt.setInt(1, Database.getActiveUser());
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    AttendanceRecord record = new AttendanceRecord();
                    record.course_number = rs.getInt("number");
                    record.course_section = rs.getInt("section");;
                    record.date = rs.getDate("date");
                    record.time = rs.getTime("date");
                    record.status = rs.getString("status");
                    System.out.println("Record Found...");
                    cards.add(record);
                }
            } catch (SQLException e) {
                System.out.println(e.getLocalizedMessage());
            }
            return cards;
        }
    }

    // Make sure if some how we didn't close connection, that we do when we destroy object
    protected void finalize() {
        try {
            _conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
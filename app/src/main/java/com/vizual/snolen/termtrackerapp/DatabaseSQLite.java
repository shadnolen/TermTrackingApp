package com.vizual.snolen.termtrackerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseSQLite extends SQLiteOpenHelper {

    // Properties
    private static final String DATABASE_NAME = "termtrackingapp.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_TERMS = "terms";
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_ASSESSMENTS = "assessments";
    private static final String TABLE_COURSE_NOTES = "courseNotes";
    private static final String TABLE_ASSESSMENT_NOTES = "assessmentNotes";

    //  Columns term:
    private static final String TERMS_ID = "id";
    private static final String TERMS_NAME = "name";
    private static final String TERMS_STARTDATE = "startdate";
    private static final String TERMS_ENDDATE = "enddate";

    // courses:
    private static final String COURSES_ID = "id";
    private static final String COURSES_TERM_ID = "termId";
    private static final String COURSES_NAME = "name";
    private static final String COURSES_DESCRIPTION = "description";
    private static final String COURSES_STARTDATE = "startdate";
    private static final String COURSES_ENDDATE = "enddate";
    private static final String COURSES_MENTORNAME = "mentorname";
    private static final String COURSES_MENTORPHONE = "mentorphone";
    private static final String COURSES_MENTOREMAIL = "mentoremail";
    private static final String COURSES_STATUS = "status";

    // assessments:
    private static final String ASSESSMENTS_ID = "id";
    private static final String ASSESSMENTS_NAME = "name";
    private static final String ASSESSMENTS_COURSE_ID = "courseId";
    private static final String ASSESSMENTS_CODE = "code";
    private static final String ASSESSMENTS_DESCRIPTION = "description";
    private static final String ASSESSMENTS_TYPE = "type";
    private static final String ASSESSMENTS_DATE = "date";
    private static final String ASSESSMENTS_STATUS = "status";

    // Notes courses:
    private static final String COURSE_NOTES_ID = "id";
    private static final String COURSE_NOTES_COURSE_ID = "courseId";
    private static final String COURSE_NOTES_TEXT = "text";
    private static final String COURSE_NOTES_CREATED = "created";

    // assessments:
    private static final String ASSESSMENT_NOTES_ID = "id";
    private static final String ASSESSMENT_NOTES_COURSE_ID = "assessmentId";
    private static final String ASSESSMENT_NOTES_TEXT = "text";
    private static final String ASSESSMENT_NOTES_CREATED = "created";

    //Table Strings
    private static final String TERMS_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_TERMS + " (" +
                    TERMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TERMS_NAME + " TEXT, " +
                    TERMS_STARTDATE + " DATE, " +
                    TERMS_ENDDATE + " DATE, " +
                    "CONSTRAINT constraint_name UNIQUE (" + TERMS_NAME + ")" +
                    ")";

    private static final String COURSES_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_COURSES + " (" +
                    COURSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSES_NAME + " TEXT, " +
                    COURSES_TERM_ID + " INTEGER, " +
                    COURSES_DESCRIPTION + " TEXT, " +
                    COURSES_STARTDATE + " DATE, " +
                    COURSES_ENDDATE + " DATE, " +
                    COURSES_MENTORNAME + " TEXT, " +
                    COURSES_MENTORPHONE + " TEXT, " +
                    COURSES_MENTOREMAIL + " TEXT," +
                    COURSES_STATUS + " TEXT, " +
                    "CONSTRAINT fk_terms FOREIGN KEY (" + COURSES_TERM_ID + ")" +
                    " REFERENCES " + TABLE_TERMS + "(" + TERMS_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    private static final String ASSESSMENTS_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_ASSESSMENTS + " (" +
                    ASSESSMENTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENTS_COURSE_ID + " INTEGER, " +
                    ASSESSMENTS_CODE + " TEXT, " +
                    ASSESSMENTS_NAME + " TEXT, " +
                    ASSESSMENTS_DESCRIPTION + " TEXT, " +
                    ASSESSMENTS_TYPE + " TEXT, " +
                    ASSESSMENTS_DATE + " DATE, " +
                    ASSESSMENTS_STATUS + " TEXT, " +
                    "CONSTRAINT fk_courses " +
                    "FOREIGN KEY(" + ASSESSMENTS_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COURSES_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    private static final String COURSE_NOTES_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_COURSE_NOTES + " (" +
                    COURSE_NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_NOTES_COURSE_ID + " INTEGER, " +
                    COURSE_NOTES_TEXT + " TEXT, " +
                    COURSE_NOTES_CREATED + "TEXT default CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_courses_2 " +
                    "FOREIGN KEY(" + COURSE_NOTES_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COURSES_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    private static final String ASSESSMENT_NOTES_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_ASSESSMENT_NOTES + " (" +
                    ASSESSMENT_NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENT_NOTES_COURSE_ID + " INTEGER, " +
                    ASSESSMENT_NOTES_TEXT + " TEXT, " +
                    ASSESSMENT_NOTES_CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_assessments " +
                    "FOREIGN KEY(" + ASSESSMENT_NOTES_CREATED + ") REFERENCES " + TABLE_ASSESSMENTS + "(" + ASSESSMENTS_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    // Database constructor:
    public DatabaseSQLite(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // methods:
    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL(TERMS_TABLE_CREATE);
        database.execSQL(COURSES_TABLE_CREATE);
        database.execSQL(ASSESSMENTS_TABLE_CREATE);
        database.execSQL(COURSE_NOTES_TABLE_CREATE);
        database.execSQL(ASSESSMENT_NOTES_TABLE_CREATE);
    }

    // Ensure Cascade Deletion
    public void onOpen(SQLiteDatabase database){
        database.execSQL("PRAGMA foreign_keys=ON");
    }

    // Upgrade Method if Using Old School
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENT_NOTES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE_NOTES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENTS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
    }

    // Term insert:
    public boolean insertTermData(String name, long start, long end){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(TERMS_NAME, name);
        contentValues.put(TERMS_STARTDATE, start);
        contentValues.put(TERMS_ENDDATE, end);
        long result = database.insert(TABLE_TERMS, null, contentValues);

        if (result == -1)
            return false;
        else
            return true;
    }


    // retrieve all:
    public Cursor getAllTerms(){
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_TERMS +
                                        " ORDER BY " + TERMS_STARTDATE , null);
        return cursor;
    }

    //retrieve one by id:
    public Cursor getTerm(String termID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TERMS +
                                " WHERE " + TERMS_ID + " = " + termID;

        Cursor cursor = database.rawQuery(selectQuery, null);

        return cursor;
    }

    //delete row:
    public void deleteTerm(String termID){
        SQLiteDatabase database = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + TABLE_TERMS +
                                " WHERE " + TERMS_ID + " = " + termID;
        database.execSQL(selectQuery);
    }

    // names for spinner & unique check:
    public List<String> getTermNames(){

        //get names:
        Cursor cursor = getAllTerms();

        //create and populate list
        List<String> termNames = new ArrayList<>();
        while(cursor.moveToNext()){
            termNames.add(cursor.getString(cursor.getColumnIndex("name")));
        }
        cursor.close();

        //return list:
        return termNames;
    }

    public String getTermName(String id){
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_TERMS +
                                        " WHERE " + TERMS_ID + " = '" + id + "'", null);

        cursor.moveToFirst();
        String name = cursor.getString(1);
        cursor.close();
        return name;
    }

    // term id from term name:
    public int getTermId(String name){
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_TERMS +
                                        " WHERE " + TERMS_NAME + " = '" + name + "'", null);

        cursor.moveToFirst();
        int id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    //  table empty?
    public boolean isTermsEmpty(){

        boolean empty = true;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_TERMS, null);
        if (cursor != null && cursor.moveToFirst()){
            empty = (cursor.getInt(0) == 0);
        }

        cursor.close();

        return empty;
    }

    // has course?
    public boolean ifTermHasCourses(String termID){
        boolean empty = true;
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_COURSES +
                                " WHERE " + COURSES_TERM_ID + " = " + termID;

        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()){
            empty = (cursor.getInt(0) == 0);
        }

        return empty;
    }

    // start & end:
    public List<Long> getTermDates(String termName){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TERMS +
                                " WHERE " + TERMS_NAME + " = '" + termName + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);

        List<Long> termDates = new ArrayList<>();
        cursor.moveToFirst();
        termDates.add(cursor.getLong(2));
        termDates.add(cursor.getLong(3));

        return termDates;
    }

    // END TERM //

    // Course get all:
    public Cursor getAllCourses(){
        SQLiteDatabase database = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES;

        Cursor cursor = database.rawQuery(selectQuery, null);

        return cursor;
    }

    // course names:
    public List<String> getCourseNames(){
        // names from database:
        Cursor cursor = getAllCourses();

        //create and populate list:
        List<String> courseNames = new ArrayList<>();
        while(cursor.moveToNext()){
            courseNames.add(cursor.getString(cursor.getColumnIndex("name")));
        }
        cursor.close();

        //return the list:
        return courseNames;
    }

    // one by id:
    public Cursor getCourse(String courseID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES +
                " WHERE " + COURSES_ID + " = " + courseID;

        Cursor cursor = database.rawQuery(selectQuery, null);

        return cursor;
    }

    // name by id:
    public String getCourseName(String courseID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES +
                " WHERE " + COURSES_ID + " = " + courseID;

        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    // id from name:
    public int getCourseID(String name){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT " + COURSES_ID + " FROM " + TABLE_COURSES +
                                " WHERE " + COURSES_NAME + " = " + name;


        String[] columnsToReturn = {COURSES_ID};
        String selection = COURSES_NAME + " = ?";
        String[] selectionArgs = {name};
        Cursor cursor = database.query(TABLE_COURSES, columnsToReturn, selection, selectionArgs, null, null, null);


        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    //retrieve all from term:
    public Cursor getCoursesFromTerm(String termID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES +
                                " WHERE " + COURSES_TERM_ID + " = " + termID +
                                " ORDER BY " + COURSES_STARTDATE;

        Cursor cursor = database.rawQuery(selectQuery, null);
        return cursor;
    }

    public List<Long> getCourseDates(String courseName){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT " + COURSES_STARTDATE + ", " + COURSES_ENDDATE + " FROM " + TABLE_COURSES +
                " WHERE " + COURSES_NAME + " = '" + courseName + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);

        List<Long> courseDates = new ArrayList<>();
        cursor.moveToFirst();
        courseDates.add(cursor.getLong(0));
        courseDates.add(cursor.getLong(1));

        return courseDates;
    }

    // insert:
    public boolean insertCourseData(String name, String term, String description, long start, long end,
                                    String status, String menName, String menPhone, String menMail){

        // get term id otherwisen  put a zero
        int termId;

        if (term == "none"){
            termId = 0;
        } else {
            termId = getTermId(term);
        }

        // Content Values
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COURSES_NAME, name);
        contentValues.put(COURSES_TERM_ID, termId);
        contentValues.put(COURSES_DESCRIPTION, description);
        contentValues.put(COURSES_STARTDATE, start);
        contentValues.put(COURSES_ENDDATE, end);
        contentValues.put(COURSES_MENTORNAME, menName);
        contentValues.put(COURSES_MENTORPHONE, menPhone);
        contentValues.put(COURSES_MENTOREMAIL, menMail);
        contentValues.put(COURSES_STATUS, status);
        long result = database.insert(TABLE_COURSES, null, contentValues);

        if (result == -1)
            return false;
        else
            return true;
    }

    //delete:
    public void deleteCourse(String courseID){
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_COURSES + " WHERE " + COURSES_ID + " = " + courseID;
        database.execSQL(query);
    }

    //update:
    public boolean updateCourse(String courseID, String name, int term, String description,
                             long numStart, long numEnd, String status, String menName,
                             String menPhone, String menMail){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COURSES_NAME, name);
        contentValues.put(COURSES_TERM_ID, term);
        contentValues.put(COURSES_DESCRIPTION, description);
        contentValues.put(COURSES_STARTDATE, numStart);
        contentValues.put(COURSES_ENDDATE, numEnd);
        contentValues.put(COURSES_STATUS, status);
        contentValues.put(COURSES_MENTORNAME, menName);
        contentValues.put(COURSES_MENTORPHONE, menPhone);
        contentValues.put(COURSES_MENTOREMAIL, menMail);
        String where = COURSES_ID + "=?";
        String[] whereArgs = new String[] {String.valueOf(courseID)};

        long result = database.update(TABLE_COURSES, contentValues, where, whereArgs);

        if (result == -1)
            return false;
        else
            return true;
    }

    // empty?
    public boolean isCoursesEmpty(){
        boolean empty = true;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_COURSES, null);
        if (cursor != null && cursor.moveToFirst()){
            empty = (cursor.getInt(0) == 0);
        }

        cursor.close();

        return empty;
    }
       // END COURSES //

    // DRY METHOD INVOKED FOR ASSESSMENTS COMMENTS  ITS THE SAME AS ABOVE
    public Cursor getAllAssessments(){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSESSMENTS +
                                " GROUP BY " + ASSESSMENTS_COURSE_ID +
                                " ORDER BY " + ASSESSMENTS_DATE;

        return database.rawQuery(selectQuery, null);
    }


    public Cursor getAssessmentsForOneCourse(String courseID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSESSMENTS +
                                " WHERE " + ASSESSMENTS_COURSE_ID + " = " + courseID;

        return database.rawQuery(selectQuery, null);
    }


    public List<String> getAssessmentNames(){

        Cursor cursor = getAllAssessments();


        List<String> assessmentNames = new ArrayList<>();
        while(cursor.moveToNext()){
            assessmentNames.add(cursor.getString(cursor.getColumnIndex("name")));
        }
        cursor.close();

        return assessmentNames;
    }

    // DRY METHOD INVOKED FOR ASSESSMENTS COMMENTS  ITS THE SAME AS ABOVE
    public Cursor getAssessmentData(String assessmentID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSESSMENTS + " WHERE " + ASSESSMENTS_ID + " = " + assessmentID;
        return database.rawQuery(selectQuery, null);
    }

    // DRY METHOD INVOKED FOR ASSESSMENTS COMMENTS  ITS THE SAME AS ABOVE
    public boolean insertAssessment(String name, String code, String description, int course, String type,
                            String status, long dateDUE){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ASSESSMENTS_NAME, name);
        contentValues.put(ASSESSMENTS_CODE, code);
        contentValues.put(ASSESSMENTS_DESCRIPTION, description);
        contentValues.put(ASSESSMENTS_COURSE_ID, course);
        contentValues.put(ASSESSMENTS_TYPE, type);
        contentValues.put(ASSESSMENTS_DATE, dateDUE);
        contentValues.put(ASSESSMENTS_STATUS, status);

        long result = database.insert(TABLE_ASSESSMENTS, null, contentValues);

        return result != -1;
    }

    // DRY METHOD INVOKED FOR ASSESSMENTS COMMENTS  ITS THE SAME AS ABOVE
    public boolean updateAssessment(String id, String name, String code, String description, int course, String type,
                                 String status, long dateDUE){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ASSESSMENTS_NAME, name);
        contentValues.put(ASSESSMENTS_CODE, code);
        contentValues.put(ASSESSMENTS_DESCRIPTION, description);
        contentValues.put(ASSESSMENTS_COURSE_ID, course);
        contentValues.put(ASSESSMENTS_TYPE, type);
        contentValues.put(ASSESSMENTS_DATE, dateDUE);
        contentValues.put(ASSESSMENTS_STATUS, status);

        String where = ASSESSMENTS_ID + "=?";
        String[] whereArgs = new String[] {String.valueOf(id)};

        long result = database.update(TABLE_ASSESSMENTS, contentValues, where, whereArgs);

        return result != -1;

    }

    // DRY METHOD INVOKED FOR ASSESSMENTS COMMENTS  ITS THE SAME AS ABOVE
    public boolean dateCompares(String table, long start, long end){
        SQLiteDatabase database = this.getReadableDatabase();
        boolean overlap = false;
        String tableStart, tableEnd;
        int resStartCount, resEndCount;

        if (table.equals("terms")){
            table = TABLE_TERMS;
            tableStart = TERMS_STARTDATE;
            tableEnd = TERMS_ENDDATE;
            resStartCount = 3;
            resEndCount = 4;
        } else {
            table = TABLE_COURSES;
            tableStart = COURSES_STARTDATE;
            tableEnd = COURSES_ENDDATE;
            resStartCount = 4;
            resEndCount = 5;
        }

        String selectQuery = "SELECT * FROM " + table + " WHERE " +
                                tableStart + " BETWEEN " + start + " AND " + end;

        Cursor cursor = database.rawQuery(selectQuery, null);

        // Overlap Flag
        if (cursor.getCount() != 0){
            cursor.moveToFirst();
            if (start != cursor.getLong(resStartCount)) {
                overlap = true;
            }
        }
        cursor.close();

        // DRY METHOD INVOKED FOR ASSESSMENTS COMMENTS  ITS THE SAME AS ABOVE
        if (!overlap){
            selectQuery = "SELECT * FROM " + table + " WHERE " +
                    tableEnd + " BETWEEN " + start + " AND " + end;

            cursor = database.rawQuery(selectQuery, null);

            if (cursor.getCount() != 0){
                cursor.moveToFirst();
                if (end != cursor.getLong(resEndCount)) {
                    overlap = true;
                }
            }
        }
        cursor.close();

        return overlap;
    }
   // END ASSESSMENTS

    // Create New Note
    public boolean insertCourseNote(String courseID, String noteText){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COURSE_NOTES_COURSE_ID, courseID);
        contentValues.put(COURSE_NOTES_TEXT, noteText);

        long result = database.insert(TABLE_COURSE_NOTES, null, contentValues);
        database.close();

        return result != -1;
    }

    // Delete That Note
    public void deleteCourseNote(String noteID){
        SQLiteDatabase database = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + TABLE_COURSE_NOTES + " WHERE " + COURSE_NOTES_ID + " = " + noteID;
        database.execSQL(selectQuery);
    }

    public Cursor getCourseNotes(String courseID){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSE_NOTES +
                                " WHERE " + COURSE_NOTES_COURSE_ID + " = " + courseID;

        return database.rawQuery(selectQuery, null);
    }
   // DRY ASSESSMENT
    public boolean insertAssessmentNote(String assessmentID, String noteText){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COURSE_NOTES_COURSE_ID, assessmentID);
        contentValues.put(COURSE_NOTES_TEXT, noteText);

        long result = database.insert(TABLE_ASSESSMENT_NOTES, null, contentValues);

        return result != -1;
    }

     // FIA AND Delete..........;)
    public void deleteEverything(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_COURSES);
        database.execSQL("vacuum");
        database.execSQL("DELETE FROM " + TABLE_TERMS);
        database.execSQL("vacuum");
    }
}



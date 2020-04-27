package com.vizual.snolen.termtrackerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CoursesActivity extends AppCompatActivity {

    // Did you eat your Variables today?
    DatabaseSQLite database;

    Button addCourse, saveCourse, cancelCourse;
    Calendar calendar = Calendar.getInstance();
    TableLayout courseTable;
    int dateFieldToggle;

    EditText enterName, enterDescription, mentorName,
            mentorPhone, mentorMail;
    TextView editStart, editEnd;

    ScrollView listView, entryView;
    Spinner termSpinner, statusSpinner;

    SharedPreferences sharedPrefer;
    SharedPreferences.Editor editor;

    // Table Refresh onResume
    @Override
    public void onResume(){
        super.onResume();
        populateCourses();
    }

    // Date Picker
    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Courses");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Database Connection
        database = new DatabaseSQLite(this);

        // Cast
        courseTable = (TableLayout) findViewById(R.id.courseTable);

        listView = (ScrollView) findViewById(R.id.courseListTable);
        entryView = (ScrollView) findViewById(R.id.courseEntryView);

        enterName = (EditText) findViewById(R.id.editCourseName);
        enterDescription = (EditText) findViewById(R.id.courseDescription);

        editStart = (TextView) findViewById(R.id.editStart);
        editEnd = (TextView) findViewById(R.id.editEnd);

        mentorName = (EditText) findViewById(R.id.editMentorName);
        mentorMail = (EditText) findViewById(R.id.editMentorEmail);
        mentorPhone = (EditText) findViewById(R.id.editMentorPhone);

        addCourse = (Button) findViewById(R.id.addCourseButton);
        saveCourse = (Button) findViewById(R.id.saveButton);
        cancelCourse = (Button) findViewById(R.id.cancelButton);

        termSpinner = (Spinner) findViewById(R.id.termSpinner);
        statusSpinner = (Spinner) findViewById(R.id.statusSpinner);

        sharedPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefer.edit();

        // Populate
        populateCourses();
    }

    // Database Retrieval
    public void populateCourses(){

        // Clear View
        courseTable.removeAllViews();

        // Database Data (ARRAYLIST)
        ArrayList<List> allCourses = getCourses();

        if(!allCourses.isEmpty()) {
            for (List<String> list : allCourses) {
                TableRow row = new TableRow(CoursesActivity.this);

                TextView columnID = new TextView(CoursesActivity.this);
                TextView columnName = new TextView(CoursesActivity.this);
                TextView statusCol = new TextView(CoursesActivity.this);

                Button detailBtn = new Button(CoursesActivity.this);

                columnID.setText(list.get(0));
                columnID.setMinWidth(130);
                final String idNum = columnID.getText().toString();

                columnName.setText(list.get(1));
                columnName.setMinWidth(250);
                columnName.setMaxEms(10);
                columnName.setEllipsize(TextUtils.TruncateAt.END);
                columnName.setHorizontallyScrolling(false);
                columnName.setSingleLine();

                statusCol.setText(list.get(2));
                statusCol.setMinWidth(250);

                detailBtn.setText("DETAILS");
                detailBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // I. Next Activity Pass Though
                        editor.putString("course", idNum);
                        editor.commit();

                        // II. To Details Activity
                        Intent intent = new Intent(v.getContext(), CourseDetailActivity.class);
                        v.getContext().startActivity(intent);
                    }
                });

                row.addView(columnID);
                row.addView(columnName);
                row.addView(statusCol);
                row.addView(detailBtn);

                courseTable.addView(row);
            }
        }
    }

    //onClick methods for date fields
    public void startDateSelect_CA(View v){
        DatePickerDialog datepicker = new DatePickerDialog(CoursesActivity.this, datePick,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        List<Long> termDates = database.getTermDates(termSpinner.getSelectedItem().toString());
        dateFieldToggle = 1;

        datepicker.getDatePicker().setMinDate(termDates.get(0));
        datepicker.getDatePicker().setMaxDate(termDates.get(1));
        datepicker.show();
    }

    public void endDateSelect_CA(View v){
        DatePickerDialog datepicker = new DatePickerDialog(CoursesActivity.this, datePick,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        List<Long> termDates = database.getTermDates(termSpinner.getSelectedItem().toString());
        dateFieldToggle = 2;

        datepicker.getDatePicker().setMinDate(termDates.get(0));
        datepicker.getDatePicker().setMaxDate(termDates.get(1));
        datepicker.show();
    }


    public void addCourse(View v){

        //Layout Visibility
        listView.setVisibility(View.GONE);
        addCourse.setVisibility(View.GONE);
        entryView.setVisibility(View.VISIBLE);

     // Spinner status:
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.course_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // terms:
        List<String> termSpinnerList = database.getTermNames();
        ArrayAdapter<String> termAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, termSpinnerList);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        termSpinner.setAdapter(termAdapter);
    }

    public void saveCourse(View v){
        boolean dataInvalid = false;

        String courseName = enterName.getText().toString();
        String description = enterDescription.getText().toString();

        String start = editStart.getText().toString();
        String end = editEnd.getText().toString();

        long numStart = 1;
        long numEnd = 1;

        String status = statusSpinner.getSelectedItem().toString();
        String term = termSpinner.getSelectedItem().toString();

        String menName = mentorName.getText().toString();
        String menPhone = mentorPhone.getText().toString();
        String menMail = mentorMail.getText().toString();


        // Validation Blank Course:
        if (courseName.isEmpty()){
            Toast.makeText(CoursesActivity.this, "Enter name.", Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        // unique name:
        if (!dataInvalid){
            List<String> courseNames = database.getCourseNames();
            int temp = courseNames.contains(courseName) ? 1 : 2;
            if (temp == 1){
                dataInvalid = true;
                Toast.makeText(CoursesActivity.this, "Course name already exists.", Toast.LENGTH_SHORT).show();
            }
        }

        // Date to Long
        if (!dataInvalid) {

            // Empty?
            if (start.isEmpty() || end.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(CoursesActivity.this, "Dates cannot be blank.",
                        Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = simpleDate.parse(start);
                    numStart = date.getTime();
                    date = simpleDate.parse(end);
                    numEnd = date.getTime();
                } catch (ParseException e) {
                    dataInvalid = true;
                    Toast.makeText(CoursesActivity.this, "There was a problem with your dates.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // End Date Before Start?
        if (!dataInvalid){
            boolean datesvalid = (numEnd > numStart);
            if (!datesvalid){
                Toast.makeText(CoursesActivity.this, "End date must be after start date.",
                        Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            }
        }

        //  Between Term Start & End
        if (!dataInvalid){
                List<Long> termDates = database.getTermDates(term);
                boolean datesvalid = (numStart >= termDates.get(0));

                if (!datesvalid){
                    Toast.makeText(CoursesActivity.this, "Start date outside of term.",
                            Toast.LENGTH_SHORT).show();
                    dataInvalid = true;
                } else {
                    datesvalid = (numEnd <= termDates.get(1));
                    if (!datesvalid){
                        Toast.makeText(CoursesActivity.this, "End date outside of term.",
                                Toast.LENGTH_SHORT).show();
                        dataInvalid = true;
                    }
                }
            }

        //ensure dates don't overlap with other courses
        if (!dataInvalid){
            boolean overlap = database.dateCompares("courses", numStart, numEnd);
            if (overlap){
                Toast.makeText(CoursesActivity.this, "Course dates overlap with another course.",
                        Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            }
        }

        // Save to Database
        if (!dataInvalid){
            boolean isInserted = database.insertCourseData(courseName, term, description, numStart, numEnd,
                                    status, menName, menPhone, menMail);

            if (isInserted){
                Toast.makeText(CoursesActivity.this, "Course added.",
                        Toast.LENGTH_SHORT).show();


                entryView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                addCourse.setVisibility(View.VISIBLE);

                // Repopulate
                populateCourses();
            } else {
                Toast.makeText(CoursesActivity.this, "Course not added.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cancelCourse(View v){
        entryView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        addCourse.setVisibility(View.VISIBLE);
    }

    public ArrayList<List> getCourses(){
        Cursor cursor = database.getAllCourses();
        ArrayList<List> courseList = new ArrayList<>();

        while(cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String status = cursor.getString(cursor.getColumnIndex("status"));

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);
            data.add(status);
            courseList.add(data);
        }

        return courseList;
    }

    // DatePicker
    private void updateLabel(){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat simpleDate = new SimpleDateFormat(myFormat, Locale.US);

        String newDate = simpleDate.format(calendar.getTime());

        if(dateFieldToggle == 1) {
            editStart.setText(newDate);
        }
        else
            editEnd.setText(newDate);
    }

    // Navigation Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navmenu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.termscreen:
                startActivity(new Intent(this, TermsActivity.class));
                return true;
            case R.id.coursesscreen:
                startActivity(new Intent(this, CoursesActivity.class));
                return true;
            case R.id.assessmentsscreen:
                startActivity(new Intent(this, AssessmentsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

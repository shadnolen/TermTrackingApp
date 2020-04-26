package com.vizual.snolen.termtrackerapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssessmentDetailActivity extends AppCompatActivity {

    // We Need Some Variables Right?
    DatabaseSQLite database;
    Calendar calendar = Calendar.getInstance();

    ScrollView addAssessEnterView;
    Button assessSaveButton, assessCancelButton;
    EditText addAssessHead, addAssessID, addAssessDescrip;
    TextView addDueDate;

    Spinner addSpinnerCourse, addSpinnerStatus, addSpinnerType;
    String assessmentID = null;

    SharedPreferences sharedPrefer;
    SharedPreferences.Editor editor;

    //    Let's Make Things Easier W/ DatePicker
    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateLabel(calendar);
            calendar = Calendar.getInstance();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);
        database = new DatabaseSQLite(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Assessments");

        addAssessEnterView = (ScrollView) findViewById(R.id.addAssessEnterView);
        assessSaveButton = (Button) findViewById(R.id.assessSaveButton);
        assessCancelButton = (Button) findViewById(R.id.assessCancelButton);

        addAssessHead = (EditText) findViewById(R.id.addAssessHead);
        addAssessID = (EditText) findViewById(R.id.addAssessID);
        addAssessDescrip = (EditText) findViewById(R.id.addAssessDescrip);
        addDueDate = (TextView) findViewById (R.id.addDueDate);

        addSpinnerType = (Spinner) findViewById(R.id.addSpinnerType);
        addSpinnerStatus = (Spinner) findViewById(R.id.addSpinnerStatus);
        addSpinnerCourse = (Spinner) findViewById(R.id.addSpinnerCourse);

        sharedPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefer.edit();
        assessmentID = sharedPrefer.getString("assessment", "");

        //Populate Assessment
        populateAssessmentFields(assessmentID);

    }

    // onClick Date View
    public void dateSelect_ADD(View v){
        DatePickerDialog datepicker = new DatePickerDialog(AssessmentDetailActivity.this, datePick,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        List<Long> courseDates = database.getCourseDates(addSpinnerCourse.getSelectedItem().toString());

        datepicker.getDatePicker().setMinDate(courseDates.get(0));
        datepicker.getDatePicker().setMaxDate(courseDates.get(1));
        datepicker.show();
    }

    // Fill Method, Spinners,
    public void populateAssessmentFields(String assessmentID){

        List<String> courseSpinnerList_ADD = database.getCourseNames();
        ArrayAdapter<String> courseAdapter_ADD = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, courseSpinnerList_ADD);
        courseAdapter_ADD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addSpinnerCourse.setAdapter(courseAdapter_ADD);

        // Spinner type:
        ArrayAdapter<CharSequence> typeAdapter_ADD = ArrayAdapter.createFromResource(this, R.array.assessment_type,
                android.R.layout.simple_spinner_item);
        typeAdapter_ADD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addSpinnerType.setAdapter(typeAdapter_ADD);

        // status:
        ArrayAdapter<CharSequence> statusAdapter_ADD = ArrayAdapter.createFromResource(this, R.array.assessment_status,
                android.R.layout.simple_spinner_item);
        statusAdapter_ADD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addSpinnerStatus.setAdapter(statusAdapter_ADD);

        // Get Data
        Cursor cursor = database.getAssessmentData(assessmentID);
        cursor.moveToFirst();

        // Fill Blank Data
        addAssessHead.setText(cursor.getString(3));
        addAssessID.setText(cursor.getString(2));
        addAssessDescrip.setText(cursor.getString(4));

        // Set Course and Start Spinners
        int courseSpinnerPosition = courseAdapter_ADD.getPosition(database.getCourseName(cursor.getString(1)));
        addSpinnerCourse.setSelection(courseSpinnerPosition);

        // Spinner type:
        int typeSpinnerPosition = typeAdapter_ADD.getPosition(cursor.getString(5));
        addSpinnerType.setSelection(typeSpinnerPosition);

        // status:
        int statusSpinnerPosition = statusAdapter_ADD.getPosition(cursor.getString(7));
        addSpinnerStatus.setSelection(statusSpinnerPosition);

       // Convert and Fill Date
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        addDueDate.setText(simpleDate.format(cursor.getLong(6)));

    }

     // Save Button
    public void saveAssessButton(View v){

        boolean dataInvalid = false;
        String name = addAssessHead.getText().toString();
        String code = addAssessID.getText().toString();
        String description = addAssessDescrip.getText().toString();

        String course = addSpinnerCourse.getSelectedItem().toString();
        String type = addSpinnerType.getSelectedItem().toString();
        String status = addSpinnerStatus.getSelectedItem().toString();

        String dueDateString = addDueDate.getText().toString();
        long dateNum = 1;

        // Name Blank?
        if (name.isEmpty()){
            Toast.makeText(AssessmentDetailActivity.this, "Enter name.",
                    Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        // Make Sure it's Not Empty
        if (!dataInvalid){
            if (code.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(AssessmentDetailActivity.this, "Enter code.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Convert Date to Long
        if (!dataInvalid){
            if (dueDateString.isEmpty()){
                dataInvalid = true;
                Toast.makeText(AssessmentDetailActivity.this, "Select date.",
                        Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = simpleDate.parse(dueDateString);
                    dateNum = date.getTime();
                } catch (ParseException e){
                    dataInvalid = true;
                    Toast.makeText(AssessmentDetailActivity.this, "There was a problem with your dates.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }


        // Is Date Due Valid?
        if (!dataInvalid){
            List<Long> courseDates = database.getCourseDates(course);
            boolean datesvalid = (dateNum >= courseDates.get(0));

            if(!datesvalid){
                dataInvalid = true;
                Toast.makeText(AssessmentDetailActivity.this, "Due date outside of course dates.",
                        Toast.LENGTH_SHORT).show();
            } else {
                datesvalid = (dateNum <= courseDates.get(1));
                if (!datesvalid){
                    dataInvalid = true;
                    Toast.makeText(AssessmentDetailActivity.this, "Due date outside of course dates.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Let's Use That Database Code We wrote
        if (!dataInvalid){

            // courseID
            int course_id = database.getCourseID(course);

            // Insert
            boolean isUpdated = database.updateAssessment(assessmentID, name, code, description, course_id, type, status, dateNum);

            if (isUpdated){
                Toast.makeText(AssessmentDetailActivity.this, "Assessment updated.",
                        Toast.LENGTH_SHORT).show();

                // Update when Updated
                finish();

            } else {
                Toast.makeText(AssessmentDetailActivity.this, "Assessment not updated.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //  Cancel button
    public void cancelAssessmentAdd_ADD(View v){
        finish();
    }

    // DatePicker Update
    private void updateLabel(Calendar chosenCalendar){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat simpleDate = new SimpleDateFormat(myFormat, Locale.US);
        String newDate = simpleDate.format(chosenCalendar.getTime());
        addDueDate.setText(newDate);
    }

    // Date Alert
    public void addAlert_ADD(View view) {
        //set course alerts
        String dateDUE = addDueDate.getText().toString();

        // Convert Date to Long
        if (!dateDUE.isEmpty()){
            SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy",
                    Locale.getDefault());
            try {
                Date date = simpleDate.parse(dateDUE);
                long dateNum = date.getTime();

                Intent alertIntent = new Intent(this, AlarmNotifi.class);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, dateNum, PendingIntent.getBroadcast(this, 1,
                        alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                Toast.makeText(AssessmentDetailActivity.this, "Alert set.",
                        Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {

            }
        } else {
            Toast.makeText(AssessmentDetailActivity.this, "Dates cannot be blank.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

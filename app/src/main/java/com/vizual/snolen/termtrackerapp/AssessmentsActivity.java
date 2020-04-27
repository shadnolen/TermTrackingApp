package com.vizual.snolen.termtrackerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class AssessmentsActivity extends AppCompatActivity {

    // Did you eat your Variables today?
    DatabaseSQLite database;
    Calendar calendar = Calendar.getInstance();

    TableLayout assessListTable;
    ScrollView assessListView, assessEnterView;

    Button assessSave, assessAss, assessCancel;
    EditText assessHeader, assessCodeID, assesDescrip;
    TextView dateDUE;

    Spinner spinnerCourseAssess, spinnerStatus, spinnerType;
    SharedPreferences sharedPrefer;
    SharedPreferences.Editor editor;

//    Generate our DatePicker
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
    public void onResume(){
        super.onResume();
        populateAssessments();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments);
        database = new DatabaseSQLite(this);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar)
                findViewById(R.id.toolbar);
        getSupportActionBar().setTitle("Assessments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        assessListTable = (TableLayout) findViewById(R.id.assessListTable);
        assessListView = (ScrollView) findViewById(R.id.assessListView);
        assessEnterView = (ScrollView) findViewById(R.id.assessEnterView);

        assessSave = (Button) findViewById(R.id.assessSave);
        assessAss = (Button) findViewById(R.id.addAssessmentButton_AA);
        assessCancel = (Button) findViewById(R.id.assessCancel);

        assessHeader = (EditText) findViewById(R.id.assessHeader);
        assessCodeID = (EditText) findViewById(R.id.assessCodeID);
        assesDescrip = (EditText) findViewById(R.id.assesDescrip);
        dateDUE = (TextView) findViewById (R.id.dateDUE_AA);

        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        spinnerStatus = (Spinner) findViewById(R.id.spinnerStatus);
        spinnerCourseAssess = (Spinner) findViewById(R.id.spinnerCourseAssess);

        sharedPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefer.edit();

        assessListView.setVisibility(View.VISIBLE);

        //Existing Assessments?
        populateAssessments();

        // Course -> New Assessment
        Intent intent = getIntent();
        String activity = intent.getStringExtra("activity");
        if (activity != null) {
            if (activity.equals(".CourseDetailActivity")) {
                assessAss.performClick();
                editor.remove("activity");
                editor.commit();
            }
        }

    }

    // Date View
    public void dateSelect(View v){
        DatePickerDialog datepicker = new DatePickerDialog(AssessmentsActivity.this, datePick,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        List<Long> courseDates = database.getCourseDates(spinnerCourseAssess.getSelectedItem().toString());

        datepicker.getDatePicker().setMinDate(courseDates.get(0));
        datepicker.getDatePicker().setMaxDate(courseDates.get(1));
        datepicker.show();
    }

    // Populate Assessments
    public void populateAssessments(){

        // Clear View
        assessListTable.removeAllViews();

        // Get Existing
        ArrayList<List> allAssessments = getAssessments();

        for (List<String> list : allAssessments){
            TableRow row = new TableRow(AssessmentsActivity.this);
            TextView columnID = new TextView(AssessmentsActivity.this);
            TextView columnName = new TextView(AssessmentsActivity.this);
            TextView typeCol = new TextView(AssessmentsActivity.this);
            Button detailBtn_AA = new Button(AssessmentsActivity.this);

            columnID.setText(list.get(0));
            columnID.setMinWidth(120);
            final String idNum = columnID.getText().toString();
            columnName.setText(list.get(1));
            columnName.setMinWidth(250);
            columnName.setMaxEms(10);
            columnName.setEllipsize(TextUtils.TruncateAt.END);
            columnName.setSingleLine();
            typeCol.setText(list.get(2));
            typeCol.setMinWidth(250);

            detailBtn_AA.setText("Details");
            detailBtn_AA.setPadding(5,0, 0, 0);
            detailBtn_AA.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){


                    editor.putString("assessment", idNum);
                    editor.commit();


                    Intent intent = new Intent(v.getContext(), AssessmentDetailActivity.class);
                    v.getContext().startActivity(intent);
                }
            });

            row.addView(columnID);
            row.addView(columnName);
            row.addView(typeCol);
            row.addView(detailBtn_AA);
            assessListTable.addView(row);
        }
    }

    // addAssessments Action
    public void addAssessment(View v){
        assessListView.setVisibility(View.GONE);
        assessAss.setVisibility(View.GONE);
        assessEnterView.setVisibility(View.VISIBLE);

       // More Spinner Action
        List<String> courseSpinnerList_AA = database.getCourseNames();
        ArrayAdapter<String> courseAdapter_AA = new ArrayAdapter<String>(
               this, android.R.layout.simple_spinner_item, courseSpinnerList_AA);
        courseAdapter_AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseAssess.setAdapter(courseAdapter_AA);

        //Selection <- Course Detail
        String course = sharedPrefer.getString("courseName", "");
        if (!course.isEmpty()){
            int courseSel = courseAdapter_AA.getPosition(course);
            spinnerCourseAssess.setSelection(courseSel);
            editor.remove("courseName");
            editor.commit();
        }

        // Spinner type:
        ArrayAdapter<CharSequence> typeAdapter_AA = ArrayAdapter.createFromResource(this, R.array.assessment_type,
                android.R.layout.simple_spinner_item);
        typeAdapter_AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter_AA);

        // status:
        ArrayAdapter<CharSequence> statusAdapter_AA = ArrayAdapter.createFromResource(this, R.array.assessment_status,
                android.R.layout.simple_spinner_item);
        statusAdapter_AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter_AA);
    }

    // Save Button
    public void saveAssessment_AA(View v){
        boolean dataInvalid = false;
        String name = assessHeader.getText().toString();
        String code = assessCodeID.getText().toString();
        String description = assesDescrip.getText().toString();

        String course = spinnerCourseAssess.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        String dueDateString = dateDUE.getText().toString();
        long dateNum = 1;

        //Validations
        if (name.isEmpty()){
            Toast.makeText(AssessmentsActivity.this, "Enter name.",
                    Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        // Unique?
        if (!dataInvalid){
            List<String> assessments = database.getAssessmentNames();
            int temp = assessments.contains(name) ? 1 : 2;
            if (temp == 1){
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Assessment name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Blank?
        if (!dataInvalid){
            if (code.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Enter code.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Covert to Long
        if (!dataInvalid){
            if (dueDateString.isEmpty()){
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Select date.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = simpleDate.parse(dueDateString);
                    dateNum = date.getTime();
                } catch (ParseException e){
                    dataInvalid = true;
                    Toast.makeText(AssessmentsActivity.this, "There was a problem with your dates.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Due Date <- Course Date
        if (!dataInvalid){
            List<Long> courseDates = database.getCourseDates(course);
            boolean datesvalid = (dateNum >= courseDates.get(0));

            if(!datesvalid){
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Due date outside of course dates.",
                        Toast.LENGTH_SHORT).show();
            } else {
                datesvalid = (dateNum <= courseDates.get(1));
                if (!datesvalid){
                    dataInvalid = true;
                    Toast.makeText(AssessmentsActivity.this, "Due date outside of course dates.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Use that Database Code Some more
        if (!dataInvalid){

            // courseID
            int course_id = database.getCourseID(course);
            // Insert
            boolean isInserted = database.insertAssessment(name, code, description, course_id, type, status, dateNum);

            if (isInserted){
                Toast.makeText(AssessmentsActivity.this, "Assessment added.",
                        Toast.LENGTH_SHORT).show();
                finish();
                // Danger Will Robinson
            } else {
                Toast.makeText(AssessmentsActivity.this, "Assessment not added.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Cancel & Clear Button
    public void cancelAssessmentAdd(View v){

        assessHeader.setText("");
        assesDescrip.setText("");
        assessCodeID.setText("");
        spinnerCourseAssess.setSelection(0);
        spinnerStatus.setSelection(0);
        spinnerType.setSelection(0);
        dateDUE.setText("");

        assessListView.setVisibility(View.VISIBLE);
        assessAss.setVisibility(View.VISIBLE);
        assessEnterView.setVisibility(View.GONE);
    }

    public ArrayList<List> getAssessments(){
        Cursor cursor = database.getAllAssessments();
        ArrayList<List> assessmentList = new ArrayList<>();

        while (cursor.moveToNext()){
            String id = cursor.getString(0);
            String name = cursor.getString(3);
            String type = cursor.getString(7);

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);
            data.add(type);
            assessmentList.add(data);
        }

        return assessmentList;
    }

    // DatePicker Update
    private void updateLabel(Calendar chosenCalendar){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat simpleDate = new SimpleDateFormat(myFormat, Locale.US);
        String newDate = simpleDate.format(chosenCalendar.getTime());
        dateDUE.setText(newDate);
    }

    // Back to....
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navmenu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.termscreen:
                startActivity(new Intent(this,
                        TermsActivity.class));
                return true;
            case R.id.coursesscreen:
                startActivity(new Intent(this,
                        CoursesActivity.class));
                return true;
            case R.id.assessmentsscreen:
                startActivity(new Intent(this,
                        AssessmentsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

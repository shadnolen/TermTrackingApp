package com.vizual.snolen.termtrackerapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

public class CourseDetailActivity extends AppCompatActivity {

    // Variable a day keeps the errors away...
    DatabaseSQLite database;
    Calendar calendar = Calendar.getInstance();

    EditText courseName, courseDescription, courseMentorName,
            courseMentorPhone, courseMentorMail;
    TextView courseStart, courseEnd;

    TableLayout assessmentsTable_CD, courseNotesTable;
    int dateFieldToggle;

    Spinner termSpinner, statusSpinner;
    ScrollView courseDetailView;

    String courseID = null;
    Button newNoteButton_CD;

    SharedPreferences sharedPrefer;
    SharedPreferences.Editor editor;

    Boolean startDateChanged = false;
    Boolean endDateChanged = false;

    // DatePicker
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
        setContentView(R.layout.activity_course_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Course Detail");

        database = new DatabaseSQLite(this);

        courseDetailView = (ScrollView) findViewById(R.id.courseDetailView);
        courseName = (EditText) findViewById(R.id.courseName_CD);
        termSpinner = (Spinner) findViewById(R.id.termSpinner_CD);

        courseDescription = (EditText) findViewById(R.id.courseDescription_CD);
        courseStart = (TextView) findViewById(R.id.editStart_CD);
        courseEnd = (TextView) findViewById(R.id.editEnd_CD);
        statusSpinner = (Spinner) findViewById(R.id.statusSpinner_CD);

        courseMentorName = (EditText) findViewById(R.id.editMentorName_CD);
        courseMentorPhone = (EditText) findViewById(R.id.editMentorPhone_CD);
        courseMentorMail = (EditText) findViewById(R.id.editMentorEmail_CD);

        courseNotesTable = (TableLayout) findViewById(R.id.notesTable_CD);
        assessmentsTable_CD = (TableLayout) findViewById(R.id.assessmentsTable_CD);

        newNoteButton_CD = (Button) findViewById(R.id.newNoteButton_CD);

        // Spinner status:
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.course_status,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // terms:
        List<String> termSpinnerList = database.getTermNames();
        ArrayAdapter<String> termAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, termSpinnerList);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        termSpinner.setAdapter(termAdapter);

        // Previous Activity?
        sharedPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefer.edit();
        courseID = sharedPrefer.getString("course", "");

        // Report Findings
        fillInCourse(courseID);

        // Date Listener
        courseStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) { startDateChanged = true; }
        });
        courseEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) { endDateChanged = true; }
        });

        // New Note Button
        newNoteButton_CD.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                newNote_CD();
            }
        });

    }

    public void fillInCourse(String courseID){
        Cursor cursor = database.getCourse(courseID);
        SimpleDateFormat simpledate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        // Course Data Text Fields
        cursor.moveToFirst();
        courseName.setText(cursor.getString(1));
        courseDescription.setText(cursor.getString(3));

        courseMentorName.setText(cursor.getString(6));
        courseMentorPhone.setText(cursor.getString(7));
        courseMentorMail.setText(cursor.getString(8));

        // Spinner start:
        String termSet = database.getTermName(cursor.getString(2));
        ArrayAdapter termAdap = (ArrayAdapter) termSpinner.getAdapter();
        int termSpinnerPosition = termAdap.getPosition(termSet);
        termSpinner.setSelection(termSpinnerPosition);

        // status:
        String statusSet = cursor.getString(9);
        ArrayAdapter statusAdap = (ArrayAdapter) statusSpinner.getAdapter();
        int statusSpinnerPosition = statusAdap.getPosition(statusSet);
        statusSpinner.setSelection(statusSpinnerPosition);

        // Fill dates:
        long startNum = cursor.getLong(4);
        long endNum = cursor.getLong(5);

        String start = simpledate.format(startNum);
        String end = simpledate.format(endNum);

        courseStart.setText(start);
        courseEnd.setText(end);

        // assessments:
        fillAssess(courseID);

        // notes:
        fillNote(courseID);

    }

    // Delete Button
    public void deleteButton_CD(View v){
        database.deleteCourse(courseID);

        //  Courses Activity Switch
        Intent intent = new Intent(v.getContext(), CoursesActivity.class);
        v.getContext().startActivity(intent);
    }

    // Save Button
    public void saveButton_CD(View v){
        boolean dataInvalid = false;

        String name = courseName.getText().toString();
        String description = courseDescription.getText().toString();
        String start = courseStart.getText().toString();
        String end = courseEnd.getText().toString();

        long numStart = 1;
        long numEnd = 1;

        String status = statusSpinner.getSelectedItem().toString();
        String term = termSpinner.getSelectedItem().toString();
        String menName = courseMentorName.getText().toString();
        String menPhone = courseMentorPhone.getText().toString();
        String menMail = courseMentorMail.getText().toString();


        //Got Validation?
        // blank:
        if (name.isEmpty()){
            Toast.makeText(CourseDetailActivity.this, "Enter name.",
                    Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        // unique:
        if (!dataInvalid){
            List<String> courseNames = database.getCourseNames();
            int temp = courseNames.contains(name) ? 1 : 2;
            if (temp == 1){
                int idFromDatabase = database.getCourseID(name);
                if (Integer.parseInt(courseID) != idFromDatabase) {
                    dataInvalid = true;
                    Toast.makeText(CourseDetailActivity.this, "Already in rolled in Course.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Covert to Long
        if (!dataInvalid) {

            // Empty Dates?
            if (start.isEmpty() || end.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(CourseDetailActivity.this, "Dates can't be blank.",
                        Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat simpledate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = simpledate.parse(start);
                    numStart = date.getTime();
                    date = simpledate.parse(end);
                    numEnd = date.getTime();
                } catch (ParseException e) {
                    dataInvalid = true;
                    Toast.makeText(CourseDetailActivity.this, "There was a problem, check your dates.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Start<-End
        if (!dataInvalid){
            boolean datesvalid = (numEnd > numStart);
            if (!datesvalid){
                Toast.makeText(CourseDetailActivity.this, "End date must be after start date.",
                        Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            }
        }

        //  -> Dates <-
        if (!dataInvalid){
            List<Long> termDates = database.getTermDates(term);
            boolean datesvalid = (numStart >= termDates.get(0));

            if (!datesvalid){
                Toast.makeText(CourseDetailActivity.this, "Start date outside of term.",
                        Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            } else {
                datesvalid = (numEnd <= termDates.get(1));
                if (!datesvalid){
                    Toast.makeText(CourseDetailActivity.this, "End date outside of term.",
                            Toast.LENGTH_SHORT).show();
                    dataInvalid = true;
                }
            }
        }


        // Overlapping?
        if (!dataInvalid){
            if (!startDateChanged || !endDateChanged) {
                boolean overlap = database.dateCompares("courses", numStart, numEnd);
                if (overlap) {
                    Toast.makeText(CourseDetailActivity.this, "Course dates overlap with another course.",
                            Toast.LENGTH_SHORT).show();
                    dataInvalid = true;
                }
            }
        }

        // You Should Know by Now.....
        if (!dataInvalid){
            int termID = database.getTermId(term);
            boolean isUpdated = database.updateCourse(courseID, name, termID, description, numStart, numEnd,
                    status, menName, menPhone, menMail);

            if (isUpdated){
                Toast.makeText(CourseDetailActivity.this, "Course updated.",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CourseDetailActivity.this, "Course not updated.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

     // New Note
    public void newNote_CD(){
        AlertDialog.Builder noteBuilder = new AlertDialog.Builder(this);
        noteBuilder.setTitle("Note");

        // Input
        final EditText noteInput = new EditText(this);
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setSingleLine(false);
        noteInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        noteInput.setVerticalScrollBarEnabled(true);
        noteInput.setMovementMethod(ScrollingMovementMethod.getInstance());
        noteBuilder.setView(noteInput);

        // Dialog Buttons

        noteBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteText = noteInput.getText().toString();
                boolean isInserted = database.insertCourseNote(courseID, noteText);
                if (isInserted) {
                    Toast.makeText(CourseDetailActivity.this, "Note added.",
                            Toast.LENGTH_SHORT).show();
                    fillInCourse(courseID);
                } else {
                    Toast.makeText(CourseDetailActivity.this, "Note not added.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        noteBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        noteBuilder.show();
    }

    public void fillNote(final String courseID){

        courseNotesTable.removeAllViews();

        Cursor cursor = database.getCourseNotes(courseID);
        ArrayList<List> notesList = new ArrayList<>();

        while(cursor.moveToNext()){
            String id = cursor.getString(0);
            String timestamp = cursor.getString(3);
            String note = cursor.getString(2);

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(timestamp);
            data.add(note);

            notesList.add(data);
        }

        for (List<String> list : notesList){
            final TableRow row = new TableRow(CourseDetailActivity.this);
            final TableRow row2 = new TableRow(CourseDetailActivity.this);
            TextView timestampCol = new TextView(CourseDetailActivity.this);
            TextView noteCol = new TextView(CourseDetailActivity.this);

            timestampCol.setText(list.get(1));
            timestampCol.setPadding(0, 0, 18, 0);
            noteCol.setMaxEms(12);
            noteCol.setSingleLine();
            noteCol.setEllipsize(TextUtils.TruncateAt.END);
            noteCol.setText(list.get(2));
            final String noteText = list.get(2);
            final String noteID = list.get(0);

            // Dialog Box View
            noteCol.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    // create:
                    AlertDialog.Builder noteBuilder = new AlertDialog.Builder(CourseDetailActivity.this);
                    noteBuilder.setTitle("Note");

                    // input:
                    final TextView noteInput = new TextView(CourseDetailActivity.this);
                    noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    noteInput.setSingleLine(false);
                    noteInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    noteInput.setVerticalScrollBarEnabled(true);
                    noteInput.setMovementMethod(ScrollingMovementMethod.getInstance());
                    noteInput.setText(noteText);
                    noteBuilder.setView(noteInput);

                    //wanna share?
                    noteBuilder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, noteText);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                            dialog.dismiss();
                        }
                    });

                    // 86' Note
                    noteBuilder.setNeutralButton("Return", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    // Delete
                    noteBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            database.deleteCourseNote(noteID);
                            fillNote(courseID);
                        }
                    });

                    noteBuilder.show();
                }
            });

            row.addView(timestampCol);
            row.addView(noteCol);

            courseNotesTable.addView(row);
            courseNotesTable.addView(row2);
        }

        cursor.close();
    }

    public void fillAssess(String courseID){

        assessmentsTable_CD.removeAllViews();
        Cursor cursor = database.getAssessmentsForOneCourse(courseID);
        ArrayList<List> assessmentList = new ArrayList<>();

        while(cursor.moveToNext()){

            String name = cursor.getString(3);
            String type = cursor.getString(5);
            String dateDUE = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(cursor.getLong(6)));

            List<String> data = new ArrayList();
            data.add(name);
            data.add(type);
            data.add(dateDUE);
            assessmentList.add(data);
        }

        for (List<String> list : assessmentList){
            TableRow row = new TableRow(CourseDetailActivity.this);

            TextView columnName = new TextView(CourseDetailActivity.this);
            TextView typeCol = new TextView(CourseDetailActivity.this);
            TextView dateCol = new TextView(CourseDetailActivity.this);

            Button assessmentDetails_CD = new Button(CourseDetailActivity.this);
            assessmentDetails_CD.setText("DETAILS");
            assessmentDetails_CD.setPadding(1, 0, 0, 0);

            columnName.setText(list.get(0));
            final String assessName = columnName.getText().toString();
            typeCol.setText(" " + list.get(1));
            dateCol.setText(" " + list.get(2));

            assessmentDetails_CD.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    // Shared Preferences -> next activity
                    editor.putString("assessment", assessName);
                    editor.commit();

                    // Assessment Details
                    Intent intent = new Intent(v.getContext(), AssessmentDetailActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
            row.addView(columnName);
            row.addView(typeCol);
            row.addView(dateCol);
            row.addView(assessmentDetails_CD);
            assessmentsTable_CD.addView(row);
        }
    }

    // Date Views
    public void startDateSelect_CD(View v){
            DatePickerDialog datepicker = new DatePickerDialog(CourseDetailActivity.this, datePick,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            List<Long> termDates = database.getTermDates(termSpinner.getSelectedItem().toString());
            dateFieldToggle = 1;

            datepicker.getDatePicker().setMinDate(termDates.get(0));
            datepicker.getDatePicker().setMaxDate(termDates.get(1));
            datepicker.show();
    }
    public void endDateSelect_CD(View v){
        DatePickerDialog datepicker = new DatePickerDialog(CourseDetailActivity.this, datePick,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        List<Long> termDates = database.getTermDates(termSpinner.getSelectedItem().toString());
        dateFieldToggle = 2;

        datepicker.getDatePicker().setMinDate(termDates.get(0));
        datepicker.getDatePicker().setMaxDate(termDates.get(1));
        datepicker.show();
    }

    // Assessment Button
    public void addAssessment_CD(View v){
        editor.putString("courseName", courseName.getText().toString());
        editor.commit();
        Intent intent = new Intent(v.getContext(), AssessmentsActivity.class);
        intent.putExtra("activity", ".CourseDetailActivity");
        v.getContext().startActivity(intent);
    }

    //  addAlert Button
    public void addAlert_CD(View v){
        //set course alerts
        String start = courseStart.getText().toString();
        String end = courseEnd.getText().toString();

        // TextView -> Long
        if (!start.isEmpty() && !end.isEmpty()){
            SimpleDateFormat simpledate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            try {
                Date startDate = simpledate.parse(start);
                Date endDate = simpledate.parse(end);
                long startNum = startDate.getTime();
                long endNum = endDate.getTime();

                Intent alertIntent = new Intent(this, AlarmNotifi.class);

                AlarmManager alarmManagerStart = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                AlarmManager alarmManagerEnd = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                alarmManagerStart.set(AlarmManager.RTC_WAKEUP, startNum, PendingIntent.getBroadcast(this, 1,
                        alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
                alarmManagerEnd.set(AlarmManager.RTC_WAKEUP, endNum, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

                Toast.makeText(CourseDetailActivity.this, "Alert set.",
                        Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {

            }
        } else {
            Toast.makeText(CourseDetailActivity.this, "Dates cannot be blank.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Update TextView Dates
    private void updateLabel(){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat simpledate = new SimpleDateFormat(myFormat, Locale.US);

        String newDate = simpledate.format(calendar.getTime());

        if(dateFieldToggle == 1) {
            courseStart.setText(newDate);
        }
        else
            courseEnd.setText(newDate);
    }
}

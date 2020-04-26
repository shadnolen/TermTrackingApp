package com.vizual.snolen.termtrackerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
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

public class TermsActivity extends AppCompatActivity {

    EditText termHeader;
    TextView editStart, editEnd;
    public Button saveButton, addButton, cancelButton;
    DatabaseSQLite database;
    Calendar calendar = Calendar.getInstance();
    TableLayout table;
    ScrollView termListView, termEntry;
    SharedPreferences sharedPrefer;
    SharedPreferences.Editor editor;

    // Generate DatePicker
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
        populateTerms();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = new DatabaseSQLite(this);

        getSupportActionBar().setTitle("Terms");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editStart = (TextView) findViewById(R.id.termStart);
        editEnd = (TextView) findViewById(R.id.termEnd);
        saveButton = (Button) findViewById(R.id.saveTermButton);
        addButton = (Button) findViewById(R.id.addTermButton);
        cancelButton = (Button) findViewById(R.id.cancelTermButton);
        table = (TableLayout) findViewById(R.id.termTable);
        termListView = (ScrollView) findViewById(R.id.termListView);
        termEntry = (ScrollView) findViewById(R.id.termEntry);
        termHeader = (EditText) findViewById(R.id.termHeader);

        sharedPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefer.edit();

        //DatePickers
        editStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(TermsActivity.this, datePick, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        termListView.setVisibility(View.VISIBLE);
        populateTerms();
    }

    // Populate
    public void populateTerms(){

        //clear existing view
        table.removeAllViews();

        // Check  Database for Existing Term
        ArrayList<List> allTerms = getTerms();

        //Fill Existing Term
        for (List<String> list : allTerms){

            TableRow row = new TableRow(TermsActivity.this);
            TextView idCol = new TextView(TermsActivity.this);
            TextView nameCol = new TextView(TermsActivity.this);
            TextView startCol = new TextView(TermsActivity.this);
            TextView spaceCol = new TextView(TermsActivity.this);
            TextView endCol = new TextView(TermsActivity.this);
            Button detailBtn = new Button(TermsActivity.this);

            //convert dates
            long start = Long.valueOf(list.get(2));
            long end = Long.valueOf(list.get(3));

            idCol.setText(list.get(0));
            idCol.setMinWidth(120);
            final String idNum = idCol.getText().toString();
            nameCol.setText(list.get(1));
            nameCol.setMinWidth(250);
            startCol.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(start)));
            startCol.setMinWidth(250);
            spaceCol.setMinWidth(45);
            endCol.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(end)));
            endCol.setMinWidth(250);

            detailBtn.setText("Details");
            detailBtn.setPadding(15,0, 0, 0);
            detailBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    //put term data into Shared Preferences to send to next activity
                    editor.putString("term", idNum);
                    editor.commit();

                    //Details activity
                    Intent intent = new Intent(v.getContext(), TermDetailsActivity.class);
                    v.getContext().startActivity(intent);
                }
            });

            row.addView(idCol);
            row.addView(nameCol);
            row.addView(startCol);
            row.addView(spaceCol);
            row.addView(endCol);
            row.addView(detailBtn);
            table.addView(row);
        }
    }

    // Add Term
    public void addTerm(View v){
        termListView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        termEntry.setVisibility(View.VISIBLE);

        if (database.isTermsEmpty()){
            termHeader.setText("Term 1");
        } else {
            fillInTerm();
        }

    }

    // Recent Term?
    public void fillInTerm(){
        Cursor cursor = database.getAllTerms();
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        cursor.moveToLast();
        String name = "Term Name" + (1 + cursor.getInt(0));
        long startNum = cursor.getLong(2);
        long endNum = cursor.getLong(3);
        String start = simpleDate.format(startNum);
        String end = simpleDate.format(endNum);

        //fill in new dates
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(simpleDate.parse(end));
            calendar.add(Calendar.DATE, 1);
            start = simpleDate.format(calendar.getTime());
            calendar.add(Calendar.MONTH, 6);
            calendar.add(Calendar.DATE, -1);
            end = simpleDate.format(calendar.getTime());
        } catch (ParseException e){
            Toast.makeText(TermsActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
        }

        termHeader.setText(name);
        editStart.setText(start);
        editEnd.setText(end);
    }

    // Save Button
    public void addData(View v) {
        boolean dataInvalid = false;
        String name = termHeader.getText().toString();
        String start = editStart.getText().toString();
        String end = editEnd.getText().toString();
        long numStart = 1;
        long numEnd = 1;



        // Blank Field?
        if (name.isEmpty()){
            Toast.makeText(TermsActivity.this, "Name cannot be blank.", Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        // Start Selected?
        if (!dataInvalid) {
            if (start.isEmpty()) {
                Toast.makeText(TermsActivity.this, "Select start date.", Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            }
        }

        // Unique Name?
        if (!dataInvalid) {
            List<String> terms = database.getTermNames();
            int temp = terms.contains(name) ? 1 : 2;
            if (temp == 1){
                dataInvalid = true;
                Toast.makeText(TermsActivity.this, "Term name already exists.", Toast.LENGTH_SHORT).show();
            }
            }


        //got Validation?
        if (!dataInvalid) {

            SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date startDate, endDate;

            // to Long
            try {
                startDate = simpleDate.parse(start);
                numStart = startDate.getTime();
                endDate = simpleDate.parse(end);
                numEnd = endDate.getTime();

                //Check Overlap
                boolean overlap = database.dateCompares("terms", numStart, numEnd);
                if (overlap) {
                    dataInvalid = true;
                    Toast.makeText(TermsActivity.this, "Term dates cannot overlap with other terms.", Toast.LENGTH_SHORT).show();
                }

            } catch (ParseException e) {
                dataInvalid = true;
                Toast.makeText(TermsActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
            }
        }


        if (!dataInvalid) {
            boolean isInserted = database.insertTermData(name, numStart, numEnd);

            if (isInserted) {
                Toast.makeText(TermsActivity.this, "Term added.", Toast.LENGTH_SHORT).show();

                // Repopulate Table
                populateTerms();


                termEntry.setVisibility(View.GONE);
                termListView.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
            }
            else
                Toast.makeText(TermsActivity.this, "Term not added.", Toast.LENGTH_SHORT).show();
        }
    }

    // Cancel Button
    public void cancelButton(View v){
        termEntry.setVisibility(View.VISIBLE);
        termListView.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
    }

    // Update Datepicker
    private void updateLabel(Calendar chosenCalendar){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat simpleDate = new SimpleDateFormat(myFormat, Locale.US);

        String newDate = simpleDate.format(chosenCalendar.getTime());
        chosenCalendar.add(Calendar.MONTH, 6);
        chosenCalendar.add(Calendar.DAY_OF_MONTH, -1);
        String endDate = simpleDate.format(chosenCalendar.getTime());
        editStart.setText(newDate);
        editEnd.setText(endDate);
    }

    //Existing Terms?
    public ArrayList<List> getTerms(){
        Cursor cursor = database.getAllTerms();

        ArrayList<List> termList = new ArrayList<>();

        while (cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String start = cursor.getString(cursor.getColumnIndex("startdate"));
            String end = cursor.getString(cursor.getColumnIndex("enddate"));

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);
            data.add(start);
            data.add(end);
            termList.add(data);
        }
        return termList;
    }

    //create navigation menu
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


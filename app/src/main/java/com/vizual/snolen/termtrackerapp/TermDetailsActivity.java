package com.vizual.snolen.termtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TermDetailsActivity extends AppCompatActivity {

    //Eat your Variables with Every Meal
    TextView termDetailHeader;
    TextView termDetailStart, termDetailEnd;
    Button deleteTermBtn;
    DatabaseSQLite database;
    TableLayout innerCoursesTable;
    ScrollView termDetails;
    String termID = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = new DatabaseSQLite(this);
        termDetails = (ScrollView) findViewById(R.id.termDetails);
        termDetailHeader = (TextView) findViewById(R.id.termDetailHeader);
        termDetailStart = (TextView) findViewById(R.id.termDetailStart);
        termDetailEnd = (TextView) findViewById(R.id.termDetailEnd);
        deleteTermBtn = (Button) findViewById(R.id.deleteTermDetail);
        innerCoursesTable = (TableLayout) findViewById(R.id.innerCoursesTable);

        // Previous Activity?
        SharedPreferences sharedPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefer.edit();
        termID = sharedPrefer.getString("term", "");

        //Populate is the rumour is true
        fillInTerm(termID);
        populateCourseTable(termID);

    }

    public void fillInTerm(String termID){
        Cursor res = database.getTerm(termID);
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        // Term Data
        res.moveToFirst();
        String name = res.getString(1);
        long startNum = res.getLong(2);
        long endNum = res.getLong(3);
        String start = simpleDate.format(startNum);
        String end = simpleDate.format(endNum);

        termDetailHeader.setText(name);
        termDetailStart.setText(start);
        termDetailEnd.setText(end);

        getSupportActionBar().setTitle(name);
    }

    public void populateCourseTable(String termID){

        innerCoursesTable.removeAllViews();
        Cursor cursor = database.getCoursesFromTerm(termID);
        ArrayList<List> coursesList = new ArrayList<>();

        while (cursor.moveToNext()){
            String id = cursor.getString(0);
            String name = cursor.getString(1);

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);

            coursesList.add(data);
        }

        for (List<String> list : coursesList){
            final TableRow row = new TableRow(TermDetailsActivity.this);
            TextView columnID = new TextView(TermDetailsActivity.this);
            TextView columnName = new TextView(TermDetailsActivity.this);
            Button removeCourseBtn = new Button(TermDetailsActivity.this);

            // Fill
            columnID.setText(list.get(0));
            columnID.setMinWidth(120);
            final String idNum = columnID.getText().toString();
            columnName.setText(list.get(1));
            columnName.setPadding(0,0, 5, 0);
            columnName.setMaxEms(17);
            columnName.setSingleLine();
            columnName.setEllipsize(TextUtils.TruncateAt.END);

            // Delete Button
            removeCourseBtn.setText("Delete");
            removeCourseBtn.setPadding(8, 0, 0, 0);
            removeCourseBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    database.deleteCourse(idNum);
                    row.setVisibility(View.GONE);
                }
            });

            row.addView(columnID);
            row.addView(columnName);
            row.addView(removeCourseBtn);
            innerCoursesTable.addView(row);
        }
    }

    // Delete Term
    public void deleteTerm(View v){

        // is it Empty?
        boolean empty = database.ifTermHasCourses(termID);

        if(empty){

            //Ok to Delete
            database.deleteTerm(termID);
            Toast.makeText(TermDetailsActivity.this, "Term deleted.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(v.getContext(), TermsActivity.class);
            v.getContext().startActivity(intent);
        } else {

            // Better fix that issue
            Toast.makeText(TermDetailsActivity.this, "Term cannot be deleted while courses are assigned.", Toast.LENGTH_LONG).show();
        }
    }
}

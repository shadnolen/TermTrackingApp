package com.vizual.snolen.termtrackerapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    Button termsButton, coursesButton, assessButton;
    DatabaseSQLite db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Term Tracking App");

        termsButton = (Button) findViewById(R.id.termsBtn);
        coursesButton = (Button) findViewById(R.id.coursesBtn);
        assessButton = (Button) findViewById(R.id.assessmentsBtn);

        // Terms
        termsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                db = new DatabaseSQLite(context);
                Intent intent = new Intent(v.getContext(), TermsActivity.class);
                v.getContext().startActivity(intent);
            }
        });

        // Courses
        coursesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                db = new DatabaseSQLite(context);

                //Term exist?
                if (db.isTermsEmpty()) {
                    Toast.makeText(MainActivity.this, "Add Terms before adding Courses.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(v.getContext(), CoursesActivity.class);
                    v.getContext().startActivity(intent);
                }
            }
        });

        // Assessments
        assessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                db = new DatabaseSQLite(context);
                if (db.isCoursesEmpty()) {
                    Toast.makeText(MainActivity.this, "Add Courses, before adding Assessments.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(v.getContext(), AssessmentsActivity.class);
                    v.getContext().startActivity(intent);
                }
            }
        });

    }

    //navigation menu
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

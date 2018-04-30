package com.couchbase.stockexchange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.util.ArrayList;
import java.util.Arrays;

public class FilterActivity extends AppCompatActivity {

    private Database db;
    private ArrayList<String> currentFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
        // Create a LinearLayout element
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        currentFilters = new ArrayList<>();

        db = ((Application) getApplication()).getDatabase();
        currentFilters = new ArrayList<>(Arrays.asList(((Application) getApplication()).getCurrentFilters()));
        Query filterQuery = QueryBuilder.selectDistinct(SelectResult.property("sector"))
                .from(DataSource.database(db))
                .where(Expression.property("sector").notNullOrMissing());

        try {
            ResultSet rs = filterQuery.execute();

            for (Result result: rs){
                String sector = result.getString("sector");
                CheckBox checkbox = new CheckBox(this);

                checkbox.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        boolean checked = ((CheckBox) view).isChecked();
                        String currentFilter = ((CheckBox) view).getText().toString();

                        if (checked){
                            currentFilters.add(currentFilter);
                        } else {
                            currentFilters.remove(currentFilter);
                        }
                    }
                });
                checkbox.setChecked(currentFilters.contains(sector));
                checkbox.setText(sector);
                linearLayout.addView(checkbox);
            }
        } catch (CouchbaseLiteException e) {
            Intent i = new Intent(getBaseContext(), ListDetailActivity.class);
            startActivity(i);
        }

        // Add the LinearLayout element to the ScrollView
        scrollView.addView(linearLayout);

    }

    public void applyFilters(View v){
        Log.e("Foo", currentFilters.toString());
        ((Application) getApplication()).setCurrentFilters(currentFilters.toArray(new String[currentFilters.size()]));
        Intent i = new Intent(getBaseContext(), ListDetailActivity.class);
        startActivity(i);
    }
}

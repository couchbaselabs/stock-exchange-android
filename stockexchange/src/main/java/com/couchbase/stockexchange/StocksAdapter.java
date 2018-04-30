package com.couchbase.stockexchange;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.internal.support.Log;


public class StocksAdapter extends ArrayAdapter<String> {
    private static final String TAG = StocksAdapter.class.getSimpleName();

    private StocksFragment fragment;
    private Database db;
    private String listID;

    private Query query;
    private ListenerToken queryListener;

    public StocksAdapter(final StocksFragment fragment, Database db) {
        super(fragment.getContext(), 0);
        this.fragment = fragment;
        this.db = db;
        ((Application) fragment.getActivity().getApplication()).addPrefixChangeListener(new MyChangeListener(){
            @Override
            public void onChange(){
                updateQuery();
            }
        });
        updateQuery();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_task, parent, false);

        String docID = getItem(position);
        final Document stock = db.getDocument(docID);
        if (stock == null)
            throw new IllegalStateException("Document does not exists: " + docID);

        // text
        TextView text = convertView.findViewById(R.id.text);
        text.setText(stock.getString("company"));

        TextView price = convertView.findViewById(R.id.price);
        Float pr = stock.getFloat("price");
        price.setText(pr.toString());

        return convertView;
    }

    private Query buildQuery(String[] sectors, String prefix) {
        Expression whereClause = Expression.property("company").notNullOrMissing().and(Expression.property("sector").notNullOrMissing()).and(Expression.property("price").notNullOrMissing());
        Expression sectorFilter = null;
        Expression prefixFilter = null;

        for (String sector: sectors){
            if (sectorFilter == null){
                sectorFilter = Expression.property("sector").equalTo(Expression.string(sector));
            } else {
                sectorFilter = sectorFilter.or(Expression.property("sector").equalTo(Expression.string(sector)));
            }
        }

        if (prefix != null){
            prefixFilter = Function.lower(Expression.property("company")).like(Function.lower(Expression.string("%" + prefix + "%")));
        }

        if (sectorFilter != null){
            whereClause = whereClause.and(sectorFilter);
        }

        if (prefixFilter != null){
            whereClause = whereClause.and(prefixFilter);
        }

        return QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("company"), SelectResult.property("price"))
                .from(DataSource.database(db)).where(whereClause).orderBy(Ordering.expression(Function.lower(Expression.property("company"))));
    }

    public void updateQuery(){
        if (query != null) {
            query.removeChangeListener(queryListener);
        }

        String[] filters = ((Application) fragment.getActivity().getApplication()).getCurrentFilters();
        String prefix =  ((Application) fragment.getActivity().getApplication()).getPrefix();
        query = buildQuery(filters, prefix);
        Log.e("TEST", query.toString());
        queryListener = query.addChangeListener(new QueryChangeListener() {
            @Override
            public void changed(final QueryChange change) {
                ((Activity) fragment.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clear();
                        ResultSet rs = change.getResults();
                        Result result;
                        while ((result = rs.next()) != null) {
                            Log.i(TAG, result.toMap().toString());
                            add(result.getString("id"));
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }
}

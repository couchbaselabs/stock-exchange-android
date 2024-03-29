package com.couchbase.stockexchange;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputType;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.internal.support.Log;

import java.util.Map;
import java.util.TreeMap;

public class StocksFragment extends Fragment {
    private static final String TAG = StocksFragment.class.getSimpleName();

    private ListView listView;
    private StocksAdapter adapter;

    private Database db;

    public StocksFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, null);

        db = ((Application) getActivity().getApplication()).getDatabase();

        adapter = new StocksAdapter(this, db);

        listView = view.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id) {
                RelativeLayout customView = (RelativeLayout) inflater.inflate(R.layout.info_popup,null);

                Document doc = db.getDocument(adapter.getItem(pos));
                Map<String, Object> info = null;
                try {
                    info = doc.toMap();
                } catch (Exception e){
                    Log.e("TAG", e.toString());
                    Log.e("TAG", doc.toMap().toString());
                }

                if (info != null){
                    info = new TreeMap<>(info);
                    Integer prevText = null;
                    for (String key : info.keySet())
                    {
                        String value = info.get(key).toString();

                        key = new StringBuffer(key.length())
                                .append(Character.toTitleCase(key.charAt(0)))
                                .append(key.substring(1))
                                .toString().replace("_", " ");
                        TextInputLayout textLayout = new TextInputLayout(getContext());
                        TextInputEditText textView = new TextInputEditText(getContext());
                        if (prevText != null) {
                            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            p.addRule(RelativeLayout.BELOW, prevText);
                            textLayout.setLayoutParams(p);
                        }
                        prevText = View.generateViewId();
                        textLayout.setId(prevText);
                        textLayout.setHint(key);
                        textLayout.setHintTextAppearance(R.style.TextAppearance_AppCompat_Large);
                        textView.setText(value);
                        textView.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                        textView.setInputType(InputType.TYPE_NULL);
                        textView.setFocusable(false);
                        textView.setClickable(false);
                        textView.setTextIsSelectable(false);
                        textView.setBackgroundResource(android.R.color.transparent);
                        textLayout.addView(textView);
                        customView.addView(textLayout);
                    }
                } else {
                    TextView textView = new TextView(getContext());
                    textView.setText("No additional information to display");
                    textView.setTextSize(25.0f);
                    customView.addView(textView);
                }

                final PopupWindow popup = new PopupWindow(
                        customView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    popup.setElevation(5.0f);
                }
                popup.setFocusable(true);
                popup.setTouchable(true);

                popup.showAtLocation(view, Gravity.CENTER,0, 0);
                return true;
            }
        });

        return view;
    }
}

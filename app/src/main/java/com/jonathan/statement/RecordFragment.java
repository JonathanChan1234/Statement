package com.jonathan.statement;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecordFragment extends Fragment implements FragmentBackHandler{

    public static final String RECORD_RESPONSE = "Record Response";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }
    ListView listView;
    Button deleteButton;

    RequestQueue requestQueue;
    String username;
    String read_record_api = "http://192.168.86.120/test-db-connection/record_query.php";
    String delete_record_api = "http://192.168.86.120/test-db-connection/delete_query.php";

    Spinner categorySpinner;
    String[] categoryArray;
    String selectedCategory;

    int[] idList;

    ArrayList<Integer> selectedItemId = new ArrayList<>();
    ArrayList<String> recordList;

    boolean isBackHandled = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestQueue = Volley.newRequestQueue(getContext());
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("controller", 0);
        username = sharedPreferences.getString(LoginActivity.DEFAULT_ACCOUNT, "");
        initUI(view);
    }

    private void initUI(View view) {
        listView = view.findViewById(R.id.recordList);
        deleteButton = view.findViewById(R.id.deleteButton);
        refreshList();
        categorySpinner = view.findViewById(R.id.categoryFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.category,
                android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categoryArray = getResources().getStringArray(R.array.category);
        setFilter();
        setDeletefunction();
    }

    private void refreshList() {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        FetchArrayRequest request = new FetchArrayRequest(Request.Method.POST, read_record_api, params, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(RECORD_RESPONSE, response.toString());
                updateListView(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(RECORD_RESPONSE, error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(request, "REFRESH_REQUEST");
    }

    private void setFilter() {
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory = categoryArray[i];
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("category", selectedCategory);
                FetchArrayRequest request = new FetchArrayRequest(Request.Method.POST, read_record_api, params, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(RECORD_RESPONSE, response.toString());
                        updateListView(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(RECORD_RESPONSE, error.getMessage());
                    }
                });
                AppController.getInstance().addToRequestQueue(request, "FILTER_REQUEST");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateListView(JSONArray response) {
        recordList = new ArrayList<>();
        if(response.length() == 0) recordList.clear();
        idList = new int[response.length()];
        for(int i=0; i<response.length(); ++i) {
            try {
                String record = response.getJSONObject(i).getString("created_at") +  " \tPurpose: "
                        + response.getJSONObject(i).getString("purpose") + "\n$"
                        + response.getJSONObject(i).getDouble("amount");
                recordList.add(record);
                idList[i] = response.getJSONObject(i).getInt("id");
                Log.d("ID", idList[i] + "");
            }
            catch(JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, recordList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.rgb(0, 0,0));
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("LongClicked", "add");
                isBackHandled = true;
                view.setBackgroundColor(Color.rgb(105, 255, 204));
                selectedItemId.add(idList[i]);
                setDeleteButtonVisible();
                return false;
            }
        });
    }

    private void setDeletefunction() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject query = new JSONObject();
                JSONArray idArray = Utils.listToJSONArray(selectedItemId);
                try {
                    query.put("ids", idArray);
                    query.put("username", username);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                Map<String, String> params = new HashMap<>();
                params.put("request", query.toString());
                Log.d("Query", query.toString());
                CustomRequest request = new CustomRequest(Request.Method.POST, delete_record_api, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {
                            if(response.getInt("success") == 1) {
                                Toast.makeText(getContext(), "Delete Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), "Something is wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        refreshList();
                        selectedItemId.clear();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", error.getMessage());
                        refreshList();
                        selectedItemId.clear();
                    }
                });
                AppController.getInstance().addToRequestQueue(request, "DELETE_REQUEST");
                isBackHandled = false;
                setDeleteButtonVisible();
            }
        });
    }

    private void setDeleteButtonVisible() {
        if(isBackHandled) {
            deleteButton.setVisibility(View.VISIBLE);
        }
        else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    public boolean onBackpressed() {
        if(isBackHandled) {
            isBackHandled = false;
            if(recordList == null) return false;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, recordList){
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = view.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.rgb(0, 0,0));
                    return view;
                }
            };
            listView.setAdapter(adapter);
            Toast.makeText(getContext(), "Back pressed", Toast.LENGTH_SHORT).show();
            setDeleteButtonVisible();
            if(selectedItemId != null) selectedItemId.clear();
            return true;
        }
        return BackHandlerHelper.handleBackPress(this);
    }
}
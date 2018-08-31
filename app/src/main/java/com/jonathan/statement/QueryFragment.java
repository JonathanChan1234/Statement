package com.jonathan.statement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QueryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_query, container, false);
    }

    Button submitButton;
    EditText descriptionText, amountText;
    Spinner purposeSpinner;
    RadioGroup group;

    String[] purposeArray;
    String purpose;
    String username;

    String insert_api = "http://192.168.86.120/test-db-connection/insert_query.php";
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        submitButton = view.findViewById(R.id.submitButton);
        descriptionText = view.findViewById(R.id.descriptionText);
        amountText = view.findViewById(R.id.amountText);
        purposeSpinner = view.findViewById(R.id.purposeSpinner);
        group = view.findViewById(R.id.typeGroup);

        purposeArray = getResources().getStringArray(R.array.purpose);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.purpose,
                android.R.layout.simple_spinner_dropdown_item);
        purposeSpinner.setAdapter(adapter);
        purposeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                purpose = purposeArray[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("controller", 0);
        username = sharedPreferences.getString(LoginActivity.DEFAULT_ACCOUNT, "");

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = descriptionText.getText().toString();
                String amount = amountText.getText().toString();

                int selectedType = group.getCheckedRadioButtonId();
                RadioButton selectedButton = view.findViewById(selectedType);
                Log.d("Radio", "no button " + selectedType + " found");
                String type = selectedButton.getText().toString();

                if(TextUtils.isEmpty(description) || TextUtils.isEmpty(amount) || TextUtils.isEmpty(purpose) || TextUtils.isEmpty(type)) {
                    Toast.makeText(getContext(), "Some info is missing", Toast.LENGTH_SHORT).show();
                }
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("description", description);
                params.put("amount", amount);
                params.put("type", type);
                params.put("purpose", purpose);

                CustomRequest request = new CustomRequest(Request.Method.POST, insert_api, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.getInt("success") == 1) {
                                Toast.makeText(getContext(), "Add successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                AppController.getInstance().addToRequestQueue(request, "SUBMIT_QUERY");
            }
        });
    }
}
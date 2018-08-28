package com.jonathan.statement;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class FetchArrayRequest extends Request<JSONArray> {
    private Response.Listener<JSONArray> listener;
    private Map<String, String> params;

    public FetchArrayRequest(String url, Map<String, String> params, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = responseListener;
        this.params = params;
    }

    public FetchArrayRequest(int method, String url, Map<String, String> params, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = responseListener;
        this.params = params;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    @Override
    protected void deliverResponse(JSONArray response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        }
        catch(UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
        catch(JSONException e) {
            return Response.error(new ParseError(e));
        }
    }
}

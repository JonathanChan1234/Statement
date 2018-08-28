package com.jonathan.statement;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

public class ReportFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    Button requestButton;
    ImageView requestImage, internalImage;
    String photo_url = "http://192.168.86.120/upload-picture/upload/profile1.jpg";
    RequestQueue requestQueue;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestButton = view.findViewById(R.id.requestButton);
        requestImage = view.findViewById(R.id.requestImage);
        internalImage = view.findViewById(R.id.internalImage);
        requestQueue = Volley.newRequestQueue(getContext());
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageRequest request = new ImageRequest(photo_url, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        requestImage.setImageBitmap(response);
                    }
                },0,
                        0,
                        ImageView.ScaleType.CENTER_CROP,
                        Bitmap.Config.RGB_565,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error", error.getMessage());
                            }
                        });
                requestQueue.add(request);
            }
        });
    }
}

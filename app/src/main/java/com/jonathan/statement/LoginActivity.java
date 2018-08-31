package com.jonathan.statement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String HTTP_REQUEST_TAG = "HTTP Request";
    public static final String DEFAULT_ACCOUNT = "DEFAULT_ACCOUNT";
    EditText accountText, passwordText, emailText;
    Button signInButton, createAccountButton;
    ImageView profileImage;

    boolean isPressed = false;
    SharedPreferences sharedPreferences;

    String userChoosenTask, encodedImage;
    Bitmap thumbnail;
    ProgressDialog progressDialog;

    int REQUEST_CAMERA = 0;
    int SELECT_FILE = 1;

    String url= "http://192.168.86.120/test-db-connection/index.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountText = findViewById(R.id.accountText);
        passwordText = findViewById(R.id.passwordText);
        emailText = findViewById(R.id.emailText);
        signInButton = findViewById(R.id.signInButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        profileImage = findViewById(R.id.profileImage);

        sharedPreferences = getSharedPreferences("controller", 0);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPressed) {
                    profileImage.setVisibility(View.VISIBLE);
                    emailText.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.GONE);
                    createAccountButton.setText("Create Account");
                }
                else {
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Waiting");
                    progressDialog.setTitle("Processing");
                    progressDialog.show();
                    createAccount(accountText.getText().toString(), passwordText.getText().toString(), emailText.getText().toString());
                    createAccountButton.setText("Register");
                    profileImage.setVisibility(View.GONE);
                    emailText.setVisibility(View.GONE);
                    signInButton.setVisibility(View.VISIBLE);
                }
                isPressed = !isPressed;
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(accountText.getText().toString(), passwordText.getText().toString());
            }
        });

        profileImage.setOnClickListener(setProfileImage);
    }

    private void signIn(final String account, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("username", account);
        params.put("password", password);
        CustomRequest customRequest = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getInt("success") == 1) {
                        Toast.makeText(LoginActivity.this,  "Log in Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(DEFAULT_ACCOUNT, account);
                        editor.apply();
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(LoginActivity.this,  "Wrong password or account name", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this,  "Fail to connect the server", Toast.LENGTH_SHORT).show();
                Log.d(HTTP_REQUEST_TAG + " Fail", error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(customRequest, "SIGNIN_REQUEST");
    }

    private void createAccount(final String account, String password, String email) {
        Map<String, String> params = new HashMap<>();
        params.put("username", account);
        params.put("password", password);
        params.put("email", email);
        if(thumbnail != null) {
            String image = getStringImage(thumbnail);
            JSONObject imageObject = new JSONObject();
            try {
                imageObject.put("size", "1000");
                imageObject.put("type", "image/jpg");
                imageObject.put("data", image);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            params.put("image", String.valueOf(imageObject));
        }
        CustomRequest customRequest = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getInt("success") == 1) {
                        if(progressDialog != null) progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,  "Create Account Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(DEFAULT_ACCOUNT, account);
                        editor.apply();
                        startActivity(intent);
                    }
                    else {
                        if(progressDialog != null) progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,  response.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
                catch(JSONException e) {
                    if(progressDialog != null) progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progressDialog != null) progressDialog.hide();
                Toast.makeText(LoginActivity.this,  "Fail to connect the server", Toast.LENGTH_SHORT).show();
                Log.d(HTTP_REQUEST_TAG + " Fail", error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(customRequest, "ACCOUNT_REQUEST");
    }

    View.OnClickListener setProfileImage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            selectRetrieveMethod();
        }
    };

    private void selectRetrieveMethod() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean result = Utils.checkPermission(LoginActivity.this);
                if(items[i].equals("Take Photo")) {
                    Log.d("Camera", "take photo in selectImg");
                    userChoosenTask = "Take Photo";
                    if(result) cameraIntent();
                }
                else if(items[i].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if(result) galleryIntent();
                }
                else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo")) {
                        Log.d("Camera", "take photo in Permission read external storage");
                        cameraIntent();
                    }
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(intent);
            }
            else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(intent);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        thumbnail = (Bitmap) data.getExtras().get("data");
        Log.d("capture image", thumbnail.toString());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        Log.d("Path", destination.getPath());
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        profileImage.setImageBitmap(thumbnail);
    }

    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(LoginActivity.this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        profileImage.setImageBitmap(thumbnail);
    }

    public String getStringImage(Bitmap bmp) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedImage;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return encodedImage;
    }
}

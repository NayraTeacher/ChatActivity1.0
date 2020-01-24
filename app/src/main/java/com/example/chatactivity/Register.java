package com.example.chatactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Register extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private EditText username, password;
    private String user, pass;

    private FirebaseDatabase fbdb;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseUser userauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        fbdb = FirebaseDatabase.getInstance();

        //Abrir Intent de autenticacion firebase
        // Choose authentication providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.GitHubBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                userauth = FirebaseAuth.getInstance().getCurrentUser();
                UserDetails.username = userauth.getDisplayName();
                UserDetails.password = "";
                user = UserDetails.username;
                pass = UserDetails.password;
                registerInFB();
                startActivity(new Intent(Register.this, Contacts.class));

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();

            }
        }
    }


    public void login(View v) {
        startActivity(new Intent(Register.this, Login.class));
    }

    public void register(View v) {
        user = username.getText().toString();
        pass = password.getText().toString();
        if (validate(user, pass))
            registerInFB();

    }

    private boolean validate(String user, String pass){
        boolean ok = false;
        if(user.equals("")){
            username.setError("can't be blank");
        }
        else if(pass.equals("")){
            password.setError("can't be blank");
        }
        else if(!user.matches("[A-Za-z0-9]+")){
            username.setError("only alphabet or number allowed");
        }
        else if(user.length()<5){
            username.setError("at least 5 characters long");
        }
        else if(pass.length()<5){
            password.setError("at least 5 characters long");
        }
        else {
            ok = true;
        }
        return ok;
    }

    private void registerInFB() {
            final ProgressDialog pd = new ProgressDialog(Register.this);
            pd.setMessage("Loading...");
            pd.show();

            String url = "https://chatactivity-495ae.firebaseio.com/users.json";

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                @Override
                public void onResponse(String s) {
                    DatabaseReference reference = fbdb.getReference().child("users");

                    if(s.equals("null")) {
                        reference.child(user).child("password").setValue(pass);
                        Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                    }
                    else {
                        try {
                            JSONObject obj = new JSONObject(s);

                            if (!obj.has(user)) {
                                reference.child(user).child("password").setValue(pass);
                                Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Register.this, "username already exists", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    pd.dismiss();
                }

            },new com.android.volley.Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("" + volleyError );
                    pd.dismiss();
                }
            });

            RequestQueue rQueue = Volley.newRequestQueue(Register.this);
            rQueue.add(request);
        }



}

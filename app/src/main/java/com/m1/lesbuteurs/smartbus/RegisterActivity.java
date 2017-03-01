package com.m1.lesbuteurs.smartbus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;
import com.m1.lesbuteurs.smartbus.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_REGISTER;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputLastname;
    private EditText inputFirstname;
    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputLastname = (EditText) findViewById(R.id.lastname);
        inputFirstname = (EditText) findViewById(R.id.firstname);
        inputUsername = (EditText) findViewById(R.id.username);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String lastname = inputLastname.getText().toString().trim();
                String firstname = inputFirstname.getText().toString().trim();
                String username = inputUsername.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!username.isEmpty() && !lastname.isEmpty() && !firstname.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    registerUser(username, lastname, firstname, email, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Renseigner tous les champs !", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * Inscription et insertion base de donnée pour connexion
     * */
    private void registerUser(final String username, final String lastname, final String firstname, final String email, final String password) {

        pDialog.setMessage("Inscription ...");
        showDialog();

        StringRequest registerApp = new StringRequest(Request.Method.POST, URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    JSONObject user = jObj.getJSONObject("created");
                    String username = user.getString("username");
                    String lastname = user.getString("lastname");
                    String firstname = user.getString("firstname");
                    String email = user.getString("email");
                    String api_token = user.getString("api_token");
                    String created_at = user.getString("created_at");

                    db.addUser(username, lastname, firstname, inputPassword.getText().toString(), email,
                            null, null, null, null, null, null, null,
                            null, null, null, null, null, api_token, created_at);

                    Toast.makeText(getApplicationContext(), "Inscription réussi. Connectez-vous !", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Username ou Email déjà utilisé !", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("lastname", lastname);
                params.put("firstname", firstname);
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(registerApp);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}

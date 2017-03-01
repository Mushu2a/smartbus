package com.m1.lesbuteurs.smartbus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;
import com.m1.lesbuteurs.smartbus.helper.SessionManager;
import com.sendbird.android.SendBird;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL;
import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_LOGIN;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private Button btnLogin;
    private Button btnLinkToRegister;

    private static final String appSenderBirdId = "413E6E38-0D52-4523-AB7F-22A62053B908";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean ping = executeCommand(URL);

        if (ping != true) {
            URL = "smartbus.pe.hu";
        }

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Initialise SenderBird
        SendBird.init(appSenderBirdId, this);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        // Vérifie si l'utilisateur est déjà authentifié
        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Vérifie si les information son non vide
                if (!email.isEmpty() && !password.isEmpty()) {
                    checkLogin(email, password);
                } else {
                    Toast.makeText(getApplicationContext(),"Merci de vérifier les informations !", Toast.LENGTH_LONG).show();
                }
            }

        });

        // Lien vers l'enregistrement
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * Fonction qui vérifie les détails de la connexion
     * */
    private void checkLogin(final String email, final String password) {

        pDialog.setMessage("Connexion ...");
        showDialog();

        StringRequest connectApp = new StringRequest(Request.Method.GET, URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    if (response != "Unauthorized.") {
                        // Création de la session authentifié, puisque le login est bon
                        session.setLogin(true);

                        // Enregistre dans la BDD SQLite
                        String username = jObj.getString("username");
                        String lastname = jObj.getString("lastname");
                        String firstname = jObj.getString("firstname");
                        String email = jObj.getString("email");
                        String phone = jObj.getString("phone");
                        String birthday = jObj.getString("birthday");
                        String address1 = jObj.getString("address1");
                        String address2 = jObj.getString("address2");
                        String city = jObj.getString("city");
                        String zip = jObj.getString("zip");
                        String country = jObj.getString("country");
                        String gender = jObj.getString("gender");
                        String brandBus = jObj.getString("brandBus");
                        String comfort = jObj.getString("comfort");
                        String number = jObj.getString("number");
                        String owner = jObj.getString("owner");
                        String api_token = jObj.getString("api_token");
                        String created_at = jObj.getString("created_at");

                        // Inserting row in users table
                        db.addUser(username, lastname, firstname, inputPassword.getText().toString(), email, phone, birthday, address1, address2, city, zip,
                                country, gender, brandBus, comfort, number, owner, api_token, created_at);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Erreur connexion", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "JSON erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Erreur connexion : " + error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("email", email);
                headers.put("username", email);
                headers.put("password", password);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(connectApp);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public boolean executeCommand(String url){
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + url);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if (mExitValue == 0){
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            System.out.println(" Exception : "+ignore);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" Exception : "+e);
        }
        return false;
    }
}

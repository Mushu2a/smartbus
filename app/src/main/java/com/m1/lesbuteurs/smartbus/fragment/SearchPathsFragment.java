package com.m1.lesbuteurs.smartbus.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.m1.lesbuteurs.smartbus.AppController;
import com.m1.lesbuteurs.smartbus.R;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_ADD_BOOKING;
import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_SEARCH_PATHS;

public class SearchPathsFragment extends AppCompatActivity {

    private SQLiteHandler db;
    String username;
    String email;
    String password;

    String de;
    String vers;
    String aller;
    String retour;

    ArrayList<HashMap<String, String>> pathsList;
    private ListView lv;

    public SearchPathsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_searchpaths);

        db = new SQLiteHandler(getApplicationContext());

        Intent content = getIntent();
        Bundle bundle = content.getExtras();

        if(bundle != null) {
            de = (String) bundle.get("de");
            vers = (String) bundle.get("vers");
            aller = (String) bundle.get("aller");
            retour = (String) bundle.get("retour");
        }

        pathsList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        searchPaths(de, vers, aller, retour);
    }

    /**
     * Recherche les trajets
     */
    private void searchPaths(final String de, final String vers, final String aller, final String retour) {

        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");
        password = user.get("password");
        email = user.get("email");

        Map<String,String> params = new HashMap<String, String>();
        params.put("startCity", de);
        params.put("finnishCity", vers);
        params.put("dateTo", aller);
        params.put("dateFrom", retour);

        JsonObjectRequest searchPaths = new JsonObjectRequest(Request.Method.POST, URL_SEARCH_PATHS, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // handle response
                try {

                    if (response.toString() != "Unauthorized.") {

                        JSONArray paths = response.getJSONArray("paths");

                        for (int i = 0; i < paths.length(); i++) {
                            JSONObject json = paths.getJSONObject(i);

                            int id = json.getInt("id");
                            int bookingSeats = json.getInt("bookingSeats");
                            int remainingSeats = json.getInt("remainingSeats");
                            int type = json.getInt("type");
                            String startCity = json.getString("startCity");
                            String middleCity = json.getString("middleCity");
                            String finnishCity = json.getString("finnishCity");
                            String price = json.getString("price");
                            String date = json.getString("date");
                            String startTime = json.getString("startTime");

                            HashMap<String, String> path = new HashMap<>();
                            if (bookingSeats < remainingSeats) {
                                path.put("id", ""+id);
                                path.put("place", (remainingSeats - bookingSeats) + "/" + remainingSeats + " place(s)");
                                if (type == 1) {
                                    path.put("type", "Aller");
                                } else if (type == 2) {
                                    path.put("type", "Retour");
                                } else {
                                    path.put("type", "Aller-Retour");
                                }
                                path.put("startCity", "Départ : " + startCity);
                                if (json.isNull("middleCity")) {
                                    path.put("middleCity", "");
                                } else {
                                    path.put("middleCity", "Détour : " + middleCity);
                                }
                                path.put("finnishCity", "Arrivée : " + finnishCity);
                                path.put("price", price + "€");
                                String finalDate = date.substring(0, 10);
                                path.put("date", finalDate + " à " + startTime);

                                // Ajout dans la liste
                                pathsList.add(path);
                            }
                        }

                        ListAdapter adapter = new SimpleAdapter(getApplicationContext(), pathsList,
                                R.layout.listview_list_paths,
                                new String[]{"place", "type", "startCity", "middleCity", "finnishCity", "price", "date"},
                                new int[]{R.id.place, R.id.type, R.id.toPath, R.id.middlePath, R.id.fromPath, R.id.price, R.id.date});

                        lv.setAdapter(adapter);
                        lv.setOnItemClickListener(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                                    Toast.makeText(getApplicationContext(), "Voir détails avec profil", Toast.LENGTH_LONG).show();
                                }
                            }
                        );
                        lv.setOnItemLongClickListener(
                            new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {

                                    // Après appuie long sur le choix du trajet, réservation
                                    HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(position);

                                    StringRequest bookingPath = new StringRequest(Request.Method.POST, URL_ADD_BOOKING+"/"+map.get("id"), new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Toast.makeText(getApplicationContext(), ""+response, Toast.LENGTH_LONG).show();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // handle error
                                        }
                                    }) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String,String> headers = new HashMap<String, String>();
                                            headers.put("Content-Type","application/x-www-form-urlencoded");
                                            headers.put("username", username);
                                            headers.put("email", email);
                                            headers.put("password", password);
                                            return headers;
                                        }
                                    };

                                    AppController.getInstance().addToRequestQueue(bookingPath);

                                    return true;
                                }
                            }
                        );

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
                // handle error
                Toast.makeText(getApplicationContext(), "Aucun résultat trouvé !", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Content-Type","application/x-www-form-urlencoded");
                headers.put("username", username);
                headers.put("email", email);
                headers.put("password", password);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(searchPaths);
    }
}

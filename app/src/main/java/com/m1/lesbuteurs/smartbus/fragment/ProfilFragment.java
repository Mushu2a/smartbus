package com.m1.lesbuteurs.smartbus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.m1.lesbuteurs.smartbus.AppController;
import com.m1.lesbuteurs.smartbus.R;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_LOGIN;

public class ProfilFragment extends ListFragment implements View.OnClickListener {

    private boolean viewGroupIsVisible = true;
    private View mViewGroup;
    private Button mButton;
    private Button mButtonUpdate;

    private SQLiteHandler db;
    String username;
    String password;
    String email;

    private String[] arrText = {
        "Pseudo : ", "Mot de passe : ", "Nom : ", "Prénom : ", "Email : ", "Tel : ", "Date : ",
        "Adresse : ", "complément : ", "Ville : ", "CP : ", "Pays : ", "Genre : ",
        "Marque : ", "Comfort : ", "Nombre Place : ", "Proprétaire : "
    };

    ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
    SimpleAdapter adapter;

    public ProfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new SQLiteHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");
        password = user.get("password");
        String lastname = user.get("lastname");
        String firstname = user.get("firstname");
        email = user.get("email");

        String[] user_profil = {
            username, "", lastname, firstname, email, user.get("phone"),
            user.get("birthday"), user.get("address1"), user.get("address2"), user.get("city"),
            user.get("zip"), user.get("country"), user.get("gender"), user.get("brandBus"),
            user.get("comfort"), user.get("number"), user.get("owner")
        };

        for (int i = 0; i < arrText.length; i++) {
            map = new HashMap<String, String>();
            map.put("arrText", arrText[i]);
            map.put("user", user_profil[i]);
            data.add(map);
        }

        TextView user_name = (TextView) view.findViewById(R.id.user_name);
        lastname = lastname.substring(0,1).toUpperCase() + lastname.substring(1).toLowerCase();
        firstname = firstname.substring(0,1).toUpperCase() + firstname.substring(1).toLowerCase();
        user_name.setText(lastname + " " + firstname);

        TextView pseudo = (TextView) view.findViewById(R.id.username);
        pseudo.setText(username);

        mViewGroup = view.findViewById(R.id.profile_layout);

        mButton = (Button) view.findViewById(R.id.button);
        mButton.setOnClickListener(this);

        mButtonUpdate = (Button) view.findViewById(R.id.update_profil);
        mButtonUpdate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                updateProfil(username, email, password, view);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] from = {"arrText", "user"};
        int[] to = {R.id.textView1, R.id.editText1};

        adapter = new SimpleAdapter(getActivity(), data, R.layout.listview_list_profil, from, to);
        setListAdapter(adapter);
    }

    /**
     * Fonction qui modifie les informations du profil
     * */
    private void updateProfil(final String username, final String email, final String password, View view) {

        Map<String,String> params = new HashMap<String, String>();
        /*params.put("username", "mushu2");
        params.put("password", "");
        params.put("lastname", "");
        params.put("firstname", "");
        params.put("email", "");
        params.put("phone", "");
        params.put("birthday", "");
        params.put("address1", "");
        params.put("address2", "");
        params.put("city", "");
        params.put("zip", "");
        params.put("country", "");
        params.put("gender", "");
        params.put("brandBus", "");
        params.put("comfort", "");
        params.put("number", "");
        params.put("owner", "");*/

        JsonObjectRequest updateLogin = new JsonObjectRequest(Request.Method.POST, URL_LOGIN, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // handle response
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

        AppController.getInstance().addToRequestQueue(updateLogin);

        getActivity().finish();
        startActivity(getActivity().getIntent());
    }

    /**
    * Vérifier si les edit text important sont non vide
    * */
    private boolean validate(EditText[] fields){
        for(int i = 0; i < fields.length; i++){
            EditText currentField = fields[i];
            if(currentField.getText().toString().length() <= 0){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View button) {

        if (viewGroupIsVisible) {
            mViewGroup.setVisibility(View.GONE);
            mButton.setText("Afficher");
        } else {
            mViewGroup.setVisibility(View.VISIBLE);
            mButton.setText("Cacher");
        }

        viewGroupIsVisible = !viewGroupIsVisible;
    }
}

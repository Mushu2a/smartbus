package com.m1.lesbuteurs.smartbus.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.m1.lesbuteurs.smartbus.AppController;
import com.m1.lesbuteurs.smartbus.R;
import com.m1.lesbuteurs.smartbus.adapter.Path;
import com.m1.lesbuteurs.smartbus.adapter.PathsAdapter;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_BOOKINGS;
import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_PATHS_CREATE;

public class PathsFragment extends Fragment {

    private SQLiteHandler db;
    String username;
    String email;
    String password;

    String usernameC;
    String emailC;
    String passwordC;

    private boolean viewGroupIsVisible = true;
    private View mViewGroup;
    private Button mButton;
    private Button mButtonUpdate;

    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;

    DatePicker datePicker;
    EditText de;
    EditText vers;
    EditText aller;
    EditText retour;

    EditText newPathEditToPlace;
    EditText newPathEditFromPlace;
    EditText newPathEditPrice;
    EditText newPathEditDate;
    EditText newPathEditStartTime;

    private Spinner spinner;
    TabHost tabHost;

    private RecyclerView recyclerView;
    private PathsAdapter adapter;
    private List<Path> pathList;

    public PathsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new SQLiteHandler(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_paths, container, false);

        /*
        * Concerne TAB1
        * */
        de = (EditText) rootView.findViewById(R.id.editTo);
        vers = (EditText) rootView.findViewById(R.id.editFrom);
        aller = (EditText) rootView.findViewById(R.id.editGo);
        retour = (EditText) rootView.findViewById(R.id.editReturn);
        Button search = (Button) rootView.findViewById(R.id.searchPath);

        Calendar mcurrentDate = Calendar.getInstance();
        mYear = mcurrentDate.get(Calendar.YEAR);
        mMonth = mcurrentDate.get(Calendar.MONTH);
        mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        aller.setText(mDay + "." + mMonth + "." + mYear);
        retour.setText(mDay + "." + mMonth + "." + mYear);

        aller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        aller.setText(selectedday + "." + (selectedmonth + 1) + "." + selectedyear);
                    }
                },mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        retour.setText(selectedday + "." + (selectedmonth + 1) + "." + selectedyear);
                    }
                },mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (de.getText().toString().matches("") || vers.getText().toString().matches("")) {
                    Toast.makeText(getContext(), "Vous devez renseigner un départ et une arrivée", Toast.LENGTH_LONG).show();
                } else {
                    Intent searchPathIntent = new Intent(getContext(), SearchPathsFragment.class);
                    searchPathIntent.putExtra("de", de.getText().toString());
                    searchPathIntent.putExtra("vers", vers.getText().toString());
                    searchPathIntent.putExtra("aller", aller.getText().toString());
                    searchPathIntent.putExtra("retour", retour.getText().toString());
                    startActivity(searchPathIntent);
                }
            }
        });

        /*
        * Concerne TAB2
        * */
        /*
          "startCity":"Ajaccio",
          "finnishCity":"Vero",*/
        spinner = (Spinner) rootView.findViewById(R.id.newPathType);
        newPathEditToPlace = (EditText) rootView.findViewById(R.id.newPathEditToPlace);
        newPathEditFromPlace = (EditText) rootView.findViewById(R.id.newPathEditFromPlace);
        newPathEditPrice = (EditText) rootView.findViewById(R.id.newPathEditPrice);
        newPathEditDate = (EditText) rootView.findViewById(R.id.newPathEditDate);
        newPathEditStartTime = (EditText) rootView.findViewById(R.id.newPathEditStartTime);

        newPathEditDate.setText(mDay + "." + mMonth + "." + mYear);

        newPathEditDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        newPathEditDate.setText(selectedday + "." + (selectedmonth + 1) + "." + selectedyear);
                    }
                },mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });

        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        newPathEditStartTime.setText(mHour + ":" + mMinute);

        newPathEditStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        newPathEditStartTime.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        Button newPath = (Button) rootView.findViewById(R.id.newPath);
        newPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newPathEditToPlace.getText().toString().matches("") || newPathEditFromPlace.getText().toString().matches("")) {
                    Toast.makeText(getContext(), "Vous devez renseigner un départ et une arrivée", Toast.LENGTH_LONG).show();
                } else {

                    HashMap<String, String> userC = db.getUserDetails();
                    usernameC = userC.get("username");
                    passwordC = userC.get("password");
                    emailC = userC.get("email");

                    Map<String,String> params = new HashMap<String, String>();
                    params.put("type", ""+spinner.getSelectedItemId()+"");
                    params.put("startCity", newPathEditToPlace.getText().toString());
                    params.put("finnishCity", newPathEditFromPlace.getText().toString());
                    params.put("price", newPathEditPrice.getText().toString());
                    params.put("date", newPathEditDate.getText().toString());
                    params.put("startTime", newPathEditStartTime.getText().toString());

                    JsonObjectRequest creatPath = new JsonObjectRequest(Request.Method.POST, URL_PATHS_CREATE, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(getContext(), ""+response, Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), ""+error, Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> headers = new HashMap<String, String>();
                            headers.put("Content-Type","application/x-www-form-urlencoded");
                            headers.put("username", usernameC);
                            headers.put("email", emailC);
                            headers.put("password", passwordC);
                            return headers;
                        }
                    };

                    AppController.getInstance().addToRequestQueue(creatPath);
                }
            }
        });

        /*
        * Concerne TAB3
        * */
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        pathList = new ArrayList<>();
        adapter = new PathsAdapter(getActivity(), pathList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        preparePaths();

        /*
        * Créer les tabs
        * */
        TabHost host = (TabHost) rootView.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Recherche");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Recherche");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Nouveau");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Nouveau");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Mes trajets");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Mes trajets");
        host.addTab(spec);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Adding few albums for testing
     */
    private void preparePaths() {

        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");
        password = user.get("password");
        email = user.get("email");


        Map<String,String> params = new HashMap<String, String>();

        JsonObjectRequest myPaths = new JsonObjectRequest(Request.Method.GET, URL_BOOKINGS, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    if (response.toString() != "Unauthorized.") {

                        JSONArray bookings = response.getJSONArray("booking");

                        for (int i = 0; i < bookings.length(); i++) {
                            JSONObject jsonObjectBooking = bookings.getJSONObject(i);

                            // Phone node is JSON Object
                            JSONObject path = jsonObjectBooking.getJSONObject("path");

                            int user_id = path.getInt("user_id");
                            String startCity = path.getString("startCity");
                            String finnishCity = path.getString("finnishCity");
                            String date = path.getString("date");

                            Path a = new Path(user_id, startCity +" - "+ finnishCity, date);
                            pathList.add(a);

                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Erreur connexion", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "JSON erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Erreur de création !", Toast.LENGTH_LONG).show();
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

        AppController.getInstance().addToRequestQueue(myPaths);
    }
}

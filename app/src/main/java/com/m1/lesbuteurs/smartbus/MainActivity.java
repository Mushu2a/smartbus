package com.m1.lesbuteurs.smartbus;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.HashMap;
import java.util.List;

import com.m1.lesbuteurs.smartbus.breakout.Splash;
import com.m1.lesbuteurs.smartbus.fragment.FragmentDrawer;
import com.m1.lesbuteurs.smartbus.fragment.MessagesFragment;
import com.m1.lesbuteurs.smartbus.fragment.PathsFragment;
import com.m1.lesbuteurs.smartbus.fragment.ProfilFragment;

import com.m1.lesbuteurs.smartbus.map.MapLocationActivity;
import com.m1.lesbuteurs.smartbus.map.MapMarkerLocationActivity;
import com.m1.lesbuteurs.smartbus.qrcode.ScanQRCode;

import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;
import com.m1.lesbuteurs.smartbus.helper.SessionManager;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener, LocationListener {

    private SQLiteHandler db;
    private SessionManager session;

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    private String loca;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Retour détails de l'utilisateur gràce à sqlite
        HashMap<String, String> user = db.getUserDetails();

        String username = user.get("username");
        String lastname = user.get("lastname");
        String firstname = user.get("firstname");
        String email = user.get("email");

        HashMap<String, String> userS = session.getUserDetails();

        // Displaying the user details on the screen
        // txtName.setText(name);
        // txtEmail.setText(email);
        // txtLastname.setText(lastname);
        // txtFirstname.setText(firstname);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_phone) {

            checkLocation();
            loca = getLastKnownLocation();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("SMS d'alert");
            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Non",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Oui",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:114"));
                        sendIntent.putExtra("sms_body", "Un problème est survenue, voici ma position : " + loca);
                        startActivity(sendIntent);
                    }
                });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return true;
        }

        if (id == R.id.action_location){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Redirection");
            alertDialogBuilder
                .setMessage("Que voulez-vous faire ?")
                .setCancelable(false)
                .setPositiveButton("Voir les localisations",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                    Intent newActivityLocation = new Intent(MainActivity.this, MapMarkerLocationActivity.class);
                    startActivity(newActivityLocation);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                })
                .setNegativeButton("Être localisé",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    Intent newActivityLocation = new Intent(MainActivity.this, MapLocationActivity.class);
                    startActivity(newActivityLocation);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return true;
        }

        if (id == R.id.action_qrcode){
            Intent newActivityQRCode = new Intent(MainActivity.this, ScanQRCode.class);
            startActivity(newActivityQRCode);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new ProfilFragment();
                title = getString(R.string.title_profil);
                break;
            case 1:
                fragment = new PathsFragment();
                title = getString(R.string.title_paths);
                break;
            case 2:
                fragment = new MessagesFragment();
                title = getString(R.string.title_messages);
                break;
            case 3:
                Intent intent1 = new Intent(MainActivity.this, Splash.class);
                startActivity(intent1);
                break;
            case 4:
                // Déconnecte l'utilisateur
                logoutUser();
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Déconnecte l'utilisateur.
     * */
    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        session.logoutUser();

        // Redirection vers l'activity de connexion
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                checkLocation();
                break;
            default:
                break;
        }
    }

    void checkLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
            return;
        }
    }

    private String getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        String location = "";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
        }

        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
                location = "{ " + l.getLatitude() + ", " + l.getLongitude() + " }";
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

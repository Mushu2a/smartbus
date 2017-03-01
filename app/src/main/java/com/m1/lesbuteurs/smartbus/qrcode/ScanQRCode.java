package com.m1.lesbuteurs.smartbus.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Importation des classes IntentIntegrator et IntentResult de la librairie zxing
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.m1.lesbuteurs.smartbus.R;

public class ScanQRCode extends AppCompatActivity implements View.OnClickListener {

    // Storage Permissions
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode_view);

        qrcodeView();

        Button scan = (Button) findViewById(R.id.scan_button);
        scan.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_button){
            qrcodeView();
        }
    }

    /**
     * Appeler quand le bouton 'scan' est cliquer
     */
    public void qrcodeView() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Les autorisations n'ont pas été accepter
            requestCameraPermission();

        } else {
            // Les autorisations de la caméra accepter et du coup lance le scanner
            IntentIntegrator integrator = new IntentIntegrator(this);
            // integrator.setPrompt("Scan a barcode");
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        }
    }

    /**
     * Affichage des informations du QR Code
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Annuler", Toast.LENGTH_LONG).show();
            } else {
                String scanContent = result.getContents();
                String scanFormat = result.getFormatName();

                TextView scan_content = (TextView) findViewById(R.id.scan_content);

                scan_content.setText("FORMAT: " + scanFormat + "\n\n CONTENU: \n\n" + scanContent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    /**
     * Requête pour permissions de la camera
     * Si les permissions sont précédement annuler on informe l'utilisateur
     * Sinon on demande une autorisation
     */
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            // Fournir une justification supplémentaire à l'utilisateur si l'autorisation n'a pas été accordée
            // et l'utilisateur bénéficierait d'un contexte supplémentaire pour l'utilisation de la permission.
            // Par exemple si l'utilisateur a précédemment refusé l'autorisation.

            Toast.makeText(getApplicationContext(), "L'autorisation de la caméra est nécessaire pour détecter un QRCode, activation dans les paramètres.", Toast.LENGTH_SHORT).show();
        } else {

            // Demande l'autorisation de la caméra
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    /**
     * Appel reçu lorsque la demande d'autorisation est terminée.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                qrcodeView();
            } else {
                Toast.makeText(getApplicationContext(), "Les autorisations n'ont pas été accordées.", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
package com.m1.lesbuteurs.smartbus.qrcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.m1.lesbuteurs.smartbus.R;

public class GenerateQRCode extends AppCompatActivity {

    ImageView qrCodeImageview;
    String QRcode;
    String qrcode;
    public final static int WIDTH = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode_view);
        getID();

        Intent content = getIntent();
        Bundle bundle = content.getExtras();

        if(bundle != null) {
            qrcode = (String) bundle.get("qrcode");
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                QRcode = qrcode;
                try {
                    synchronized (this) {
                        wait(10);
                        // runOnUiThread method used to do UI task in main thread.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Bitmap bitmap = null;
                                    bitmap = encodeAsBitmap(QRcode);
                                    qrCodeImageview.setImageBitmap(bitmap);
                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void getID() {
        qrCodeImageview=(ImageView) findViewById(R.id.img_qr_code_image);
    }

    // Création et retour de l'image QRCode en BItmap
    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Format non supporté
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                // pixels[offset + x] = result.get(x, y) ? ContextCompat.getColor(this, R.color.green):ContextCompat.getColor(this, R.color.transparency);
                pixels[offset + x] = result.get(x, y) ? ContextCompat.getColor(this, R.color.black) : ContextCompat.getColor(this, R.color.transparency);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}

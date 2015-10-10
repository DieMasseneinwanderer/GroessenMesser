package ch.einwanderer.massen.groessenmesser;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class FormActivity extends AppCompatActivity {

    private double upperAngel;
    private double lowerAngel;
    private double distance;
    private double size;

    private EditText upperEdit;
    private EditText lowerEdit;
    private EditText distanceEdit;
    private EditText sizeEdit;

    private final int SCAN_QR_CODE = 159;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Intent intent = getIntent();
        lowerAngel = intent.getDoubleExtra("lower", 0.0);
        upperAngel = intent.getDoubleExtra("upper", 0.0);

        upperEdit = (EditText) findViewById(R.id.upper);
        lowerEdit = (EditText) findViewById(R.id.lower);
        distanceEdit = (EditText) findViewById(R.id.distance);
        sizeEdit = (EditText) findViewById(R.id.size);

        upperEdit.setText(String.format("%.1f", upperAngel));
        lowerEdit.setText(String.format("%.1f", lowerAngel));

        distanceEdit.requestFocus();
        distanceEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    distance = Double.parseDouble(distanceEdit.getText().toString());
                    calculateSize();
                    sizeEdit.setText(String.format("%.1f", size));
                }
            }
        });

        Button logButton = (Button) findViewById(R.id.log);
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateSize();
                sizeEdit.setText(String.format("%.1f", size));

                Intent i = new Intent("com.google.zxing.client.android.SCAN");

                if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_ALL).isEmpty()) {
                    Toast.makeText(getApplicationContext(), "QR scan App not Installed", Toast.LENGTH_LONG).show();
                    return;
                }

                i.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(i, SCAN_QR_CODE);
            }
        });
    }

    private void calculateSize() {
        double betweenAngel = upperAngel - lowerAngel;
        double hypotenuse = distance / Math.sin(Math.toRadians(lowerAngel));
        double oppositeAngel = 180 - (betweenAngel + lowerAngel);
        size =  hypotenuse * Math.sin(Math.toRadians(betweenAngel)) / Math.sin(Math.toRadians(oppositeAngel));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == SCAN_QR_CODE && resultCode == RESULT_OK) {
            Intent logIntent = new Intent("ch.appquest.intent.LOG");

            if (getPackageManager().queryIntentActivities(logIntent, PackageManager.MATCH_ALL).isEmpty()) {
                Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
                return;
            }

            String logmessage = Double.toString(size);
            JSONObject jsonLog = new JSONObject();
            try {
                jsonLog.put("task", "Groessenmesser");
                jsonLog.put("object", resultIntent.getStringExtra("SCAN_RESULT"));
                jsonLog.put("height", logmessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            logIntent.putExtra("ch.appquest.logmessage", jsonLog.toString());
            startActivity(logIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

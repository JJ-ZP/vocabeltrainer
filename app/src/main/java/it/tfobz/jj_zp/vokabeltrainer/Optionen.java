package it.tfobz.jj_zp.vokabeltrainer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import it.tfobz.jj_zp.vokabeltrainer.R;

public class Optionen extends AppCompatActivity {

    private Switch erinnerungsSwitch;
    private Switch extraInfoSwitch;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optionen);

        erinnerungsSwitch = findViewById(R.id.erinnerungsSwitch);
        extraInfoSwitch = findViewById(R.id.extraInfoSwitch);
        resetButton = findViewById(R.id.dbResetButton);
        erinnerungsSwitch.setChecked(getPreferences(MODE_PRIVATE)
                .getBoolean(getString(R.string.erinnerungsID), false));
        extraInfoSwitch.setChecked(getSharedPreferences(getString(R.string.default_preference),MODE_PRIVATE)
                .getBoolean(getString(R.string.showExtraInformation), false));
        if(extraInfoSwitch.isChecked())
            resetButton.setVisibility(View.VISIBLE);
        else
            resetButton.setVisibility(View.INVISIBLE);
    }

    public void setErinnerungen(View view){
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(getString(R.string.erinnerungsID), erinnerungsSwitch.isChecked());
        editor.apply();

        if(erinnerungsSwitch.isChecked()){
            MyNotificationManager.checkDailyAt(this, 6, 30);
        }else{
            MyNotificationManager.cancelDaily(this);
        }
    }

    public void setExtraInfo(View view){
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.default_preference),MODE_PRIVATE).edit();
        editor.putBoolean(getString(R.string.showExtraInformation), extraInfoSwitch.isChecked());
        editor.apply();

        if(extraInfoSwitch.isChecked())
            resetButton.setVisibility(View.VISIBLE);
        else
            resetButton.setVisibility(View.INVISIBLE);
    }

    public void resetDatabase(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(Optionen.this);
        builder.setTitle("Datenbank wirklich zurücksetzen?");
        builder.setMessage("Sämtliche Daten gehen dabei verlohren!\n" +
                "Dieser Vorgang kann nicht rückgängig gemacht werden!");
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton("Zurücksetzen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                VokabeltrainerDB vokabeltrainerDB = VokabeltrainerDB.getInstance(Optionen.this);
                vokabeltrainerDB.loeschenTabellen();
                vokabeltrainerDB.erstellenTabellen();
                MyNotificationManager.cancelDaily(Optionen.this);
                if(erinnerungsSwitch.isChecked())
                    MyNotificationManager.checkDailyAt(Optionen.this, 6, 30);

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

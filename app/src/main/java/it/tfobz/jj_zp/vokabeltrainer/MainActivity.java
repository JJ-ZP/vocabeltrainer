package it.tfobz.jj_zp.vokabeltrainer;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }
}

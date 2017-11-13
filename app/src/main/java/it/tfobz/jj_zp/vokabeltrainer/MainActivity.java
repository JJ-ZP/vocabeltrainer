package it.tfobz.jj_zp.vokabeltrainer;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private VokabeltrainerDB vokabeltrainerDB;
    private RecyclerView recyclerView;
    private LernkarteiAdapter lernkarteiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vokabeltrainerDB = VokabeltrainerDB.getInstance(this);

        recyclerView = (RecyclerView) findViewById(R.id.LernkarteienList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lernkarteiAdapter = new LernkarteiAdapter(vokabeltrainerDB);
        recyclerView.setAdapter(lernkarteiAdapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Lernkartei wirklich löschen?");
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Löschen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int pos = viewHolder.getAdapterPosition();
                        vokabeltrainerDB.loeschenLernkartei(vokabeltrainerDB.getLernkarteien().get(pos).getNummer());
                        lernkarteiAdapter.notifyDataSetChanged();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                lernkarteiAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_options:
                intent = new Intent(this, Optionen.class);
                startActivity(intent);
                return true;
            case R.id.menu_about:
                intent = new Intent(this, Ueber.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    public void startAddLernKarteiDialog(final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View v = View.inflate(view.getContext(), R.layout.lernkartei_dialog, null);
        builder.setView(v);
        builder.setTitle("Lernkartei hinzufügen");
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                EditText nameTE = v.findViewById(R.id.lernkarteiName);
                EditText word1TE = v.findViewById(R.id.lernkarteiWort1);
                EditText word2TE = v.findViewById(R.id.lernkarteiWort2);
                EditText anzahlFaecherTE = v.findViewById(R.id.lernkarteiFaecher);

                Log.i("LLOG", nameTE.getText().toString() + " // " +
                       word1TE.getText().toString()  + " // " +
                       word2TE.getText().toString()  + " // " +
                       anzahlFaecherTE.getText().toString());

                Lernkartei lernkartei = new Lernkartei(-1, nameTE.getText().toString(),
                        word1TE.getText().toString(), word2TE.getText().toString(), false,
                        true);
                lernkartei.validiere();
                if(lernkartei.getFehler() == null){
                    VokabeltrainerDB vokabeltrainerDB = VokabeltrainerDB.getInstance(view.getContext());
                    if(vokabeltrainerDB.hinzufuegenLernkartei(lernkartei) == 0){
                        for(int k = 0; k < Integer.parseInt(anzahlFaecherTE.getText().toString()); k++){
                            vokabeltrainerDB.hinzufuegenFach(lernkartei.getNummer(),
                                    new Fach(-1, "Fach #"+k, (int) Math.pow(2, k), null));
                        }
                        lernkarteiAdapter.notifyDataSetChanged();
                        Toast.makeText(view.getContext(), "Lernkartei hinzugefügt", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(view.getContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(view.getContext(), "Ungültige Angabe", Toast.LENGTH_SHORT).show();
                }
                Log.i("LLOG", "Items: " +lernkarteiAdapter.getItemCount());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

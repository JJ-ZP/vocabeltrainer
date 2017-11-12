package it.tfobz.jj_zp.vokabeltrainer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class CardAdapter extends RecyclerView.Adapter {

    private VokabeltrainerDB db;
    private int lernkarteinummer;

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

        public ConstraintLayout mTextView;
        private RecyclerViewClickListener mListener;

        public ViewHolder(ConstraintLayout v, RecyclerViewClickListener listener) {
            super(v);
            mTextView = v;
            mListener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }
    }

    public CardAdapter(VokabeltrainerDB db, int lernkarteinummer){
        this.db = db;
        this.lernkarteinummer = lernkarteinummer;
    }

    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_view, parent, false);

        return new ViewHolder(v, new RecyclerViewClickListener() {
            @Override
            public void onClick(final View view, int position) {

                final Karte karte = db.getAllKarten(lernkarteinummer).get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final View v = View.inflate(view.getContext(), R.layout.card_dialog, null);

                EditText vorneTE = v.findViewById(R.id.cardVorne);
                EditText hintenTE = v.findViewById(R.id.cardHinten);
                Switch grossKleinTgl = v.findViewById(R.id.cardGrossKleinTgl);

                vorneTE.setText(karte.getWortEins());
                hintenTE.setText(karte.getWortZwei());
                Log.i("LLOG", "checked beim setzen? - " +karte.getGrossKleinschreibung());
                grossKleinTgl.setChecked(karte.getGrossKleinschreibung());

                builder.setView(v);
                builder.setTitle("Karte bearbeiten");
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText vorneTE = v.findViewById(R.id.cardVorne);
                        EditText hintenTE = v.findViewById(R.id.cardHinten);
                        Switch grossKleinTgl = v.findViewById(R.id.cardGrossKleinTgl);

                        karte.setWortEins(vorneTE.getText().toString());
                        karte.setWortZwei(hintenTE.getText().toString());
                        karte.grossKleinschreibung = grossKleinTgl.isChecked();

                        Log.i("LLOG", "checked? - " +grossKleinTgl.isChecked());

                        karte.validiere();
                        if(karte.getFehler() == null){
                            VokabeltrainerDB vokabeltrainerDB = VokabeltrainerDB.getInstance(view.getContext());
                            if(vokabeltrainerDB.aendernKarte(karte) == 0){
                                notifyDataSetChanged();
                                Toast.makeText(view.getContext(), "Karte gespeichert", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(view.getContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(view.getContext(), "Ungültige Angabe", Toast.LENGTH_SHORT).show();
                        }
                        Log.i("LLOG", "Items: " +getItemCount());
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView name = ((ViewHolder)holder).mTextView.findViewById(R.id.listeNamen);
        TextView beschreibung = ((ViewHolder)holder).mTextView.findViewById(R.id.listeBeschreibung);

        name.setText(db.getAllKarten(lernkarteinummer).get(position).getWortEins());
        beschreibung.setText(db.getAllKarten(lernkarteinummer).get(position).getWortZwei());
    }


    @Override
    public int getItemCount() {
        return db.getAllKarten(lernkarteinummer).size();
    }

}

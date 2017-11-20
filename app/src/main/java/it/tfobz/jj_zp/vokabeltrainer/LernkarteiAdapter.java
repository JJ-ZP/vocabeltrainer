package it.tfobz.jj_zp.vokabeltrainer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class LernkarteiAdapter extends RecyclerView.Adapter {

    private VokabeltrainerDB db;

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
        boolean onLongClick(View view , int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, View.OnLongClickListener {

        public ConstraintLayout mTextView;
        private RecyclerViewClickListener mListener;

        public ViewHolder(ConstraintLayout v, RecyclerViewClickListener listener) {
            super(v);
            mTextView = v;
            mListener = listener;
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            return mListener.onLongClick(view , getAdapterPosition());
        }
    }

    public LernkarteiAdapter(VokabeltrainerDB db){
        this.db = db;
    }

    @Override
    public LernkarteiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_view, parent, false);

        return new ViewHolder(v, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                int lernkarteiNummer = db.getLernkarteien().get(position).getNummer();
                Intent intent = new Intent(view.getContext(), LernkarteiActivity.class);
                intent.putExtra("it.tfobz.jj_zp.vokabeltrainer.KarteiId", lernkarteiNummer);
                view.getContext().startActivity(intent);
            }

            @Override
            public boolean onLongClick(View view, int position) {
                final int lernkarteiNummer = db.getLernkarteien().get(position).getNummer();
                final Lernkartei lernkartei = db.getLernkartei(lernkarteiNummer);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final View v = View.inflate(view.getContext(), R.layout.lernkartei_dialog, null);

                final EditText name = v.findViewById(R.id.lernkarteiName);
                final EditText wortEinsBeschreibung = v.findViewById(R.id.lernkarteiWort1);
                final EditText wortZweiBeschreibung = v.findViewById(R.id.lernkarteiWort2);
                final EditText anzahlFaecher = v.findViewById(R.id.lernkarteiFaecher);

                final boolean richtung  = lernkartei.getRichtung();
                final boolean großKleinSchreibung = lernkartei.getGrossKleinschreibung();

                v.findViewById(R.id.warningsymbol).setVisibility(View.VISIBLE);
                v.findViewById(R.id.warningtext).setVisibility(View.VISIBLE);

                name.setText(lernkartei.getBeschreibung());
                wortEinsBeschreibung.setText(lernkartei.getWortEinsBeschreibung());
                wortZweiBeschreibung.setText(lernkartei.getWortZweiBeschreibung());

                builder.setView(v);
                builder.setTitle("Lernkartei bearbeiten");

                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i("LLOG","Abbrechen");
                    }
                });

                builder.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i("LLOG","Speichern");
                        ArrayList<Karte> karten = db.getAllKarten(lernkarteiNummer);
                        db.loeschenLernkartei(lernkarteiNummer);
                        Lernkartei lernkartei = new Lernkartei(-1,name.getText().toString(),
                                wortEinsBeschreibung.getText().toString(),wortZweiBeschreibung.getText().toString(),
                                richtung,großKleinSchreibung);

                        db.hinzufuegenLernkartei(lernkartei);

                        for(int k = 0; k < Integer.parseInt(anzahlFaecher.getText().toString()); k++){
                            db.hinzufuegenFach(lernkartei.getNummer(),
                                    new Fach(-1, "Fach #"+k, (int) Math.pow(2, k), null));
                        }

                        for (Karte karte : karten) {
                            db.hinzufuegenKarte(lernkartei.getNummer(),new Karte(-1,karte.getWortEins(),karte.getWortZwei(),
                                    karte.getRichtung(),karte.getGrossKleinschreibung()));
                        }

                        LernkarteiAdapter.this.notifyDataSetChanged();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView name = ((ViewHolder)holder).mTextView.findViewById(R.id.listeNamen);
        TextView beschreibung = ((ViewHolder)holder).mTextView.findViewById(R.id.listeBeschreibung);
        ImageView image = ((ViewHolder)holder).mTextView.findViewById(R.id.learnNotification);

        if(!db.getAllZuLernendeKarten(db.getLernkarteien().get(position).getNummer()).isEmpty()){
            image.setVisibility(View.VISIBLE);
        }

        name.setText(db.getLernkarteien().get(position).toString());
        beschreibung.setText(db.getLernkarteien().get(position).getWortEinsBeschreibung() + " - " +
                db.getLernkarteien().get(position).getWortZweiBeschreibung());
    }

    @Override
    public int getItemCount() {
        return db.getLernkarteien() != null ? db.getLernkarteien().size() : 0;
    }

}

package it.tfobz.jj_zp.vokabeltrainer;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class LernkarteiAdapter extends RecyclerView.Adapter {

    private VokabeltrainerDB db;

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
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView name = ((ViewHolder)holder).mTextView.findViewById(R.id.listeNamen);
        TextView beschreibung = ((ViewHolder)holder).mTextView.findViewById(R.id.listeBeschreibung);
        ImageView image = ((ViewHolder)holder).mTextView.findViewById(R.id.learnNotification);

        if(db.getLernkarteienErinnerung().contains(db.getLernkarteien().get(position))){
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

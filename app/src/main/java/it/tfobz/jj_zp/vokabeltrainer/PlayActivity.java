package it.tfobz.jj_zp.vokabeltrainer;

import android.app.Fragment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PlayActivity extends AppCompatActivity {

    public static class CardFrontFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.card_front, container, false);
        }
    }

    public static class CardBackFragment extends Fragment {

        public String text = "Ooops, etwas ist schiefgelaufen!";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.card_back, container, false);
            TextView txtView = v.findViewById(R.id.textKarteHinten);
            txtView.setText(text);
            return v;
        }
    }


    private VokabeltrainerDB vokabeltrainerDB;
    private int lernkarteinummer;
    private List<Fach> listeZuLernenderFaecher;
    private TextView wortEinsFeld;
    private TextView wortZweiFeld;
    private CardBackFragment cardBackFragment;
    private boolean allesGelerntFuerHeute;
    private Karte aktuelleKarte;

    private Button knopf1;
    private Button knopf2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vokabeltrainerDB = VokabeltrainerDB.getInstance(this);
        lernkarteinummer = getIntent().getIntExtra("it.tfobz.jj_zp.vokabeltrainer.KarteiId", lernkarteinummer);
        getSupportActionBar().setTitle(vokabeltrainerDB.getLernkartei(lernkarteinummer).toString() + " - Play");

        wortEinsFeld = findViewById(R.id.textKarteVorne);
        knopf1 = findViewById(R.id.gewusst_knopf);
        knopf2 = findViewById(R.id.nicht_gewusst_knopf);

        //TODO - START: korrigieren
        listeZuLernenderFaecher = vokabeltrainerDB.getFaecherErinnerung(lernkarteinummer);
        if(listeZuLernenderFaecher.isEmpty()){
            Log.i("LLOG", "alles gelernt fuer Heute");
            listeZuLernenderFaecher = vokabeltrainerDB.getFaecher(lernkarteinummer);
            allesGelerntFuerHeute = true;
        }
        //TODO - END

        getNextCard();
    }

    public void getNextCard(){
        int id = (int)(Math.random() * listeZuLernenderFaecher.size());
        //TODO: nur karten lesen die noch gelernt werden müssen und erst dann zufällige karten lesen
        //TODO: sobald alles für heute in dieser Lernkartei gelernt wurde und auch dann bessere Kartenauswahl
        aktuelleKarte = vokabeltrainerDB.getZufaelligeKarte(lernkarteinummer, listeZuLernenderFaecher
                .get(id).getNummer());

        while(aktuelleKarte == null){
            listeZuLernenderFaecher.remove(id);
            if(listeZuLernenderFaecher.isEmpty() && !allesGelerntFuerHeute){
                listeZuLernenderFaecher = vokabeltrainerDB.getFaecher(lernkarteinummer);
                allesGelerntFuerHeute = true;
            }else if(listeZuLernenderFaecher.isEmpty() && allesGelerntFuerHeute){
                Toast.makeText(this, "Keine Karten zum Lernen", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            id = (int)(Math.random() * listeZuLernenderFaecher.size());
            aktuelleKarte = vokabeltrainerDB.getZufaelligeKarte(lernkarteinummer, listeZuLernenderFaecher
                    .get(id).getNummer());
        }

        cardBackFragment = new CardBackFragment();
        cardBackFragment.text = aktuelleKarte.getWortZwei();
        getFragmentManager().beginTransaction().add(R.id.untereKarteLayout, cardBackFragment).commit();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flips_in_right,
                        R.animator.card_flips_out_right,
                        R.animator.card_flips_in_left,
                        R.animator.card_flips_out_left)
                .replace(R.id.untereKarteLayout, new CardFrontFragment())
                .addToBackStack(null)
                .commit();

        wortEinsFeld.setText(aktuelleKarte.getWortEins());

        knopf1.setVisibility(View.INVISIBLE);
        knopf2.setVisibility(View.INVISIBLE);

    }

    public void gewusst(View view){
        if (!allesGelerntFuerHeute)
            vokabeltrainerDB.setKarteRichtig(aktuelleKarte);
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.untereKarteLayout)).commit();
        getNextCard();
    }

    public void nichtGewusst(View view){
        if (!allesGelerntFuerHeute)
            vokabeltrainerDB.setKarteFalsch(aktuelleKarte);
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.untereKarteLayout)).commit();
        getNextCard();
    }

    public void flipCard(View view){
        getFragmentManager().popBackStack();

        knopf1.setVisibility(View.VISIBLE);
        knopf2.setVisibility(View.VISIBLE);
    }
}

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

import java.util.ArrayList;
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
    private List<Fach> listeAllerFaecher;
    private List<Karte> listeZuLernenderKarten;
    private List<Karte> listeAllerKarten;
    private TextView wortEinsFeld;
    private TextView wortZweiFeld;
    private TextView infoText;
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
        infoText = findViewById(R.id.infoTextPlay);
        knopf1 = findViewById(R.id.gewusst_knopf);
        knopf2 = findViewById(R.id.nicht_gewusst_knopf);

        if(!getSharedPreferences(getString(R.string.default_preference),MODE_PRIVATE).getBoolean(getString(R.string.showExtraInformation), false)) {
            infoText.setVisibility(View.INVISIBLE);
            Log.i("LLOG", "Zusatzinfos deaktiviert");
        } else
            infoText.setVisibility(View.VISIBLE);

        //TODO - START: korrigieren
        //listeZuLernenderFaecher = vokabeltrainerDB.getFaecherErinnerung(lernkarteinummer);
        listeZuLernenderFaecher = new ArrayList<>();
        listeAllerFaecher = vokabeltrainerDB.getFaecher(lernkarteinummer);
        listeZuLernenderKarten = vokabeltrainerDB.getAllZuLernendeKarten(lernkarteinummer);
        listeAllerKarten = vokabeltrainerDB.getAllKarten(lernkarteinummer);
        allesGelerntFuerHeute = listeZuLernenderKarten.isEmpty();

        //TODO - END

        getNextCard();
    }

    public void getNextCard(){
        if(listeAllerKarten.isEmpty()){
            Toast.makeText(this, "Keine Karten vorhanden!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(!allesGelerntFuerHeute){
            if(listeZuLernenderKarten.isEmpty()) {
                allesGelerntFuerHeute = true;
                for (Fach f : listeZuLernenderFaecher) {
                    int ret = vokabeltrainerDB.setGelerntFach(f.getNummer());
                    Log.i("LLOG", "zuletzt gelernt geändert bei: "+ret);
                }
                Toast.makeText(this, "Alles gelernt für heute!", Toast.LENGTH_SHORT).show();
            }else{
                aktuelleKarte = listeZuLernenderKarten.get(0);
                listeZuLernenderFaecher.add(
                        vokabeltrainerDB.getFach(vokabeltrainerDB.getFachnummer(aktuelleKarte)));
                listeZuLernenderKarten.remove(0);
            }
        }

        if(allesGelerntFuerHeute){
            //TODO: make it less random so that it appears more random
            int id = (int) (Math.random() * listeAllerKarten.size());
            aktuelleKarte = listeAllerKarten.get(id);
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
        infoText.setText("Groß/Kleinschreibung beachten: "+ aktuelleKarte.getGrossKleinschreibung()+"\n\nFachnummer: "+
                vokabeltrainerDB.getFachnummer(aktuelleKarte) +
                "\nabfragen alle "+
                vokabeltrainerDB.getFach(vokabeltrainerDB.getFachnummer(aktuelleKarte))
                        .getErinnerungsIntervall() + " Tage\nzuletzt gelernt am "+
                vokabeltrainerDB.getFach(vokabeltrainerDB.getFachnummer(aktuelleKarte))
                        .getGelerntAmEuropaeischString()+"\n\nEndlosmodus: "+allesGelerntFuerHeute);

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

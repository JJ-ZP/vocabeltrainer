package it.tfobz.jj_zp.vokabeltrainer.grafik;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.tfobz.jj_zp.vokabeltrainer.R;

/**
 * Created by jonas on 12.11.17.
 */

public class BackLayoutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_back, container, false);
    }
}

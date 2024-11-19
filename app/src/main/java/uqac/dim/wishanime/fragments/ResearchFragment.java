package uqac.dim.wishanime.fragments;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

import uqac.dim.wishanime.BoutonAsynchrone;
import uqac.dim.wishanime.R;
import uqac.dim.wishanime.persistance.AnimeDb;

public class ResearchFragment extends Fragment {

    // https://jikan.moe/
    // https://api.jikan.moe/v4/anime?q={anime}
    // https://api.jikan.moe/v4/anime/{id}
    private static ResearchFragment INSTANCE;
    private String url_api = "https://api.jikan.moe/v4/anime?q=";
    private ArrayList<LinearLayout> preview;

    public static ResearchFragment getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE =  new ResearchFragment();
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_research, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayout gridLay = getView().findViewById(R.id.grid_anime);
        preview = new ArrayList<>();
        if(gridLay.getChildCount() != 0)
        {
            Log.i("DIM", String.valueOf(gridLay.getChildCount()));
        }

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }

        EditText edittext = getView().findViewById(R.id.edit_search_name);

        getView().findViewById(R.id.btn_search)
                .setOnClickListener(view1 -> {

                        //Ajout de nom de l'anime en fin de l'url
                        url_api += edittext.getText();

                        BoutonAsynchrone async = new BoutonAsynchrone(getContext(), this);
                        async.execute(url_api);
                        url_api = url_api.substring(0,url_api.length()-(edittext.getText().length()));
                         });
    }

    public void addLinear(LinearLayout line)
    {
        preview.add(line);
    }
}
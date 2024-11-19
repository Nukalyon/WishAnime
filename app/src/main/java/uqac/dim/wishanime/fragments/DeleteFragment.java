package uqac.dim.wishanime.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import uqac.dim.wishanime.MainActivity;
import uqac.dim.wishanime.R;
import uqac.dim.wishanime.persistance.Anime;
import uqac.dim.wishanime.persistance.AnimeDb;

public class DeleteFragment extends Fragment {

    private static DeleteFragment INSTANCE;
    private Context context;
    private AnimeDb adb;
    private List<Anime> animes;
    private boolean thread_en_cours = false;

    public DeleteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adb = AnimeDb.getDatabase(getContext());
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lecturedb(context);
    }

    public static DeleteFragment getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE = new DeleteFragment();
        }
        return INSTANCE;
    }

    private void lecturedb(Context context) {
        Thread th = new Thread(() -> {
            animes = adb.animeDao().getAllAnimes();

            for (int i = 0; i < animes.size(); i++)
            {
                AsyncUpdateGrid async = new AsyncUpdateGrid(this, context);
                async.execute(animes.get(i));
            }
        });
        th.start();
    }

    private class AsyncUpdateGrid extends AsyncTask<Anime, Void, Bitmap>
    {
        private Context context;
        private DeleteFragment fragment;
        private int maxHeight;
        private  int maxWidth;

        final float ratio = 16 / 9;

        Anime current;
        GridLayout db_grid;

        public AsyncUpdateGrid(DeleteFragment deleteFragment, Context context)
        {
            this.context = context;
            this.fragment = deleteFragment;
        }

        @Override
        protected Bitmap doInBackground(Anime... anime)
        {
            current = anime[0];

            //Téléchargement de l'image
            Bitmap image = null;
            try
            {
                URL download_image = new URL(current.img);
                image = BitmapFactory.decodeStream(download_image.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            db_grid = getView().findViewById(R.id.home_anime_grid);
            db_grid.setColumnCount(MainActivity.columns);

            this.maxWidth = (db_grid.getWidth()/db_grid.getColumnCount()) - 2;
            this.maxHeight = (int) (maxWidth * ratio);

            //Crée l'affichage
            LinearLayout temp_lay = new LinearLayout(context);
            temp_lay.setOrientation(LinearLayout.VERTICAL);

            int maxWidth = (db_grid.getWidth()/db_grid.getColumnCount()) - 2;
            float ratio = 16 / 9;
            int maxHeight = (int) (maxWidth * ratio);

            //Création du textview avec le nom de l'anime
            TextView txt = setupTextView(context);

            //Assignation de l'image téléchargée dans l'imageview
            ImageView imageView = setupImageView(context, bitmap);

            //AJout dans les éléments respectifs
            temp_lay.addView(imageView);
            temp_lay.addView(txt);

            db_grid.addView(temp_lay);
        }

        private ImageView setupImageView(Context context, Bitmap bitmap) {
            ImageView res = new ImageView(context);
            res.setImageBitmap(bitmap);
            res.setMaxWidth(maxWidth);
            res.setMaxHeight(maxHeight);
            res.setOnClickListener(v -> {
                //Donne le linear
                LinearLayout linear = (LinearLayout) v.getParent();
                GridLayout grider = (GridLayout) linear.getParent();
                //Recherche de la position de l'enfant
                for(int i = 0; i < grider.getChildCount(); i++)
                {
                    if(linear == grider.getChildAt(i))
                    {
                        //Suppression dans la bDdd locale
                        Log.d("DIM", "Debut suppression");
                        String temp_name = current.name;
                        execDeleteAnime(adb, i);
                        grider.removeViewAt(i);
                        Toast notifier = new Toast(context);
                        notifier.setText("Vous avez supprimé l'anime : " + temp_name);
                        notifier.setDuration(Toast.LENGTH_SHORT);
                        notifier.show();
                    }
                }
            });
            return res;
        }

        private TextView setupTextView(Context context) {
            TextView res = new TextView(context);
            res.setText(current.name);
            res.setWidth(maxWidth);
            res.setHeight(maxHeight);
            res.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            return res;
        }

        private void execDeleteAnime(AnimeDb adb, int i)   {
            Thread th = new Thread(() -> adb.animeDao().delete(animes.get(i)));
            th.start();
            try {
                th.join();
                animes.remove(i);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
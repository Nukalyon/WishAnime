package uqac.dim.wishanime.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import uqac.dim.wishanime.MainActivity;
import uqac.dim.wishanime.R;
import uqac.dim.wishanime.persistance.Anime;
import uqac.dim.wishanime.persistance.AnimeDb;

public class HomeFragment extends Fragment {

    public static HomeFragment INSTANCE;
    private Context context;
    private AnimeDb adb;
    private List<Anime> animes;
    BottomSheetDialog dialog;
    int animeLongClick;
    GridLayout grid;
    TextView name;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adb = AnimeDb.getDatabase(getContext());
        context = getContext();
        dialog = new BottomSheetDialog(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        grid = getView().findViewById(R.id.home_anime_grid);
        lecturedb(context);

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

    public static HomeFragment getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE =  new HomeFragment();
        }
        return INSTANCE;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        LinearLayout linear = (LinearLayout) v.getParent();
        name = (TextView) linear.getChildAt(1);
        for(int i = 0; i < grid.getChildCount(); i++)
        {
            if(grid.getChildAt(i) == linear)
            {
                animeLongClick = i;
            }
        }
        getActivity().getMenuInflater().inflate(R.menu.favorite, menu);
        if(animes.get(animeLongClick).favoris)
        {
            menu.findItem(R.id.addFavoris).setVisible(false);
            menu.findItem(R.id.supFavoris).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.addFavoris).setVisible(true);
            menu.findItem(R.id.supFavoris).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.addFavoris:
                animes.get(animeLongClick).favoris = true;
                StringBuilder str = new StringBuilder(name.getText());
                str.insert(0, (char) 0x272E);
                name.setText(str);
                break;

            case R.id.supFavoris:
                animes.get(animeLongClick).favoris = false;
                name.setText(name.getText().subSequence(1, name.getText().length()));
                break;
        }
        //Thread pour accéder à la base donnée pour modifier le champ favoris
        Thread th = new Thread(() -> {
            adb.animeDao().updateAnime(animes.get(animeLongClick));
        });
        th.start();
        Log.i("DIM", String.valueOf(animes.get(animeLongClick).favoris));
        return true;
    }


    private class AsyncUpdateGrid extends AsyncTask<Anime, Void, Bitmap>
    {
        private Context context;
        private HomeFragment fragment;
        private int maxHeight;
        private  int maxWidth;

        final float ratio = 16 / 9;

        Anime current;
        GridLayout db_grid;

        public AsyncUpdateGrid(HomeFragment homeFragment, Context context)
        {
            this.context = context;
            this.fragment = homeFragment;
        }

        @Override
        protected Bitmap doInBackground(Anime... anime)
        {
            current = anime[0];

            //Création de l'affiche et téléchargement de l'image
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
            //temp_lay.setLayoutParams(paramsLinear);

            //Création du textview avec le nom de l'anime
            TextView txt = setupTextView(context);

            //Assignation de l'image téléchargée dans l'imageview
            ImageView imageView = setupImageView(context, bitmap);

            //AJout dans les éléments respectifs
            temp_lay.addView(imageView);
            temp_lay.addView(txt);
            //frame.addView(star);
            db_grid.addView(temp_lay);
        }

        private ImageView setupImageView(Context context, Bitmap bitmap) {
            ImageView res = new ImageView(context);
            res.setImageBitmap(bitmap);
            res.setMaxWidth(maxWidth);
            res.setMaxHeight(maxHeight);
            res.setOnClickListener(v -> {
                Log.i("DIM", String.valueOf(current.favoris));
                //Donne le linear
                LinearLayout linear = (LinearLayout) v.getParent();
                GridLayout grider = (GridLayout) linear.getParent();
                //Recherche de la position de l'enfant
                for(int i = 0; i < grider.getChildCount(); i++)
                {
                    if(linear == grider.getChildAt(i))
                    {
                        dialog.setContentView(R.layout.alertdialog_animeview);
                        setupDialog(dialog, animes.get(i), bitmap);
                        dialog.show();
                    }
                }
            });
            registerForContextMenu(res);
            return res;
        }



        private TextView setupTextView(Context context) {
            TextView res = new TextView(context);
            res.setText(current.name);
            res.setWidth(maxWidth);
            res.setHeight(maxHeight);
            res.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            if(current.favoris)
            {
                StringBuilder str = new StringBuilder(res.getText());
                str.insert(0, (char) 0x272E);
                res.setText(str);
            }
            return res;
        }

        private void setupDialog(Dialog dialog, Anime anime, Bitmap bitmap) {

            //Setup de l'image de l'anime
            ImageView img_anime = dialog.findViewById(R.id.img_anime);
            img_anime.setImageBitmap(bitmap);

            //Setup du nom de l'anime
            TextView title = dialog.findViewById(R.id.title_anime);
            title.setText(anime.name);

            //Setup du nombre d'episodes
            TextView episodes = dialog.findViewById(R.id.episodes_anime);
            episodes.setText("Number of episodes:\n" + anime.episodes.toString());

            //Setup de la durée
            TextView duration = dialog.findViewById(R.id.duration_anime);
            duration.setText("Duration:\n" + anime.duration);

            //Setup du synopsis
            TextView synopsis = dialog.findViewById(R.id.synopsis_anime);
            synopsis.setText("Synopsis:\n" + anime.synopsis);

            //Setup du listener du bouton share
            ImageButton img_btn = dialog.findViewById(R.id.btn_share);
            img_btn.setOnClickListener(v -> {
                /*
                Log.i("DIM", "cliqué");
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("NAME", name_anime.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Nom copié dans le presse-papier", Toast.LENGTH_SHORT).show();
                 */
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,
                        "Viens voir " + anime.name + " avec moi !\n" +
                        "Je t'envoie même le lien tiens !\n" + anime.url +
                        "\nAllez à tout de suite !");
                intent.setType("text/plain");
                context.startActivity(Intent.createChooser(intent, "Send To"));
            });
        }
    }
}
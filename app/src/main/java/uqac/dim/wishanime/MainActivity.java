package uqac.dim.wishanime;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import uqac.dim.wishanime.fragments.DeleteFragment;
import uqac.dim.wishanime.fragments.HomeFragment;
import uqac.dim.wishanime.fragments.ResearchFragment;
import uqac.dim.wishanime.persistance.AnimeDb;

public class MainActivity extends AppCompatActivity
{

    private HomeFragment home;
    private ResearchFragment research;
    private DeleteFragment delete;
    public static final int columns = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        home = HomeFragment.getInstance();
        delete = DeleteFragment.getInstance();
        research = ResearchFragment.getInstance();

        AnimeDb adb = AnimeDb.getDatabase(getApplicationContext());
        // suppression de tous les animes de la bd pour les tests
        //startRunnable(adb);
        if(savedInstanceState == null)
        {
            openFragment(home);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    openFragment(home);
                    return true;
                case R.id.navigation_research:
                    openFragment(research);
                    return true;
                case R.id.navigation_delete:
                    openFragment(delete);
                    return true;
            }
            return false;
        });
    }

    private void startRunnable(AnimeDb adb)
    {
        class Temp implements Runnable
        {
            @Override
            public void run() {
                adb.animeDao().deleteAnime();
            }
        }
        Thread th = new Thread(new Temp());
        th.start();
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //this is a helper class that replaces the container with the fragment. You can replace or add fragments.
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.commit(); // commit() performs the action
    }
}
package uqac.dim.wishanime.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Anime.class}, version = 7, exportSchema = false)
public abstract class AnimeDb extends RoomDatabase {

    private static AnimeDb INSTANCE;
    public abstract AnimeDao animeDao();

    public static AnimeDb getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, AnimeDb.class, "animedatabase")
                            //recreate the database if necessary
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}

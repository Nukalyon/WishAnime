package uqac.dim.wishanime.persistance;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AnimeDao
{
    @Query("SELECT * FROM anime")
    List<Anime> getAllAnimes();

    @Query("SELECT * FROM anime WHERE id = :id LIMIT 1")
    Anime findById(int id);

    @Query("delete from anime")
    void deleteAnime();

    @Query("SELECT * FROM anime WHERE favoris = 1")
    List<Anime> getFavoris();

    @Insert
    void insertAll(Anime... animes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addAnime(Anime anime);

    @Delete
    void delete(Anime anime);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAnime(Anime anime);
    /*
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void setFavoris(Anime anime, boolean favoris);
     */
}
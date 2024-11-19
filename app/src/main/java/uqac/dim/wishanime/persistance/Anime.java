package uqac.dim.wishanime.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Anime
{
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "synopsis")
    public String synopsis;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "episodes")
    public Integer episodes;

    @ColumnInfo(name = "duration")
    public String duration;

    @ColumnInfo(name = "img_url")
    public String img;

    @ColumnInfo(name = "favoris")
    public boolean favoris;

    @ColumnInfo(name = "url")
    public String url;

    public Anime(int id, String name, Integer episodes, String duration, String synopsis, String img, boolean favoris, String url)
    {
        this.id = id;
        this.name = name;
        this.synopsis = synopsis;
        this.episodes = episodes;
        this.duration = duration;
        this.img = img;
        this.favoris = favoris;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Anime{" +
                "id=" + id +
                ", synopsis='" + synopsis + '\'' +
                ", name='" + name + '\'' +
                ", episodes=" + episodes +
                ", duration='" + duration + '\'' +
                ", img='" + img + '\'' +
                '}';
    }
}

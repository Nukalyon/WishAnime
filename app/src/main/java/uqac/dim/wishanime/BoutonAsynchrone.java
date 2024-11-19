package uqac.dim.wishanime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import uqac.dim.wishanime.fragments.ResearchFragment;
import uqac.dim.wishanime.persistance.Anime;
import uqac.dim.wishanime.persistance.AnimeDb;

public class BoutonAsynchrone extends AsyncTask<String, Void, JSONArray>
{
    private Context context;
    private ArrayList<Anime> requested;
    private AnimeDb adb;
    private ResearchFragment resfrag;

    HttpURLConnection connection;
    BufferedReader reader;
    String line;
    StringBuffer reponseContent;
    GridLayout grid;


    public BoutonAsynchrone(Context context, ResearchFragment resfrag)
    {
        this.context = context;
        this.adb = AnimeDb.getDatabase(context);
        this.resfrag = resfrag;
    }

    @Override
    protected JSONArray doInBackground(String... strings)
    {
        JSONArray data_infos = null;
        reponseContent = new StringBuffer();

        // Requete http vers l'url en type GET
        try {
            URL new_url = new URL(strings[0]);
            connection = (HttpURLConnection) new_url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            //Timeout of the connection after 5000 ms
            connection.setConnectTimeout(5000);


            //Réponse du server
            int status = connection.getResponseCode();
            switch (status) {
                case 200: {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while((line = reader.readLine()) != null)
                    {
                        reponseContent.append(line);
                    }
                    reader.close();

                    JSONObject all_infos = new JSONObject(reponseContent.toString());
                    data_infos = new JSONArray(all_infos.getJSONArray("data").toString());
                    break;
                }
                case 304: {
                    Toast.makeText(context, "You have the latest data (Cache Validation response)", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 400: {
                    Toast.makeText(context, "You've made an invalid request. Recheck documentation", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 404: {
                    Toast.makeText(context, "The resource was not found or MyAnimeList responded with a 404", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 405: {
                    Toast.makeText(context, "Requested Method is not supported for resource. Only GET requests are allowed", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 429: {
                    Toast.makeText(context, "You are being rate limited by Jikan or MyAnimeList is rate-limiting our servers (specified in the error response)", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 500: {
                    Toast.makeText(context, "Something is not working on our end. If you see an error response with a report_url URL, please click on it to open an auto-generated GitHub issue", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 503: {
                    Toast.makeText(context, "The service has broke.", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
            Log.i("DIM", "Disconnected");
        }

        return data_infos;
    }

    @Override
    protected void onPostExecute(JSONArray data_infos) {
        super.onPostExecute(data_infos);
        requested = new ArrayList<>();
        //Récupération du ScrollView
        //grid = ((Activity)context).findViewById(R.id.grid_anime);
        grid = resfrag.getView().findViewById(R.id.grid_anime);
        //Setter du nombre de colonnes
        grid.setColumnCount(MainActivity.columns);
        //Reset des Affiches
        grid.removeAllViews();

        for (int l = 0; l < data_infos.length(); l++)
        {
            ArrayList<Object> infos = new ArrayList<>();
            infos = extractionInfos(data_infos, l);
            try {
                //Extraction de l'url de l'image
                String url_image = data_infos.getJSONObject(l)
                        .getJSONObject("images")
                        .getJSONObject("jpg")
                        .getString("image_url");

                //Ajout dans un ImageView
                ImageView imageView = new ImageView(context);
                imageView.setOnClickListener(v -> {
                    //Donne le linear
                    LinearLayout linear = (LinearLayout) v.getParent();
                    GridLayout grider = (GridLayout) linear.getParent();
                    //Recherche de la position de l'enfant
                    for(int i = 0; i < grider.getChildCount(); i++)
                    {
                        if(linear == grider.getChildAt(i))
                        {
                            startRunnable(adb, requested, i);
                        }
                    }
                });
                int maxWidth = (grid.getWidth()/grid.getColumnCount()) - 2;
                float ratio = 16 / 9;
                int maxHeight = (int) (maxWidth * ratio);
                imageView.setMaxWidth(maxWidth);
                imageView.setMaxHeight(maxHeight);


                try
                {
                    URL download_image = new URL(url_image);
                    Bitmap image = BitmapFactory.decodeStream(download_image.openConnection().getInputStream());
                    imageView.setImageBitmap(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Ajout dans un TextView
                TextView textView = new TextView(context);
                textView.setWidth(maxWidth);
                textView.setHeight(maxHeight);
                textView.setText(infos.get(1).toString());
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                //Ajout dans le Linearlayout
                LinearLayout linear = new LinearLayout(context);
                linear.setOrientation(LinearLayout.VERTICAL);
                linear.addView(imageView);
                linear.addView(textView);

                //Ajout dans la liste des Animes
                int id = ((Integer) infos.get(0));
                String title = ((String)infos.get(1));
                int ep = ((Integer)infos.get(2));
                String dura = ((String)infos.get(3));
                String syno = ((String)infos.get(4));
                String img = ((String)infos.get(5));
                String url = ((String)infos.get(6));

                Anime anime = new Anime(id, title, ep, dura, syno, img, false, url);
                requested.add(anime);

                resfrag.addLinear(linear);
                //Ajout dans le GridLayout
                grid.addView(linear, l);
                //resfrag.updateGridLayout(linear);
                infos.clear();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void startRunnable(AnimeDb adb, ArrayList<Anime> requested, int i)
    {
        Thread th = new Thread(() -> adb.animeDao().addAnime(requested.get(i)));
        th.start();
    }

    private ArrayList<Object> extractionInfos(JSONArray data_infos, int l)
    {
        ArrayList<Object> info_posL = new ArrayList<>();
        try
        {
            int id = data_infos.getJSONObject(l).getInt("mal_id");
            String name = data_infos.getJSONObject(l).getString("title");
            int episodes;
            if(data_infos.getJSONObject(l).getString("episodes") == "null")
            {
                episodes = 0;
            }
            else
            {
                episodes = data_infos.getJSONObject(l).getInt("episodes");
            }
            String duration = data_infos.getJSONObject(l).getString("duration");
            String synopsis = data_infos.getJSONObject(l).getString("synopsis");

            String img_url = data_infos.getJSONObject(l)
                    .getJSONObject("images")
                    .getJSONObject("jpg")
                    .getString("image_url");

            String watch = data_infos.getJSONObject(l).getString("url");
            watch += "/episode";

            info_posL.add(id);
            info_posL.add(name);
            info_posL.add(episodes);
            info_posL.add(duration);
            info_posL.add(synopsis);
            info_posL.add(img_url);
            info_posL.add(watch);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return info_posL;
    }
}

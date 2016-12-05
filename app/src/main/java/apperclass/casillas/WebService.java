package apperclass.casillas;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by chao on 04/12/2016.
 */

public class WebService extends AsyncTask<String, String, String> {
    private final String IP = "http://chendam2.esy.es/Casillas";
    private final String GET_BY_ID = IP + "/get_scores_by_id.php";
    private final String INSERT = IP + "/insert_score.php";
    private final String DELETE = IP + "/delete_score_by_all_params.php";
    private final String UPDATE = IP + "/update_score.php";

    URL url;

    public WebService() {
        super();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String cadena = "";
        String id = params[1];
        String score = params[2];
        String lattitude = params[3];
        String longitude = params[4];
        try {
            switch (params[0]) {
                case "1":
                    cadena = UPDATE;
                    break;
                case "2":
                    cadena = INSERT;
                    break;
                case "3":
                    cadena = DELETE;
                    break;
            }
            switch (params[0]) {
                case "1":
                case "2":
                case "3":
                    url = new URL(cadena);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("idUser", id);
                    jsonParam.put("lattitude", lattitude);
                    jsonParam.put("longitude", longitude);
                    jsonParam.put("score", score);

                    OutputStream output = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
                    writer.write(jsonParam.toString());
                    writer.flush();
                    writer.close();

                    int respuesta = connection.getResponseCode();


                    StringBuilder result = new StringBuilder();

                    if (respuesta == HttpURLConnection.HTTP_OK) {

                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            result.append(line);
                        }
                    }
                    break;
            }
        }
        catch (MalformedURLException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        catch (JSONException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }
}
package apperclass.casillas;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Points.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Points extends Fragment {

    private OnFragmentInteractionListener mListener;

    ListView points;
    WebService webService;
    ArrayAdapter<String> adapter;
    Thread thread;
    String user;

    public Points() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_points, container, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        user = preferences.getString("user", null);
        points = (ListView) view.findViewById(R.id.lvpuntuaciones);
        points.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity()).
                        setTitle("Borrar puntuación").
                        setMessage("¿Estás seguro de querer borrar la puncuación?").
                        setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                webService = new WebService();
                                String[] parts = ((TextView)view).getText().toString().split("\n");
                                webService.execute("3", user, parts[2].split(" ")[1], parts[0].split(" ")[1], parts[1].split(" ")[1]);
                                fillListView();
                            }
                        }).setNegativeButton("Cancelar", null);
                alert.show();
                return true;
            }
        });
        if (user != null) {
            fillListView();
        }
        return view;
    }

    private void fillListView() {
        points.removeAllViewsInLayout();
        thread = new Thread(new Runnable() {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
            @Override
            public void run() {
                final String IP = "http://chendam2.esy.es/Casillas/get_scores_by_id.php?idUser=" + user;
                try {
                    URL url = new URL(IP);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 1.5; es-ES) Scores");

                    int result = connection.getResponseCode();

                    if (result == HttpURLConnection.HTTP_OK) {
                        StringBuilder resultArray = new StringBuilder();
                        InputStream in = new BufferedInputStream(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            resultArray.append(line);
                        }
                        JSONObject resultJSON = new JSONObject(resultArray.toString());
                        String state = resultJSON.getString("estado");
                        if (state.equals("1")) {
                            JSONArray scoresJSON = resultJSON.getJSONArray("scores");
                            for (int i = 0; i < scoresJSON.length(); i++) {
                                adapter.add("Lattitude: " + scoresJSON.getJSONObject(i).getString("lattitude") + "\nLongitude: " +
                                        scoresJSON.getJSONObject(i).getString("longitude") + "\nScore: " +
                                        scoresJSON.getJSONObject(i).get("score"));
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        points.setAdapter(adapter);
                    }
                });
            }
        });
        thread.start();
    }

    public void interruptThread() {
        if (thread.isAlive())
            thread.interrupt();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

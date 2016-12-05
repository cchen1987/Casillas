package apperclass.casillas;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Game.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Game extends Fragment {

    private OnFragmentInteractionListener mListener;
    LinearLayout layoutContainer;
    LinearLayout[] layoutRows;
    TextView clicks;
    Button[][] buttons;
    MenuActivity menuActivity;
    Chronometer chronometer;
    SoundPool media;
    Vibrator vibrator;
    Location location;
    LocationListener locationListener;
    LocationManager locationManager;

    int column, row, frame, valueToCheck, score;
    String option;
    boolean sound, vibration;
    int[] colors = new int[] {
            R.drawable.ic_1c,
            R.drawable.ic_2c,
            R.drawable.ic_3c,
            R.drawable.ic_4c,
            R.drawable.ic_5c,
            R.drawable.ic_6c,
    };

    int[] numbers = new int[] {
            R.drawable.ic_1n,
            R.drawable.ic_2n,
            R.drawable.ic_3n,
            R.drawable.ic_4n,
            R.drawable.ic_5n,
            R.drawable.ic_6n,
    };
    int[] currentValues;
    int[][] index, currentGameStatus;
    int numberOfClicks;
    final long miliseconds = 50;
    int buttonSound;
    long timeWhenStopped;
    WebService webService;

    public Game() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        column = Integer.parseInt(preferences.getString("game_columns", "3"));
        row = Integer.parseInt(preferences.getString("game_rows", "3"));
        frame = Integer.parseInt(preferences.getString("game_options", "2"));
        option = preferences.getString("game_mode", "Números");
        vibration = preferences.getBoolean("game_vibration", true);
        sound = preferences.getBoolean("game_sound", true);

        numberOfClicks = 0;
        timeWhenStopped = 0;
        currentGameStatus = null;

        if (sound) {
            media = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            buttonSound = media.load(getActivity(), R.raw.touch, 1);
        }

        if (vibration) {
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }

        View view = inflater.inflate(R.layout.fragment_game, container, false);
        clicks = (TextView) view.findViewById(R.id.tvclicks);
        layoutContainer = (LinearLayout) view.findViewById(R.id.gamelayout);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            else {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        if (location != null)
            Log.i("localizacion: ", (location.getLatitude() + " " + location.getLongitude()));
        else
            Toast.makeText(menuActivity, "localizacion nula", Toast.LENGTH_SHORT).show();

        generateButtons();
        return view;
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

    public void middleButtonPressed(int tempRow, int tempCol) {
        // Right side button
        rightButtonPressed(tempRow, tempCol);
        // Button below
        buttonBelowPressed(tempRow, tempCol);
        // Left side button
        leftButtonPressed(tempRow, tempCol);
        // Button above
        buttonAbovePressed(tempRow, tempCol);
    }

    public void leftOrRightColumnButtonPressed(int tempRow, int tempCol) {
        // Button above
        buttonAbovePressed(tempRow, tempCol);
        // Button below
        buttonBelowPressed(tempRow, tempCol);
        // Left column
        if (tempCol == 0) {
            // Right side button
            rightButtonPressed(tempRow, tempCol);
        }
        // Right column
        else {
            // Left side button
            leftButtonPressed(tempRow, tempCol);
        }
    }

    public void bottomRowButtonPressed(int tempRow, int tempCol) {
        // Button above the pressed button
        buttonAbovePressed(tempRow, tempCol);
        // Bottom left corner button
        if (tempCol == 0) {
            // Right button
            rightButtonPressed(tempRow, tempCol);
            // Top right button
            topRightButtonPressed(tempRow, tempCol);
        }
        // Bottom right corner button
        else if (tempCol == column - 1) {
            // Left side Button
            leftButtonPressed(tempRow, tempCol);
            // Top left button
            topLeftButtonPressed(tempRow, tempCol);
        }
        else {
            // Right side button
            rightButtonPressed(tempRow, tempCol);
            // Left side button
            leftButtonPressed(tempRow, tempCol);
        }
    }

    public void topRowButtonPressed(int tempRow, int tempCol) {
        // Button below
        buttonBelowPressed(tempRow, tempCol);
        // Left corner
        if (tempCol == 0) {
            // Right side button
            rightButtonPressed(tempRow, tempCol);
            // Bottom right button
            bottomRightButtonPressed(tempRow, tempCol);
        }
        // Right corner
        else if (tempCol == column - 1) {
            // Left side button
            leftButtonPressed(tempRow, tempCol);
            // Bottom left button
            bottomLeftButtonPressed(tempRow, tempCol);
        }
        // Not corner buttons
        else {
            // Right side button
            rightButtonPressed(tempRow, tempCol);
            // Left side button
            leftButtonPressed(tempRow, tempCol);
        }
    }

    private void topLeftButtonPressed(int tempRow, int tempCol) {
        if (index[tempRow - 1][tempCol - 1] + 1 < frame) {
            buttons[tempRow - 1][tempCol - 1].setBackgroundResource(currentValues[index[tempRow - 1][tempCol - 1] + 1]);
            index[tempRow - 1][tempCol - 1]++;
        }
        else {
            buttons[tempRow - 1][tempCol - 1].setBackgroundResource(currentValues[0]);
            index[tempRow - 1][tempCol - 1] = 0;
        }
    }

    private void buttonAbovePressed(int tempRow, int tempCol) {
        if (index[tempRow - 1][tempCol] + 1 < frame) {
            buttons[tempRow - 1][tempCol].setBackgroundResource(currentValues[index[tempRow - 1][tempCol] + 1]);
            index[tempRow - 1][tempCol]++;
        }
        else {
            buttons[tempRow - 1][tempCol].setBackgroundResource(currentValues[0]);
            index[tempRow - 1][tempCol] = 0;
        }
    }

    private void topRightButtonPressed(int tempRow, int tempCol) {
        if (index[tempRow - 1][tempCol + 1] + 1 < frame) {
            buttons[tempRow - 1][tempCol + 1].setBackgroundResource(currentValues[index[tempRow - 1][tempCol + 1] + 1]);
            index[tempRow - 1][tempCol + 1]++;
        }
        else {
            buttons[tempRow - 1][tempCol + 1].setBackgroundResource(currentValues[0]);
            index[tempRow - 1][tempCol + 1] = 0;
        }
    }

    private void leftButtonPressed(int tempRow, int tempCol) {
        if (index[tempRow][tempCol - 1] + 1 < frame) {
            buttons[tempRow][tempCol - 1].setBackgroundResource(currentValues[index[tempRow][tempCol - 1] + 1]);
            index[tempRow][tempCol - 1]++;
        }
        else {
            buttons[tempRow][tempCol - 1].setBackgroundResource(currentValues[0]);
            index[tempRow][tempCol - 1] = 0;
        }
    }

    private void rightButtonPressed(int tempRow, int tempCol) {
        if (index[tempRow][tempCol + 1] + 1 < frame) {
            buttons[tempRow][tempCol + 1].setBackgroundResource(currentValues[index[tempRow][tempCol + 1] + 1]);
            index[tempRow][tempCol + 1]++;
        }
        else {
            buttons[tempRow][tempCol + 1].setBackgroundResource(currentValues[0]);
            index[tempRow][tempCol + 1] = 0;
        }
    }

    private void bottomLeftButtonPressed(int tempRow, int tempCol) {
        if (index[tempRow + 1][tempCol - 1] + 1 < frame) {
            buttons[tempRow + 1][tempCol - 1].setBackgroundResource(currentValues[index[tempRow + 1][tempCol - 1] + 1]);
            index[tempRow + 1][tempCol - 1]++;
        }
        else {
            buttons[tempRow + 1][tempCol - 1].setBackgroundResource(currentValues[0]);
            index[tempRow + 1][tempCol - 1] = 0;
        }
    }

    private void buttonBelowPressed(int tempRow, int tempCol) {
        if (index[tempRow + 1][tempCol] + 1 < frame) {
            buttons[tempRow + 1][tempCol].setBackgroundResource(currentValues[index[tempRow + 1][tempCol] + 1]);
            index[tempRow + 1][tempCol]++;
        }
        else {
            buttons[tempRow + 1][tempCol].setBackgroundResource(currentValues[0]);
            index[tempRow + 1][tempCol] = 0;
        }
    }

    private void bottomRightButtonPressed(int tempRow, int tempCol) {
        if (index[tempRow + 1][tempCol + 1] + 1 < frame) {
            buttons[tempRow + 1][tempCol + 1].setBackgroundResource(currentValues[index[tempRow + 1][tempCol + 1] + 1]);
            index[tempRow + 1][tempCol + 1]++;
        }
        else {
            buttons[tempRow + 1][tempCol + 1].setBackgroundResource(currentValues[0]);
            index[tempRow + 1][tempCol + 1] = 0;
        }
    }

    public boolean checkForWin() {
        valueToCheck = index[0][0];
        for (int lRow = 0; lRow < row; lRow++) {
            for (int lCol = 0; lCol < column; lCol++) {
                if (valueToCheck != index[lRow][lCol]) {
                    return false;
                }
            }
        }
        webService = new WebService();

        return true;
    }

    public void generateButtons() {
        index = new int[row][column];
        if (option.equals("Colores")) {
            currentValues = colors;
        }
        else {
            currentValues = numbers;
        }

        Random rand = new Random();
        layoutRows = new LinearLayout[row];
        buttons = new Button[row][column];

        for (int lRow = 0; lRow < row; lRow++) {
            layoutRows[lRow] = new LinearLayout(getActivity());
            layoutRows[lRow].setOrientation(LinearLayout.HORIZONTAL);
            layoutRows[lRow].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            for (int lCol = 0; lCol < column; lCol++) {
                buttons[lRow][lCol] = new Button(getActivity());
                index[lRow][lCol] = rand.nextInt(frame);
                buttons[lRow][lCol].setBackgroundResource(currentValues[index[lRow][lCol]]);
                layoutRows[lRow].addView(buttons[lRow][lCol], new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            }
            layoutContainer.addView(layoutRows[lRow]);
        }

        for (int lRow = 0; lRow < row; lRow++) {
            for (int lCol = 0; lCol < column; lCol++) {
                final int tempRow = lRow, tempCol = lCol;
                buttons[lRow][lCol].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clicks.setText("Pulsaciones: " + (++numberOfClicks));
                        if (sound) {
                            media.play(buttonSound, 1, 1, 0, 0, 1);
                        }
                        if (vibration && vibrator.hasVibrator()) {
                            vibrator.vibrate(miliseconds);
                        }

                        if (index[tempRow][tempCol] + 1 < frame) {
                            buttons[tempRow][tempCol].setBackgroundResource(currentValues[index[tempRow][tempCol] + 1]);
                            index[tempRow][tempCol]++;
                        }
                        else {
                            buttons[tempRow][tempCol].setBackgroundResource(currentValues[0]);
                            index[tempRow][tempCol] = 0;
                        }

                        // Top row
                        if (tempRow == 0) {
                            topRowButtonPressed(tempRow, tempCol);
                        }
                        // Bottom row
                        else if (tempRow == row - 1) {
                            bottomRowButtonPressed(tempRow, tempCol);
                        }
                        // Left or right column
                        else if (tempCol == 0 || tempCol == column - 1) {
                            leftOrRightColumnButtonPressed(tempRow, tempCol);
                        }
                        // Middle
                        else {
                            middleButtonPressed(tempRow, tempCol);
                        }
                        if (checkForWin()) {
                            chronometer.stop();
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String user = preferences.getString("user", "");
                            calculateScore();
                            webService = new WebService();
                            if (location != null)
                                webService.execute("2", user, "" + score, "" + location.getLatitude(), "" + location.getLongitude());
                            else
                                webService.execute("2", user, "" + score, "0", "0");
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    layoutContainer.removeAllViews();
                                    clicks.setVisibility(View.INVISIBLE);
                                    chronometer.setVisibility(View.INVISIBLE);
                                    menuActivity.resetViews();
                                }
                            };
                            AlertDialog.Builder aBuilder = new AlertDialog.Builder(getActivity());
                            aBuilder.setTitle("¡Felicidades!").setMessage("Has finalizado el juego con un total de " +
                                    numberOfClicks + " pulsaciones").setNeutralButton("Aceptar", dialogClickListener);
                            AlertDialog alert = aBuilder.create();
                            alert.show();
                        }
                    }
                });
            }
        }
    }

    public void setMenuActivity(MenuActivity menuActivity) {
        this.menuActivity = menuActivity;
    }

    private void calculateScore() {
        score = ((column + row) * 15 + frame * 30 + numberOfClicks * 35 -
                (int)((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000) * 10);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 0, locationListener);
            }
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 0, locationListener);
        }
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronometer.start();
        if (currentGameStatus != null) {
            index = currentGameStatus;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            else {
                locationManager.removeUpdates(locationListener);
            }
        }
        else {
            locationManager.removeUpdates(locationListener);
        }
        timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
        chronometer.stop();
        currentGameStatus = index;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

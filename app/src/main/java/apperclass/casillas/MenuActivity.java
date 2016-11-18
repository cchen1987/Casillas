package apperclass.casillas;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        Game.OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener,
        SensorEventListener, ShakeEventManager.ShakeListener, About.OnFragmentInteractionListener,
        Help.OnFragmentInteractionListener {
    MediaPlayer mediaPlayer;
    Toolbar toolbar;
    Menu myMenu;
    LinearLayout body;
    boolean soundOn;

    ShakeEventManager shakeEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Casillas");
        setSupportActionBar(toolbar);

        mediaPlayer = new MediaPlayer();
        body = (LinearLayout) findViewById(R.id.content_menu);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        shakeEventManager = new ShakeEventManager();
        shakeEventManager.setListener(this);
        shakeEventManager.init(this);
    }

    // This method updates preference settings
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onShake() {
        if (soundOn) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            else {
                mediaPlayer.start();
            }
        }
    }

    // Preference settings fragment
    public static class Preferences extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstancesState) {
            super.onCreate(savedInstancesState);

            // Load from xml file
            addPreferencesFromResource(R.xml.preferences);

            Preference preference;
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

            preference = findPreference("game_columns");
            preference.setSummary(sharedPreferences.getString("game_columns", "3"));

            preference = findPreference("game_rows");
            preference.setSummary(sharedPreferences.getString("game_rows", "3"));

            preference = findPreference("game_options");
            preference.setSummary(sharedPreferences.getString("game_options", "2"));

            preference = findPreference("game_mode");
            preference.setSummary(sharedPreferences.getString("game_mode", "Números"));

            preference = findPreference("game_music");
            preference.setSummary(sharedPreferences.getString("game_music", "Chelsea Dagger"));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Preference preference = findPreference(s);
            switch (preference.getKey()) {
                case "game_columns":
                    preference.setSummary(sharedPreferences.getString("game_columns", "3"));
                    break;
                case "game_rows":
                    preference.setSummary(sharedPreferences.getString("game_rows", "3"));
                    break;
                case "game_options":
                    preference.setSummary(sharedPreferences.getString("game_options", "2"));
                    break;
                case "game_mode":
                    preference.setSummary(sharedPreferences.getString("game_mode", "Números"));
                    break;
                case "game_music":
                    preference.setSummary(sharedPreferences.getString("game_music", "Chelsea Dagger"));
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        myMenu = menu;
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.play_music) {
            if (!mediaPlayer.isPlaying() && soundOn) {
                setMediaSource();
                mediaPlayer.start();
            }
            return true;
        }
        else if (id == R.id.stop_music) {
            if (soundOn) {
                mediaPlayer.stop();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // This method loads media source in media player
    public void setMediaSource() {
        if (soundOn) {
            String song = PreferenceManager.getDefaultSharedPreferences(this).getString("game_music", "Chelsea Dagger");
            if (song.equals("Chelsea Dagger")) {
                mediaPlayer = MediaPlayer.create(this, R.raw.chelseadagger);
            } else if (song.equals("Hey Oh")) {
                mediaPlayer = MediaPlayer.create(this, R.raw.heyoh);
            } else {
                mediaPlayer = MediaPlayer.create(this, R.raw.prayeroftherefugee);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        body.setBackgroundResource(0);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        soundOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("game_sound", true);
        myMenu.clear();
        if (id == R.id.nav_play) {
            getMenuInflater().inflate(R.menu.play_menu, myMenu);

            if (soundOn) {
                mediaPlayer.release();
                setMediaSource();
                mediaPlayer.start();
            }
            else {
                mediaPlayer.release();
            }
            Game game = new Game();
            game.setMenuActivity(this);
            getFragmentManager().beginTransaction().replace(R.id.body, game).commit();
        }
        else if (id == R.id.nav_preferences) {
            getFragmentManager().beginTransaction().replace(R.id.body, new Preferences()).commit();
        }
        else if (id == R.id.nav_about) {
            getFragmentManager().beginTransaction().replace(R.id.body, new About()).commit();
        }
        else if (id == R.id.nav_help) {
            getFragmentManager().beginTransaction().replace(R.id.body, new Help()).commit();
        }

        item.setChecked(true);
        getSupportActionBar().setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void resetViews() {
        body.setBackgroundResource(R.drawable.casillas2);
        myMenu.clear();
        getSupportActionBar().setTitle("Casillas");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundOn) {
            mediaPlayer.pause();
        }
        shakeEventManager.deregister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundOn) {
            mediaPlayer.start();
        }
        shakeEventManager.register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundOn) {
            mediaPlayer.release();
        }
    }
}

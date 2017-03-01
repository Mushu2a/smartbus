package com.m1.lesbuteurs.smartbus.breakout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.m1.lesbuteurs.smartbus.AppController;
import com.m1.lesbuteurs.smartbus.R;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_SCORES;

/**
 * This is the main Activity class for Breakout. The user can choose to start a
 * new game or continue a saved game. The user can also toggle sound effects on
 * or off. Displays the user's high score. Contains a web link to the source code.
 * 
 */
public class Splash extends Activity {

	private String jsonResponse;
	private final String FILE_PATH = "data/data/com.m1.lesbuteurs.smartbus/data.dat";
	private final String NEW_GAME = "NEW_GAME";
	private int newGame;
	private String scoreStr = "Mon meilleur score = ";
	private int highScore;
	private static final String HIGH_SCORE_PREF = "HIGH_SCORE_PREF";
	private boolean sound;
	private final String SOUND_ON_OFF = "SOUND_ON_OFF";
	private static final String SOUND_PREFS = "SOUND_PREFS";

	/**
	 * Initializes the Activity. Sets the layout. Acquires media volume control.
	 * {@inheritDoc}
	 * 
	 * @param savedInstanceState
	 *            saved data from previous run
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.splash);

		scoreGame(null);
	}

	/**
	 * Fetches the user's high score from saved data file.
	 * 
	 * @return saved player's score. Zero if no data found.
	 * 
	 * */
	private int fetchHighScore() {
		int points = 0;

		FileInputStream fis;
		try {
			fis = new FileInputStream(FILE_PATH);
			ObjectInputStream ois = new ObjectInputStream(fis);
			points = ois.readInt();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return points;
	}

	/**
	 * Prints the player's high score to screen.
	 * 
	 * */
	private void showHighScore() {
		int points = fetchHighScore();

		if (points > highScore) {
			highScore = points;
		}

		TextView hiScore = (TextView) findViewById(R.id.hiScoreView);
		hiScore.setText(scoreStr + highScore);
	}

	/**
	 * Callback function for the New Game Button when clicked. Combines the
	 * sound on or off selection and the newGame value in the intent. Launches
	 * the Breakout class and signals a new game.
	 * 
	 * @param view
	 *            new game button view
	 * */
	public void newGame(View view) {
		newGame = 1;
		Intent intent = new Intent(this, Breakout.class);
		intent.putExtra(NEW_GAME, newGame);
		intent.putExtra(SOUND_ON_OFF, sound);
		startActivity(intent);
	}

	/**
	 * Callback function for the Continue Game Button when clicked. Combines the
	 * sound on or off selection and the newGame value in the intent. Launches
	 * the Breakout class and signals that the saved game data should be read.
	 * 
	 * @param view
	 *            continue game button view
	 * */
	public void contGame(View view) {
		newGame = 0;
		Intent intent = new Intent(this, Breakout.class);
		intent.putExtra(NEW_GAME, newGame);
		intent.putExtra(SOUND_ON_OFF, sound);
		startActivity(intent);
	}

	/**
	 * Method to make json array request where response starts with []
	 *
	 * Affichage des 5 meilleurs joueurs
	 * */
	public void scoreGame(View view) {

		JsonArrayRequest bestPlayers = new JsonArrayRequest(URL_SCORES, new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
			try {
				jsonResponse = "| TOP Players |<br><br>";

				for (int i = 0; i < response.length(); i++) {

					JSONObject scores = (JSONObject) response.get(i);

					String username = scores.getString("username");
					String score = scores.getString("score");
					String date = scores.getString("updated_at");

					i = i + 1;
					jsonResponse += "<b>" + i + ": </b> " + username + " <b>" + score + "</b> <i>" + date.substring(0,10) + "</i><br>";
					i = i - 1;

				}

				TextView topPlayers = (TextView) findViewById(R.id.topPlayers);
				topPlayers.setText(Html.fromHtml(jsonResponse));

			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(bestPlayers);
	}

	/**
	 * Called when this Activity is resumed. Restores the sound on/off button
	 * state and high score value.
	 * {@inheritDoc}
	 * */
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences soundSettings = getSharedPreferences(SOUND_PREFS, 0);
		sound = soundSettings.getBoolean("soundOn", true);
		SharedPreferences scoreSettings = getSharedPreferences(HIGH_SCORE_PREF, 0);
		highScore = scoreSettings.getInt("highScore", 0);
		ToggleButton soundButton = (ToggleButton) findViewById(R.id.soundToggleButton);
		soundButton.setChecked(sound);
		showHighScore();
	}

	/**
	 * Callback function for when the sound on/off button is clicked. Sets the
	 * sound boolean in response to the button state.
	 * 
	 * @param v
	 *            sound toggle button
	 * */
	public void soundToggle(View v) {
		sound = ((ToggleButton) v).isChecked();
	}

	/**
	 * Called when the system pauses this Activity. Saves the sound button state
	 * and high score value.
	 * {@inheritDoc}
	 * */
	@Override
	protected void onPause() {
		super.onPause();

		// save sound settings and high score
		SharedPreferences soundSettings = getSharedPreferences(SOUND_PREFS, 0);
		SharedPreferences highScoreSave = getSharedPreferences(HIGH_SCORE_PREF,
				0);
		SharedPreferences.Editor soundEditor = soundSettings.edit();
		SharedPreferences.Editor scoreEditor = highScoreSave.edit();
		scoreEditor.putInt("highScore", highScore);
		soundEditor.putBoolean("soundOn", sound);

		// Commit the edits
		soundEditor.commit();
		scoreEditor.commit();
	}
}

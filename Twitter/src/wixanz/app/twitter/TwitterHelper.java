package wixanz.app.twitter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterHelper {

	static Twitter twitter;
	static SharedPreferences prefs;
	static String username;

	public static boolean isSigned(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String token = prefs.getString("accessToken", "");
		String secret = prefs.getString("accessTokenSecret", "");
		if (token != "" && secret != "") {
			// if you can initialize Twitter4j lib and can get account settings
			// then you are signed and vice versa.
			initializeTwitterLib(prefs);
			try {
				twitter.getAccountSettings();
				return true;
			} catch (TwitterException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	// Check newtwork state
	public static boolean isNetworkAccess(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if (ni != null && ni.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	// Just to minimize my code a bit
	public static void Toast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	// remove tokens from preferences
	public static boolean clearOAuthTokens() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("accessToken", "");
		editor.putString("accessTokenSecret", "");
		editor.commit();
		return true;
	}

	public static boolean updateStatus(String msg) {
		try {
			twitter.updateStatus(msg);
		} catch (TwitterException e) {
			return false;
		}
		return true;
	}

	// Twitter4j initialization
	static Twitter initializeTwitterLib(SharedPreferences prefs) {
		AccessToken accessToken = new AccessToken(prefs.getString("accessToken", null), prefs.getString("accessTokenSecret", null));
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		twitter.setOAuthAccessToken(accessToken);
		return twitter;
	}
}

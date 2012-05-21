package wixanz.app.twitter;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterHelper {

	static Twitter twitter;
	static SharedPreferences prefs;
	static String username;
	protected static boolean sucess;


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
	
	public static boolean updateStatus(final String msg) {
		new Thread(new Runnable() {
			public void run() {
				try {
					if (twitter.updateStatus(msg) != null)
						sucess = true;
				} catch (TwitterException e) {
				}
			}
		}).start();
		
		if (sucess) {
			return true;
		} else {
			return false;
		}
	}

	// Twitter4j initialization
	static Twitter initializeTwitterLib(SharedPreferences prefs) {
		AccessToken accessToken = new AccessToken(prefs.getString("accessToken", null), prefs.getString("accessTokenSecret", null));
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		twitter.setOAuthAccessToken(accessToken);
		return twitter;
	}

	public static void DeleteRecursive(File cacheDir) {
		Log.d("DeleteRecursive", "DELETEPREVIOUS TOP" + cacheDir.getPath());
		if (cacheDir.isDirectory()) {
			String[] children = cacheDir.list();
			for (int k = 0; k < children.length; k++) {
				File temp = new File(cacheDir, children[k]);
				if (temp.isDirectory()) {
					Log.d("DeleteRecursive", "Recursive Call" + temp.getPath());
					DeleteRecursive(temp);
				} else {
					Log.d("DeleteRecursive", "Delete File" + temp.getPath() + " Bytes: " + temp.length());
					boolean b = temp.delete();
					if (b == false) {
						Log.d("DeleteRecursive", "DELETE FAIL");
					}
				}
			}

			Log.d("DeleteRecursive", "Folder " + cacheDir.getName() + " is deleted!");
			cacheDir.delete();
		}
	}
}

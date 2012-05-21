package wixanz.app.twitter;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Twitter extends Activity implements OnClickListener {
	
	private ProgressDialog dialog;
	private Boolean isConnected;
	Button bSignIn;
	private int TWITTER_AUTH;
	public twitter4j.Twitter twitter;
	private RequestToken reqTok = null;;
	private SharedPreferences prefs;
	private String accessToken;
	private String accessTokenSecret;
	private IntentFilter mNetworkStateChangedFilter;
	private BroadcastReceiver mNetworkStateIntentReceiver;
	final String TAG = getClass().getName();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		// Initialize variables
		bSignIn = (Button) findViewById(R.id.bSignIn);
		bSignIn.setOnClickListener(this);

		mNetworkStateChangedFilter = new IntentFilter();
		mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		mNetworkStateIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					isConnected = TwitterHelper.isNetworkAccess(context);
					Log.i(TAG, "Network change:" + String.valueOf(isConnected));
				}
			}
		};
		registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
	}

	public void onClick(View v) {
		new Authorization().execute();
	}

	private void redirect() {
		Intent i = new Intent(Twitter.this, TweetList.class);
		startActivity(i);
		finish();
	}

	protected Dialog onCreateDialog(int id)
	{
	    String msg, title;
	    switch (id)
	    {
	        case 1:
	            title = "Connecting to service...";
	            msg = "Please, wait a second...";
	            break;
	        default:
	            return null;
	    }
	    dialog = ProgressDialog.show(this, title, msg, true, true); 
	    return dialog;
	}
	
	/*
	 * Getting access tokens to initialize Twitter4j library in future.
	 * Twitter4j will be used as the mayor tool to manipulate tweets, statuses,
	 * homelines and so on.
	 */
	public class Authorization extends AsyncTask<Void, Void, Void> {

		protected void onPreExecute() {
			showDialog(1);
		}

		protected Void doInBackground(Void... params) {
			if (isConnected) {
				if (!(TwitterHelper.isSigned(Twitter.this))) {
					if ((accessToken == null) || (accessTokenSecret == null)) {
						// Getting Twitter4j instance
						twitter = new TwitterFactory().getInstance();

						// Set key and secret for future requestToken generation
						twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
						try {
							// Getting requestToken
							reqTok = twitter.getOAuthRequestToken(Constants.OAUTH_CALLBACK_URL);
						} catch (TwitterException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			// Go to our WebView with following callback on onActivityResult for
			// data passing
			if (isConnected && reqTok != null) {
				Intent i = new Intent(Twitter.this, TwitterWebView.class);
				i.putExtra("URL", reqTok.getAuthenticationURL() + "&force_login=true" + TwitterHelper.prefs.getString("screenName", ""));
				startActivityForResult(i, TWITTER_AUTH);
			} else if (isConnected && reqTok == null) {
				// redirect if you are already signed
				redirect();
			} else {
				TwitterHelper.Toast(Twitter.this, "Connection is aborted!");
			}
			removeDialog(1);	
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Verifier and positive result(requestCode, resultCode) from WebView
		// have been received
		if (requestCode == TWITTER_AUTH) {
			if (resultCode == Activity.RESULT_OK) {
				// Extract our verifier from Intent
				String verifier = (String) data.getExtras().get("verifier");
				AccessToken a = null;

				try {
					// Getting access Token and access Token secret
					a = twitter.getOAuthAccessToken(verifier);
					String accessToken = a.getToken();
					String accessTokenSecret = a.getTokenSecret();

					// Save our tokens and account username in Shared
					// Preferences
					prefs = PreferenceManager.getDefaultSharedPreferences(this);

					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("accessToken", accessToken);
					editor.putString("accessTokenSecret", accessTokenSecret);

					// Just for easy and fast future authorization
					if (twitter.getScreenName() != prefs.getString("screenName", "")) {
						editor.putString("screenName", "&screen_name=" + twitter.getScreenName());
					}

					editor.commit();

					if (TwitterHelper.isSigned(this)) {
						redirect();
					} else {
						// Abort message in case of access tokens are expired
						// and you cannot connect to Twitter4j lib
						AlertDialog alertDialog = new AlertDialog.Builder(this).create();
						alertDialog.setTitle("Aborted");
						alertDialog.setMessage("Access tokens are expired!");
						alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Authorization().execute();
							}
						});
						alertDialog.show();
					}
				} catch (TwitterException e) {
					Log.i(TAG, "Cannot get access token and access token secret!");
				}
			}
		} else {
			Log.e(TAG, "No Request code comes back from WebView!");
		}
	}
	
    @Override
    protected void onResume() {
	Log.d(TAG, "onResume");
	super.onResume();
	registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
    }

    @Override
    protected void onPause() {
	Log.d(TAG, "onPause");
	super.onPause();
	unregisterReceiver(mNetworkStateIntentReceiver);
    }
}
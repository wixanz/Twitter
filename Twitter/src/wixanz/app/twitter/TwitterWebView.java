package wixanz.app.twitter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebView extends Activity {

	String url;
	WebView TwitterWeb;
	private Intent intent;
	final String TAG = getClass().getName();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitterwebview);

		// Get our auth URL
		intent = getIntent();
		String url = (String) intent.getExtras().get("URL");

		TwitterWeb = (WebView) findViewById(R.id.wvTwitter);
		TwitterWeb.setWebViewClient(new WebViewClient() {

			// Catch our callback URL with verifier, save verifier to intent and
			// set result for onActivity result method as OK
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith(Constants.OAUTH_CALLBACK_URL)) {
					Uri uri = Uri.parse(url);
					String verifier = uri.getQueryParameter("oauth_verifier");
					if (verifier != null) {
						intent.putExtra("verifier", verifier);
						setResult(RESULT_OK, intent);
					} else {
						setResult(RESULT_CANCELED, intent);
					}
					TwitterWeb.clearCache(true);
					finish();
					return true;
				}
				return false;
			}

			// Msg in case network connection is aborted or twitter page isn`t
			// available at the moment and for other web service errors
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				AlertDialog alertDialog = new AlertDialog.Builder(TwitterWebView.this).create();
				alertDialog.setTitle("Aborted");
				alertDialog.setMessage(description);
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(TwitterWebView.this, Twitter.class));
						finish();
					}
				});
				alertDialog.show();
			}
		});
		TwitterWeb.getSettings().setJavaScriptEnabled(true);
		TwitterWeb.getSettings().setSavePassword(false);
		TwitterWeb.getSettings().setSaveFormData(false);
		TwitterWeb.loadUrl(url);

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent i = new Intent(this,Twitter.class);
		startActivity(i);
		finish();
	}
}

package wixanz.app.twitter;

import java.util.List;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import wixanz.app.twitter.TwitterHelper;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TweetList extends Activity implements OnClickListener, OnRefreshListener {

	Boolean up;
	int i = 1;
	ListView actualListView, tweetListView;
	Button bAddTweet, bSearchTweet, bCancel, bTweet;
	TextView text;
	EditText etAddSearch, etTweetMsg;
	PullToRefreshListView pullToRefreshView;
	HomelineListAdapter homelinesAdapter;
	TweetListAdapter tweetAdapter;
	twitter4j.Twitter twitter = TwitterHelper.twitter;
	List<Status> homelines;
	List<Tweet> tweets = null;;
	Paging paging;
	boolean search;
	AlertDialog.Builder builder;
	AlertDialog alertDialog;
	InputMethodManager inputManager;
	String TAG = getClass().getName();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweetlist);

		TwitterHelper.Toast(this, "Congradulations!\n You are signed! Your auth data is saved!");

		initializeVars();
		getHomelines();
	}

	private void getHomelines() {

		try {
			paging = new Paging(i, 10);
			actualListView = pullToRefreshView.getRefreshableView();

			homelines = twitter.getHomeTimeline(paging);
			// Use our custom adapter for custom refreshable list view
			homelinesAdapter = new HomelineListAdapter(this, R.layout.customtweetlist, homelines);
			actualListView.setAdapter(homelinesAdapter);

		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private void initializeVars() {
		pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		etAddSearch = (EditText) findViewById(R.id.etAddSearch);
		bAddTweet = (Button) findViewById(R.id.bAdd);
		bSearchTweet = (Button) findViewById(R.id.bSearch);
		bAddTweet.setOnClickListener(this);
		bSearchTweet.setOnClickListener(this);
		pullToRefreshView.setOnRefreshListener(this);
	}

	// Set up our optional menu for exit from app and switch twitter`s account
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.switchAccount:
			if (TwitterHelper.clearOAuthTokens()) {
				Intent i = new Intent(this, Twitter.class);
				startActivity(i);
				finish();
			}
			break;
		case R.id.exit:
			if (TwitterHelper.clearOAuthTokens())
				System.exit(0);
			break;
		}
		return false;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bAdd:
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.customtweet, (ViewGroup) findViewById(R.id.cTweet));

			text = (TextView) layout.findViewById(R.id.tvCount);
			etTweetMsg = (EditText) layout.findViewById(R.id.etTweetMsg);
			bCancel = (Button) layout.findViewById(R.id.bCancel);
			bTweet = (Button) layout.findViewById(R.id.bTweet);

			etTweetMsg.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				public void afterTextChanged(Editable s) {
					text.setText("Symbols: " + String.valueOf(140 - s.length()));
					if (s.length() == 140)
						TwitterHelper.Toast(TweetList.this, "Stop! Stop! Stop!");
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
			});

			bCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Hide virtual keyboard when search button is
					// pressed
					inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

					alertDialog.dismiss();
				}
			});

			bTweet.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (etTweetMsg.getText().length() < 1) {
						TwitterHelper.Toast(v.getContext(), "Very short tweet!");
					} else {
						if (TwitterHelper.updateStatus(etTweetMsg.getText().toString())) {

							// Hide virtual keyboard when search button is
							// pressed
							inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
							alertDialog.dismiss();

							TwitterHelper.Toast(v.getContext(), "Twit!");
						} else {
							TwitterHelper.Toast(v.getContext(), "You have the same status! Change it to add new one.");
						}
					}

				}
			});

			builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			alertDialog = builder.create();
			alertDialog.show();
			break;

		case R.id.bSearch:
			if (etAddSearch.getText().length() < 1) {
				Toast.makeText(this, "Very short search query!", Toast.LENGTH_LONG).show();
			} else {

				// Search tweets and pass them throughout custom adapter
				try {
					search = true;
					tweetListView = pullToRefreshView.getRefreshableView();
					tweets = twitter.search(new Query(etAddSearch.getText().toString()).page(i)).getTweets();

					tweetAdapter = new TweetListAdapter(this, R.layout.customtweetlist, tweets);

					tweetListView.setAdapter(tweetAdapter);
				} catch (TwitterException e) {
					e.printStackTrace();
				}

				tweetAdapter.notifyDataSetChanged();

				// Hide virtual keyboard when search button is pressed
				inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
			break;
		}

	}

	// Refresh pull-to-refresh list view, uploading new tweets
	public void onRefresh() {
		if (search) {
			if ((int) actualListView.getLastVisiblePosition() <= 6) {
				up = true;
				new GetTweetsTask(this, tweets, etAddSearch.getText().toString(), i, up, homelinesAdapter, pullToRefreshView).execute();
			} else {
				++i;
				up = false;
				new GetTweetsTask(this, tweets, etAddSearch.getText().toString(), i, up, homelinesAdapter, pullToRefreshView).execute();
			}
		} else {
			if ((int) actualListView.getLastVisiblePosition() <= 6) {
				up = true;
				new GetHomelinesTask(this, homelines, paging, up, homelinesAdapter, pullToRefreshView).execute();
			} else {
				paging.setPage(++i);
				up = false;
				new GetHomelinesTask(this, homelines, paging, up, homelinesAdapter, pullToRefreshView).execute();
			}
		}
	}

	public void onBackPressed() {
		// Come back to homelines if you use search and minimize app if you show
		// homelines at the moment
		if (search == true) {
			search = false;
			i = 1;
			etAddSearch.clearFocus();
			etAddSearch.setText("");
			getHomelines();
		} else {
			super.onBackPressed();
		}
	}

}

package wixanz.app.twitter;

import java.util.List;
import twitter4j.Query;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class GetTweetsTask extends AsyncTask<Void, Void, List<Tweet>> {
	
	int i;
	String msg;
	Boolean up;
	twitter4j.Twitter twitter = TwitterHelper.twitter;
	List<twitter4j.Tweet> tweets;
	ListView actualListView;
	PullToRefreshListView pullToRefreshView;
	HomelineListAdapter homelinesAdapter;
	

	public GetTweetsTask(Context context, List<twitter4j.Tweet> tweets, String msg, int i, Boolean up, HomelineListAdapter homelinesAdapter,
			PullToRefreshListView pullToRefreshView) {
		this.up = up;
		this.tweets = tweets;
		this.i = i;
		this.homelinesAdapter = homelinesAdapter;
		this.pullToRefreshView = pullToRefreshView;
		this.msg = msg;
	}

	protected List<twitter4j.Tweet> doInBackground(Void... params) {
		try {
			// Upload some homelines to the begging which are newer than
			// current homelines
			if (up) {
				for (int c = 0; c < twitter.search(new Query(msg)).getTweets().size(); c++) {
					if (twitter.search(new Query(msg)).getTweets().get(c).getCreatedAt().after(tweets.get(c).getCreatedAt())) {
						tweets.add(c, twitter.search(new Query(msg)).getTweets().get(c));
					} else {
						break;
					}
				}
				// Upload next 10 tweets to the end of the refreshable list
			} else {
				tweets.addAll(twitter.search(new Query(msg).page(i)).getTweets());
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		return tweets;

	}

	protected void onPostExecute(List<twitter4j.Tweet> result) {
		// notify about new incoming tweets
		homelinesAdapter.notifyDataSetChanged();

		// Call onRefreshComplete when the list has been refreshed.
		pullToRefreshView.onRefreshComplete();
		super.onPostExecute(result);
	}
}

package wixanz.app.twitter;

import java.util.List;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

//Uploading new homelines increasing page each time when onRefresh method
// sends a signal. Next up variable define you scroll Up or down.
public class GetHomelinesTask extends AsyncTask<Void, Void, List<Status>> {
	Boolean up;
	twitter4j.Twitter twitter = TwitterHelper.twitter;
	List<twitter4j.Status> homelines;
	Paging paging;
	Context context;
	ListView actualListView;
	PullToRefreshListView pullToRefreshView;
	HomelineListAdapter homelinesAdapter;

	public GetHomelinesTask(Context context, List<twitter4j.Status> homelines, Paging paging, Boolean up, HomelineListAdapter homelinesAdapter,
			PullToRefreshListView pullToRefreshView) {
		this.up = up;
		this.homelines = homelines;
		this.paging = paging;
		this.context = context;
		this.homelinesAdapter = homelinesAdapter;
		this.pullToRefreshView = pullToRefreshView;
	}

	protected List<twitter4j.Status> doInBackground(Void... params) {
		try {
			// Upload some homelines to the begging which are newer than current
			// homelines
			if (up) {
				// add homelines which have created time after my first
				// visible homeline
				for (int c = 0; c < twitter.getHomeTimeline().size(); c++) {
					if (twitter.getHomeTimeline().get(c).getCreatedAt().after(homelines.get(c).getCreatedAt())) {
						homelines.add(c, twitter.getHomeTimeline().get(c));
					} else {
						break;
					}
				}
				// Upload next 10 homelines to the end of the refreshable list
			} else {

				homelines.addAll(twitter.getHomeTimeline(paging));
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return homelines;

	}

	protected void onPostExecute(List<twitter4j.Status> result) {
		// notify about new incoming tweets
		homelinesAdapter.notifyDataSetChanged();

		// Call onRefreshComplete when the list has been refreshed.
		pullToRefreshView.onRefreshComplete();
		super.onPostExecute(result);
	}
}
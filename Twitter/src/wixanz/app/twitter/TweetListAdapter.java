package wixanz.app.twitter;

import java.util.List;
import twitter4j.Tweet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetListAdapter extends ArrayAdapter<Tweet> {
	private final Context context;
	private final List<Tweet> tweets;
	public ImageManager imageManager;

	public TweetListAdapter(Context context, int textViewResourceId, List<Tweet> tweets) {
		super(context, textViewResourceId, tweets);

		this.context = context;
		this.tweets = tweets;
		imageManager = new ImageManager(context.getApplicationContext());
	}

	public static class ViewHolder {
		public TextView username;
		public TextView message;
		public ImageView image;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;
		if (v == null) {
			// Set up Layout inflater to make custom view
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.customtweetlist, null);

			holder = new ViewHolder();
			holder.username = (TextView) v.findViewById(R.id.tvUsername);
			holder.message = (TextView) v.findViewById(R.id.tvText);
			holder.image = (ImageView) v.findViewById(R.id.ivImage);
			v.setTag(holder);
		} else
			holder = (ViewHolder) v.getTag();

		// Get tweet position and attach each tweet to different custom view
		// blocks
		final Tweet tweet = tweets.get(position);
		if (tweet != null) {
			holder.username.setText(tweet.getToUser());
			holder.message.setText(tweet.getText());
			holder.image.setTag(tweet.getProfileImageUrl());

			// ImageMnagaer is created for images caching
			imageManager.displayImage(tweet.getProfileImageUrl(), context, holder.image);
		}
		return v;
	}
}
package wixanz.app.twitter;

import java.util.List;
import twitter4j.Status;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HomelineListAdapter extends ArrayAdapter<Status> {
	private final Context context;
	private final List<Status> homelines;
	public ImageManager imageManager;

	public HomelineListAdapter(Context context, int textViewResourceId, List<Status> homelines) {
		super(context, textViewResourceId, homelines);

		this.context = context;
		this.homelines = homelines;
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
		final Status homeline = homelines.get(position);
		if (homeline != null) {
			holder.username.setText(homeline.getUser().getName());
			holder.message.setText(homeline.getText());
			holder.image.setTag(homeline.getUser().getProfileImageURL().toString());
			// ImageMnagaer is created for images caching
			imageManager.displayImage(homeline.getUser().getProfileImageURL().toString(), context, holder.image);
		}
		return v;
	}
}
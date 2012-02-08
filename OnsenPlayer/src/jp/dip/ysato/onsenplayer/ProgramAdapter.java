package jp.dip.ysato.onsenplayer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgramAdapter extends BaseAdapter {
	private LayoutInflater layout;
	private Context context;
	private List<ProgramBean> program;
	private View loading;
	public ProgramAdapter(Context context, View loading) {
		super();
		// TODO Auto-generated constructor stub
		layout = LayoutInflater.from(context);
		this.program = new ArrayList<ProgramBean>();
		this.context = context;
		this.loading = loading;
	}
	class ShowDetailListener implements OnClickListener {
		private ProgramBean program;
		public ShowDetailListener(ProgramBean prog) {
			this.program = prog;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String detail = program.getDetail();
			if (detail == null || detail.length() == 0) {
				String imgurl[] = program.getImageUrl().split("/");
				String detailurl = context.getString(R.string.onsentop) + String.format("%s/%s/index.html", imgurl[4], imgurl[5]);
				Intent intent = new Intent(context, DetailActivity.class);
				Bundle b = new Bundle();
				b.putStringArray("program", new String[] {detailurl, program.getStream(), program.getImageUrl(), 
						program.getTitle(), program.getNo()});
				intent.putExtra("program", b);
				context.startActivity(intent);
			} else {
				Uri uri = Uri.parse(detail);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				context.startActivity(intent);
			}			
		}
		
	}
	
	class PlayButtonListener implements OnClickListener {
		private Context context;
		private ProgramBean program;
		public PlayButtonListener(Context c, ProgramBean p) {
			context = c;
			program = p;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String stream;
			stream = program.getStream();
			if (stream == null || stream.length() == 0)
				return ;
			Intent i = new Intent(context, PlayActivity.class);
			Bundle b = new Bundle();
			b.putStringArray("program", new String[] {program.getDetail(), program.getStream(), program.getImageUrl(), 
					program.getTitle(), program.getNo()});
			i.putExtra("program", b);
			context.startActivity(i);
		}
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layout.inflate(R.layout.programview, null);
			if (loading != null)
				loading.setVisibility(View.GONE);
		}
		ProgramBean p = (ProgramBean) getItem(position);
		if (p != null) {
			TextView title = (TextView) convertView.findViewById(R.id.programTitle);
			title.setText(p.getTitle());
			ImageView image = (ImageView) convertView.findViewById(R.id.programImage);
			image.setImageBitmap(p.getImage());
			TextView upd = (TextView) convertView.findViewById(R.id.updateSign);
			if (p.getUpdate()) {
				upd.setVisibility(View.VISIBLE);
				upd.setText(R.string.newSign);
			} else {
				upd.setVisibility(View.GONE);
			}
			ShowDetailListener showDetail = new ShowDetailListener(p);
			title.setOnClickListener(showDetail);
			image.setOnClickListener(showDetail);
			ImageButton pbutton = (ImageButton) convertView.findViewById(R.id.playButton);
			pbutton.setOnClickListener(new PlayButtonListener(context, p));
		}
		return convertView;
	}
	public void setList(ArrayList<ProgramBean> programs) {
		// TODO Auto-generated method stub
		program.addAll(programs);
	}
	@Override
	public int getCount() {
		return program.size();
	}
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return program.get(arg0);
	}
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

}

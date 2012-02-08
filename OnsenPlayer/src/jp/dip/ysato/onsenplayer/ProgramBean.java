package jp.dip.ysato.onsenplayer;

import android.graphics.Bitmap;

public class ProgramBean {
	private String title;
	private Bitmap image;
	private String detail;
	private String stream;
	private String no;
	private String imageURL;
	private boolean update;
	public ProgramBean(String _title, Bitmap _image, String _stream, boolean _update, String _detail, String _no, String _imageUrl) {
		title = _title;
		image = _image;
		stream = _stream;
		detail = _detail;
		update = _update;
		no = _no;
		imageURL = _imageUrl;
	}
	public String getTitle() {
		// TODO Auto-generated method stub
		return title;
	}

	public Bitmap getImage() {
		// TODO Auto-generated method stub
		return image;
	}

	public String getStream() {
		// TODO Auto-generated method stub
		return stream;
	}

	public String getDetail() {
		// TODO Auto-generated method stub
		return detail;
	}
	
	public String getNo() {
		return no;
	}
	
	public boolean getUpdate() {
		return update;
	}
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return imageURL;
	}
}

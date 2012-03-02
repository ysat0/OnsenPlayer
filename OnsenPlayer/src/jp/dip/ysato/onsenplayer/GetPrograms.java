package jp.dip.ysato.onsenplayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

public class GetPrograms extends AsyncTask<ProgramAdapter, ProgramAdapter, Void> {
	private Context context;
	public GetPrograms(Context c) {
		context = c;
	}
	@Override
	protected Void doInBackground(ProgramAdapter... params) {
		HttpClient hc = new DefaultHttpClient();
		try {
			{
				// read cookie
				HttpGet get = new HttpGet(context.getString(R.string.onsentop));
				HttpResponse res = hc.execute(get);
				InputStream in = res.getEntity().getContent();
				in.close();
			}
			GregorianCalendar cal = new GregorianCalendar();
			int w = cal.get(Calendar.DAY_OF_WEEK) - 2;
			if (w < 0 || w > 4)
				w = 4;
			for (int i = 0; i < 5; i++) {
				int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
				int day = cal.get(Calendar.DAY_OF_MONTH);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				String md5code = md5(String.format("onsen%d%d%d", dow, day, hour));
				HttpPost post = new HttpPost(context.getString(R.string.onsenxml));
				List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
				nameValuePair.add(new BasicNameValuePair("code", md5code));
				nameValuePair.add(new BasicNameValuePair("file_name", String.format("regular_%d", w + 1)));
				post.setEntity(new UrlEncodedFormEntity(nameValuePair));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int times;
				for (times = 5; times > 0; times--) {
					try {
						HttpResponse res = hc.execute(post);
						res.getEntity().writeTo(out);
						break;
					} catch(IOException e) {
						if (times == 1) {
							Toast.makeText(context, R.string.XMLreadfail, Toast.LENGTH_LONG);
						}
					}
				}
				if (times == 0)
					continue;
				ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(in);
				NodeList nodes = doc.getElementsByTagName("program");
				ArrayList<ProgramBean> programs = new ArrayList<ProgramBean>();
				for (int prog = 0; prog < nodes.getLength(); prog++) {
					Element e = (Element) nodes.item(prog);
					String title = e.getElementsByTagName("title").item(0).getTextContent();
					String imageurl = e.getElementsByTagName("imagePath").item(0).getTextContent();
					Node n = e.getElementsByTagName("detailURL").item(0);
					String detailurl = null;
					if (n != null)
						detailurl = n.getTextContent();
					boolean update = e.getElementsByTagName("isNew").item(0).getTextContent().equals("1");
					String no = e.getElementsByTagName("number").item(0).getTextContent();
					NodeList contents = e.getElementsByTagName("contents");
					String stream = null;
					for (int content = 0; content < contents.getLength(); content++) {
						Element ec = (Element) contents.item(content);
						if (ec.getElementsByTagName("isAdvertize").item(0).getTextContent().equals("0")) {
							stream = ec.getElementsByTagName("fileUrl").item(0).getTextContent();
							break;
						}
					}
					imageurl = context.getString(R.string.onsentop) + imageurl;
					Bitmap bitmap;
					{
						HttpClient imgclient = new DefaultHttpClient();
						HttpGet get = new HttpGet(imageurl);
						HttpResponse imgres = imgclient.execute(get);
						ByteArrayOutputStream imgout = new ByteArrayOutputStream();
						imgres.getEntity().writeTo(imgout);
						bitmap = BitmapFactory.decodeByteArray(imgout.toByteArray(), 0, imgout.size());
					}
					ProgramBean p = new ProgramBean(title, bitmap, stream, update, detailurl, no, imageurl);
					programs.add(p);
				}
				params[w].setList(programs);
				publishProgress(params[w]);
				w++;
				w %= 5;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotRecognizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	private String md5(String seed) {
		// TODO Auto-generated method stub
		MessageDigest d;
		try {
			d = java.security.MessageDigest.getInstance("MD5");
			d.update(seed.getBytes());
			byte[] codebyte = d.digest();
			StringBuffer codehex = new StringBuffer();
			for (int c: codebyte) {
				codehex.append(Integer.toHexString(c & 0xff));
			}
			return codehex.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	@Override
	protected void onProgressUpdate(ProgramAdapter... p) {
		p[0].notifyDataSetChanged();
	}
}

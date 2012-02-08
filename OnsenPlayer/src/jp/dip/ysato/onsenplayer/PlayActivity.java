package jp.dip.ysato.onsenplayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.SeekBar;

public class PlayActivity extends Activity {
    private ServiceConnection serviceConnection;
	protected PlayerService playerService;
	protected MediaPlayer mediaPlayer;
	private ScheduledExecutorService playermonitor;
	private SeekBar seekbar;
	private ProgressDialog dialog;
	private PlayerServiceReceiver receiver;
	private Intent service;
	private Bundle bundle;
	private ImageButton playControlButton;
	
	private ProgressDialog ShowDialog() {
		ProgressDialog d;
		d = new ProgressDialog(this);
		d.setTitle(R.string.waiting);
		d.setMessage("Getting Stream");
		d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		d.show();
		return d;
	}
	
	class PlayerServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String message = arg1.getStringExtra("message");
			if (message.equals(PlayerService.WaitStream)) {
				dialog = ShowDialog();
			}
			if (message.equals(PlayerService.Resume)) {
				dialog.cancel();
			}
			if (message.equals(PlayerService.RemotePause)) {
				playControlButton.setImageResource(android.R.drawable.ic_media_play);
			}
			if (message.equals(PlayerService.RemotePlay)) {
				playControlButton.setImageResource(android.R.drawable.ic_media_pause);
			}
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				// TODO Auto-generated method stub
				playerService = ((PlayerService.PlayerBinder)arg1).getService();
				mediaPlayer = playerService.player();
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer arg0) {
						// TODO Auto-generated method stub
						int duration = arg0.getDuration() / 1000;
						int min = duration / 60;
						int sec = duration % 60;
						TextView tv = (TextView) PlayActivity.this.findViewById(R.id.streamLength);
						tv.setText(String.format("%02d:%02d", min, sec));
						seekbar.setMax(duration);
						dialog.cancel();
					}
				});
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				// TODO Auto-generated method stub
				playerService = null;
				mediaPlayer = null;
				finish();
			}
		};
    	seekbar = (SeekBar) findViewById(R.id.playPosition);
    	seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				mediaPlayer.seekTo(arg0.getProgress() * 1000);
			}
    	});
    	playControlButton = (ImageButton) findViewById(R.id.playControlButton);
    	playControlButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					if (playerService.isPlaying()) {
						playerService.pause(true);
						playControlButton.setImageResource(android.R.drawable.ic_media_play);
					} else {
						playControlButton.setImageResource(android.R.drawable.ic_media_pause);
					}
				} catch (IOException e) {
				}
			}
    	});
    	if (savedInstanceState != null) {
    		bundle = savedInstanceState.getBundle("playing");
    	} else {
    		Intent intent = getIntent();
    		bundle = intent.getBundleExtra("program");
    	}
		dialog = ShowDialog();
		IntentFilter filter = new IntentFilter(PlayerService.Notify);
		receiver = new PlayerServiceReceiver();
		registerReceiver(receiver, filter);
		service = new Intent(this, PlayerService.class);
		service.putExtra("program", bundle);
		startService(service);
		bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		playerService.stopSelf();
		super.onDestroy();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBundle("playing", this.bundle);
		super.onSaveInstanceState(outState);
	}
	@Override
	public void onResume() {
		super.onResume();
    	String program[] = bundle.getStringArray("program");
    	String no = program[4];
    	try {
    		int n = Integer.valueOf(no);
    		no = String.format("第%d回", n);
    	} catch(NumberFormatException e) {
    	}
    	StringBuffer t = new StringBuffer();
    	t.append(program[3]);
    	t.append('\n');
    	t.append(no);
    	TextView tv = (TextView) findViewById(R.id.playerText);
    	tv.setText(t.toString());

    	class UpdateSeekBar implements Runnable {
			private Handler handler;
			public UpdateSeekBar(Handler handler) {
				this.handler = handler;
			}
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (seekbar != null && playerService != null)
							seekbar.setProgress(playerService.getPosition());
					}
				});
			}
		}
		playermonitor = Executors.newScheduledThreadPool(1);
		playermonitor.scheduleWithFixedDelay(new UpdateSeekBar(new Handler()), 0, 500, TimeUnit.MILLISECONDS);

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(program[2]);
		ByteArrayOutputStream imgout = new ByteArrayOutputStream();
		for(int times = 10; times > 0; times--) {
			try {
				HttpResponse imgres = httpClient.execute(get);
				imgres.getEntity().writeTo(imgout);
				ImageView iv = (ImageView) findViewById(R.id.playerImage);
				iv.setImageBitmap(BitmapFactory.decodeByteArray(imgout.toByteArray(), 0, imgout.size()));
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
}

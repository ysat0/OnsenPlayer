package jp.dip.ysato.onsenplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class EventReceiver extends BroadcastReceiver {
	private static long lasttime;
	private static String lastaction;
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		String action = arg1.getAction();
		if ((System.currentTimeMillis() - lasttime) < 1000 && action.equals(lastaction))
			return;
		lasttime = System.currentTimeMillis();
		lastaction = action;
		Intent intent = new Intent(arg0, PlayerService.class);
		if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			intent.setAction(PlayerService.PAUSE);
		}
		if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
			intent.setAction(PlayerService.PlayPause);
		}
		arg0.startService(intent);
	}

}

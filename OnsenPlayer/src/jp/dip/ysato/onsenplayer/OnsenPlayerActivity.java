package jp.dip.ysato.onsenplayer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import jp.dip.ysato.onsenplayer.R.id;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class OnsenPlayerActivity extends Activity {
    /** Called when the activity is first created. */
    private String dow[] = new String[5];
	private ViewFlipper viewflipper;
	private View[] innerView; 
	private ProgramAdapter[] adapters;
	private GestureDetector gestureDetector;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        innerView = new View[5];
        adapters = new ProgramAdapter[5];
        setupdow();
        viewflipper = (ViewFlipper) findViewById(id.ViewFlipper1);
        LayoutInflater l = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				boolean ret =super.onFling(e1, e2, velocityX, velocityY);
				if (Math.abs(velocityY) < Math.abs(velocityX) && Math.abs(velocityX) > 10) {
					if (velocityX < 0) {
						viewflipper.setInAnimation(AnimationUtils.loadAnimation(OnsenPlayerActivity.this, R.anim.slide_in_right));
						viewflipper.showNext();
					} else {
						viewflipper.setInAnimation(AnimationUtils.loadAnimation(OnsenPlayerActivity.this, android.R.anim.slide_in_left));
						viewflipper.showPrevious();
					}
					ret = true;
				}
				return ret;
			}
		});
        	
        for (int i = 0; i < 5; i++) {
        	innerView[i] = l.inflate(R.layout.programlist, null);
        	TextView t = (TextView) innerView[i].findViewById(R.id.dayOfWeek);
        	t.setText(dow[i]);
        	viewflipper.addView(innerView[i]);
        	adapters[i] = new ProgramAdapter(this, (LinearLayout) findViewById(R.id.loadingView));
        	ListView list = (ListView) innerView[i].findViewById(R.id.programList);
        	list.setAdapter(adapters[i]);
        	list.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					gestureDetector.onTouchEvent(arg1);
					return false;
				}
        	});
        }
    	GregorianCalendar cal = new GregorianCalendar();
        int w = cal.get(Calendar.DAY_OF_WEEK);
        if (w < 2 || w > 6)
        	w = 6;
        while(w > 2) {
        	viewflipper.showNext();
        	w--;
        }
        GetPrograms p = new GetPrograms(this);
        p.execute(adapters);
    }
	private void setupdow() {
		// TODO Auto-generated method stub
		Calendar cal = new GregorianCalendar();
		for (int i = 0; i < 7; i++) {
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (dow >= Calendar.MONDAY && dow <= Calendar.FRIDAY)
				this.dow[dow - Calendar.MONDAY] = String.format("%1$tA", cal);
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
	}
}
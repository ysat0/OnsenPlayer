package jp.dip.ysato.onsenplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;

public class DetailActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        Intent intent = getIntent();
        final Bundle b = intent.getBundleExtra("program");
        String program[] = b.getStringArray("program");
        WebView wv = (WebView) findViewById(R.id.detailWebView);
        wv.loadUrl(program[0]);
        ImageButton btn = (ImageButton) findViewById(R.id.detailPlayButton);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
	        	Intent i = new Intent(DetailActivity.this, PlayActivity.class);
				i.putExtra("program", b);
				startActivity(i);
			}

        });
    }
}

package com.littleme.wallpaper;
import android.app.*;
import android.content.*;
import android.graphics.Canvas;
import android.os.*;
import android.view.View;
import android.widget.*;
public final class MainActivity extends Activity {
    private Preview preview;
    @Override public void onCreate(Bundle state){super.onCreate(state);LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setPadding(16,24,16,16);layout.setBackgroundColor(0xff241f29);TextView title=new TextView(this);title.setText("Little Me");title.setTextSize(26);title.setTextColor(0xffffe5ce);layout.addView(title);TextView text=new TextView(this);text.setText("A little life of its own. Preview runs at 30× speed.");text.setTextColor(0xffe5d8df);layout.addView(text);preview=new Preview();layout.addView(preview,new LinearLayout.LayoutParams(-1,0,1));Button apply=new Button(this);apply.setText("Set live wallpaper");apply.setOnClickListener(v->{try{Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,LittleWallpaper.class));startActivity(i);}catch(ActivityNotFoundException e){Toast.makeText(this,"Live wallpaper picker not available on this device",Toast.LENGTH_LONG).show();}});layout.addView(apply);TextView note=new TextView(this);note.setText("Choose Lock screen or Home and lock screens if offered by your phone.");note.setTextColor(0xffe5d8df);layout.addView(note);setContentView(layout);}
    @Override protected void onResume(){super.onResume();if(preview!=null){preview.active=true;preview.invalidate();}}
    @Override protected void onPause(){if(preview!=null)preview.active=false;super.onPause();}
    private final class Preview extends View {final Scene scene=new Scene(MainActivity.this);final long start=System.currentTimeMillis(),base=start;final World world=new World(System.nanoTime(),base);boolean active=true;Preview(){super(MainActivity.this);} @Override protected void onDraw(Canvas c){long now=System.currentTimeMillis();world.advance(base+Math.max(0,now-start)*30);scene.draw(c,world,getWidth(),getHeight(),now);if(active)postInvalidateDelayed(100);}}
}

package com.littleme.wallpaper;
import android.app.*;
import android.content.*;
import android.graphics.Canvas;
import android.os.*;
import android.view.*;
import android.widget.*;
public final class MainActivity extends Activity {
    private Preview preview;private TextView status;
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff241f29);getWindow().setNavigationBarColor(0xff241f29);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff241f29);root.setPadding(dp(12),dp(10),dp(12),dp(8));root.setOnApplyWindowInsetsListener((v,insets)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(i.left+dp(12),i.top+dp(8),i.right+dp(12),i.bottom+dp(8));}else v.setPadding(dp(12),insets.getSystemWindowInsetTop()+dp(8),dp(12),insets.getSystemWindowInsetBottom()+dp(8));return insets;});
      TextView title=new TextView(this);title.setText("Little Me");title.setTextSize(24);title.setTextColor(0xffffe5ce);root.addView(title);status=new TextView(this);status.setTextColor(0xffe5d8df);status.setTextSize(12);root.addView(status);preview=new Preview();root.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
      LinearLayout controls=new LinearLayout(this);Button mode=new Button(this);mode.setText("Demo mode");mode.setOnClickListener(v->{preview.world.demo=!preview.world.demo;mode.setText(preview.world.demo?"Demo mode":"Real time");preview.world.next();});controls.addView(mode,new LinearLayout.LayoutParams(0,-2,1));Button next=new Button(this);next.setText("Next activity");next.setOnClickListener(v->preview.world.next());controls.addView(next,new LinearLayout.LayoutParams(0,-2,1));root.addView(controls);
      Button apply=new Button(this);apply.setText("Set live wallpaper");apply.setOnClickListener(v->{try{Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,LittleWallpaper.class));startActivity(i);}catch(ActivityNotFoundException e){Toast.makeText(this,"Live wallpaper picker not available on this device",Toast.LENGTH_LONG).show();}});root.addView(apply);setContentView(root);root.requestApplyInsets();}
    int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    @Override protected void onResume(){super.onResume();if(preview!=null){preview.active=true;preview.invalidate();}}
    @Override protected void onPause(){if(preview!=null)preview.active=false;super.onPause();}
    private final class Preview extends View {final Scene scene=new Scene(MainActivity.this);final World world=new World(System.nanoTime(),System.currentTimeMillis());boolean active=true;String last="";Preview(){super(MainActivity.this);world.demo=true;}
      @Override protected void onDraw(Canvas c){long now=System.currentTimeMillis();world.advance(now);scene.draw(c,world,getWidth(),getHeight(),now);String label="You: "+(world.man.route.isEmpty()?world.name(world.man):"Walking")+"  ·  Dog: "+(world.dog.route.isEmpty()?world.name(world.dog):"Walking");if(!label.equals(last)){last=label;post(()->status.setText(label));}if(active)postInvalidateDelayed(66);}}
}

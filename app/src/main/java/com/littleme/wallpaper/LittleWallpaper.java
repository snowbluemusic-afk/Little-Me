package com.littleme.wallpaper;
import android.service.wallpaper.WallpaperService;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
public final class LittleWallpaper extends WallpaperService {
    @Override public Engine onCreateEngine(){return new HouseEngine();}
    private final class HouseEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private boolean visible=false,ready=false; private Scene scene; private World world; private SharedPreferences prefs;private long lastSave;private final Runnable frame=this::render;
        @Override public void onCreate(SurfaceHolder holder){super.onCreate(holder);prefs=getSharedPreferences("world_v3",MODE_PRIVATE);long now=System.currentTimeMillis();world=new World(System.nanoTime(),Math.min(now,prefs.getLong("time",now)));world.man.decode(prefs.getString("man",null),true);world.dog.decode(prefs.getString("dog",null),false);scene=new Scene(LittleWallpaper.this);setTouchEventsEnabled(false);}
        @Override public void onVisibilityChanged(boolean v){visible=v;handler.removeCallbacks(frame);if(v&&ready)render();else save();}
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){super.onSurfaceChanged(holder,format,width,height);ready=true;handler.removeCallbacks(frame);if(visible)render();}
        @Override public void onSurfaceDestroyed(SurfaceHolder holder){ready=false;handler.removeCallbacks(frame);save();super.onSurfaceDestroyed(holder);}
        @Override public void onDestroy(){handler.removeCallbacks(frame);save();super.onDestroy();}
        private void save(){if(world==null||prefs==null||isPreview())return;prefs.edit().putLong("time",world.time).putString("man",world.man.encode()).putString("dog",world.dog.encode()).apply();}
        private void render(){handler.removeCallbacks(frame);if(!visible||!ready)return;long now=System.currentTimeMillis();world.advance(now);SurfaceHolder holder=getSurfaceHolder();Canvas c=null;try{c=holder.lockCanvas();if(c!=null)scene.draw(c,world,c.getWidth(),c.getHeight(),now);}catch(IllegalArgumentException|IllegalStateException ignored){}finally{if(c!=null)holder.unlockCanvasAndPost(c);}if(now-lastSave>15000){save();lastSave=now;}if(visible&&ready)handler.postDelayed(frame,66);}
    }
}

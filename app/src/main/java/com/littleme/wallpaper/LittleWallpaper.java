package com.littleme.wallpaper;
import android.service.wallpaper.WallpaperService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.util.Log;
public final class LittleWallpaper extends WallpaperService {
    public static final String SETTINGS="wallpaper_settings", LIVELY="lively";
    @Override public Engine onCreateEngine(){return new HouseEngine();}
    private final class HouseEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private boolean visible=false,ready=false,destroyed=false; private Scene scene; private World world; private SharedPreferences prefs,settings;
        private long lastSave,lastError; private final Runnable frame=this::render;
        private final SharedPreferences.OnSharedPreferenceChangeListener settingsListener=(sp,key)->{if(LIVELY.equals(key))handler.post(()->{if(!destroyed&&world!=null){world.setLively(settings.getBoolean(LIVELY,true));restart();}});};
        @Override public void onCreate(SurfaceHolder holder){
            super.onCreate(holder);prefs=getSharedPreferences("world_v31",MODE_PRIVATE);settings=getSharedPreferences(SETTINGS,MODE_PRIVATE);
            long now=System.currentTimeMillis();world=new World(System.nanoTime(),Math.min(now,prefs.getLong("time",now)));
            world.man.decode(prefs.getString("man",null),true);world.dog.decode(prefs.getString("dog",null),false);
            world.setLively(settings.getBoolean(LIVELY,true));settings.registerOnSharedPreferenceChangeListener(settingsListener);
            scene=new Scene(LittleWallpaper.this);setTouchEventsEnabled(false);
        }
        private void restart(){handler.removeCallbacks(frame);if(!destroyed&&visible&&ready)handler.post(frame);}
        @Override public void onVisibilityChanged(boolean v){super.onVisibilityChanged(v);visible=v;if(v){ready=getSurfaceHolder().getSurface().isValid();world.setLively(settings.getBoolean(LIVELY,true));restart();}else{handler.removeCallbacks(frame);save();}}
        @Override public void onSurfaceCreated(SurfaceHolder holder){super.onSurfaceCreated(holder);ready=true;restart();}
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){super.onSurfaceChanged(holder,format,width,height);ready=true;restart();}
        @Override public void onSurfaceRedrawNeeded(SurfaceHolder holder){super.onSurfaceRedrawNeeded(holder);if(!destroyed&&ready){if(visible)restart();else drawOnce();}}
        @Override public void onSurfaceDestroyed(SurfaceHolder holder){ready=false;handler.removeCallbacks(frame);save();super.onSurfaceDestroyed(holder);}
        @Override public void onDestroy(){destroyed=true;visible=false;ready=false;handler.removeCallbacks(frame);if(settings!=null)settings.unregisterOnSharedPreferenceChangeListener(settingsListener);save();super.onDestroy();}
        private void save(){if(world==null||prefs==null||isPreview())return;prefs.edit().putLong("time",world.time).putString("man",world.man.encode()).putString("dog",world.dog.encode()).apply();}
        private void error(RuntimeException e){long now=SystemClock.elapsedRealtime();if(now-lastError>5000||lastError==0){lastError=now;Log.w("LittleMeWallpaper","Frame interrupted; will retry while visible",e);}}
        private void drawOnce(){
            if(world==null||scene==null)return;
            long now=System.currentTimeMillis();world.advance(now);
            SurfaceHolder holder=getSurfaceHolder();if(!holder.getSurface().isValid())return;
            Canvas c=null;try{c=holder.lockCanvas();if(c!=null)scene.draw(c,world,c.getWidth(),c.getHeight(),now);}
            catch(RuntimeException e){error(e);}finally{if(c!=null)try{holder.unlockCanvasAndPost(c);}catch(RuntimeException e){error(e);}}
        }
        private void render(){
            handler.removeCallbacks(frame);if(destroyed||!visible||!ready)return;long start=SystemClock.uptimeMillis();
            try{drawOnce();long now=SystemClock.elapsedRealtime();if(now-lastSave>10000){save();lastSave=now;}}
            catch(RuntimeException e){error(e);}finally{if(!destroyed&&visible&&ready)handler.postAtTime(frame,Math.max(SystemClock.uptimeMillis()+4,start+42));}
        }
    }
}

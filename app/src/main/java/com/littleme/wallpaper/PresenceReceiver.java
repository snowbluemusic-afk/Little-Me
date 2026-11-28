package com.littleme.wallpaper;
import android.content.*;
import com.google.android.gms.location.*;
public final class PresenceReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){
  if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction())){PendingResult result=goAsync();Presence.register(c,result::finish);return;}
  if(!"com.littleme.HOME_FENCE".equals(i.getAction())||!Presence.prefs(c).getBoolean("auto",false)||!Presence.allowed(c))return;
  GeofencingEvent event=GeofencingEvent.fromIntent(i);if(event==null)return;
  int state=World.UNKNOWN;if(!event.hasError()){int t=event.getGeofenceTransition();if(t==Geofence.GEOFENCE_TRANSITION_ENTER)state=World.HOME;else if(t==Geofence.GEOFENCE_TRANSITION_EXIT)state=World.AWAY;}
  Presence.put(c,"local",state);Presence.publish(c,Presence.self(c));
 }
}

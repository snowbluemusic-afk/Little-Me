package com.littleme.wallpaper;
import android.content.*;
import android.os.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import com.google.android.gms.location.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import org.json.JSONObject;
final class Presence {
 static final String STORE="presence_v4", HOME_ID="little_me_home";
 static final long TTL=72L*3600*1000, POLL=30000;
 static final ExecutorService IO=Executors.newSingleThreadExecutor();
 static SharedPreferences prefs(Context c){return c.getSharedPreferences(STORE,Context.MODE_PRIVATE);}
 static int state(String s){return "home".equals(s)?World.HOME:"away".equals(s)?World.AWAY:World.UNKNOWN;}
 static String name(int s){return s==World.HOME?"home":s==World.AWAY?"away":"unknown";}
 static int get(Context c,String key){SharedPreferences p=prefs(c);long t=p.getLong(key+"_time",0);if(t<=0||System.currentTimeMillis()-t>TTL)return World.UNKNOWN;return p.getInt(key,World.UNKNOWN);}
 static int self(Context c){SharedPreferences p=prefs(c);int manual=p.getInt("self_manual",World.UNKNOWN);if(manual!=World.UNKNOWN)return manual;if(p.getBoolean("auto",false)&&allowed(c)){android.location.LocationManager lm=(android.location.LocationManager)c.getSystemService(Context.LOCATION_SERVICE);if(lm!=null&&lm.isLocationEnabled())return get(c,"local");}return get(c,"remote_self");}
 static int partner(Context c){SharedPreferences p=prefs(c);if(!p.getBoolean("partner_consent",false))return World.UNKNOWN;return get(c,"remote_partner");}
 static void put(Context c,String key,int value){prefs(c).edit().putInt(key,value).putLong(key+"_time",System.currentTimeMillis()).apply();}
 static boolean allowed(Context c){return c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED&&(Build.VERSION.SDK_INT<29||c.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED);}
 static PendingIntent pending(Context c){Intent i=new Intent(c,PresenceReceiver.class).setAction("com.littleme.HOME_FENCE");return PendingIntent.getBroadcast(c,90,i,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));}
 static void register(Context c,Runnable done){
  SharedPreferences p=prefs(c);if(!p.getBoolean("auto",false)||!allowed(c)||!p.contains("latitude")){if(done!=null)done.run();return;}
  double lat=Double.longBitsToDouble(p.getLong("latitude",0)),lon=Double.longBitsToDouble(p.getLong("longitude",0));float radius=p.getFloat("radius",200);
  try{Geofence g=new Geofence.Builder().setRequestId(HOME_ID).setCircularRegion(lat,lon,radius).setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT).setNotificationResponsiveness(60000).build();
   GeofencingRequest req=new GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER|GeofencingRequest.INITIAL_TRIGGER_EXIT).addGeofence(g).build();
   LocationServices.getGeofencingClient(c).addGeofences(req,pending(c)).addOnSuccessListener(v->{p.edit().putString("fence_status","Registered").apply();if(done!=null)done.run();}).addOnFailureListener(e->{p.edit().putString("fence_status","Registration failed").apply();put(c,"local",World.UNKNOWN);if(done!=null)done.run();});
  }catch(SecurityException e){put(c,"local",World.UNKNOWN);if(done!=null)done.run();}
 }
 static void disable(Context c){prefs(c).edit().putBoolean("auto",false).remove("latitude").remove("longitude").remove("local").remove("local_time").apply();LocationServices.getGeofencingClient(c).removeGeofences(pending(c));publish(c,World.UNKNOWN);}
 static String role(Context c){return prefs(c).getString("role","self");}
 static JSONObject request(Context c,String method,String path,String token,JSONObject data)throws Exception{
  String base=prefs(c).getString("relay","").trim();URI uri=new URI(base);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null||!(uri.getPath().isEmpty()||uri.getPath().equals("/")))throw new IOException("HTTPS relay origin required");
  URL url=new URL(base.replaceAll("/+$","")+path);HttpURLConnection conn=(HttpURLConnection)url.openConnection();try{conn.setInstanceFollowRedirects(false);conn.setConnectTimeout(4000);conn.setReadTimeout(4000);conn.setRequestMethod(method);conn.setRequestProperty("Authorization","Bearer "+token);conn.setRequestProperty("Accept","application/json");
   if(data!=null){conn.setDoOutput(true);conn.setRequestProperty("Content-Type","application/json");try(OutputStream out=conn.getOutputStream()){out.write(data.toString().getBytes(StandardCharsets.UTF_8));}}
   if(conn.getResponseCode()!=200)throw new IOException("Relay request failed");try(InputStream in=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[1024];int n;while((n=in.read(b))!=-1){if(out.size()+n>8192)throw new IOException("Response too large");out.write(b,0,n);}return new JSONObject(out.toString("UTF-8"));}
  }finally{conn.disconnect();}
 }
 static void publish(Context c,int value){Context app=c.getApplicationContext();SharedPreferences p=prefs(app);String token=p.getString("write","");if(!p.getBoolean("sharing",false)||token.isEmpty())return;String role=role(app);IO.execute(()->{try{request(app,"POST","/v1/presence/"+role,token,new JSONObject().put("state",name(value)));p.edit().putString("sync_status","Published "+name(value)).apply();}catch(Exception e){p.edit().putString("sync_status","Publish failed").apply();}});}
 static void refresh(Context c){Context app=c.getApplicationContext();SharedPreferences p=prefs(app);String token=p.getString("read","");if(token.isEmpty()||!p.getBoolean("partner_consent",false))return;String endpoint=p.getString("relay","");String role=role(app);IO.execute(()->{try{JSONObject j=request(app,"GET","/v1/presence",token,null);if(!endpoint.equals(p.getString("relay",""))||!token.equals(p.getString("read",""))||!p.getBoolean("partner_consent",false))return;String own=role.equals("self")?"self":"partner",other=role.equals("self")?"partner":"self";put(app,"remote_self",state(j.optString(own)));put(app,"remote_partner",state(j.optString(other)));p.edit().putString("sync_status","Connected").apply();}catch(Exception e){put(app,"remote_self",World.UNKNOWN);put(app,"remote_partner",World.UNKNOWN);p.edit().putString("sync_status","Relay unavailable").apply();}});}
 static void apply(Context c,World w){if(!role(c).equals("self"))w.setPresence(partner(c),self(c));else w.setPresence(self(c),partner(c));}
}

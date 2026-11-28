package com.littleme.wallpaper;
import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.CancellationTokenSource;
public final class PresenceActivity extends Activity {
 private SharedPreferences prefs;private TextView status;private EditText relay,read,write;private Spinner role;private CheckBox consent,sharing;
 private final Handler ui=new Handler(Looper.getMainLooper());private final Runnable update=new Runnable(){public void run(){showStatus();ui.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=Presence.prefs(this);
  ScrollView scroll=new ScrollView(this);LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(24,48,24,40);scroll.addView(l);
  TextView title=new TextView(this);title.setText("Household presence");title.setTextSize(24);l.addView(title);
  note(l,"Optional. Each person must choose to share their own Home/Away status. GPS coordinates stay on the Android device. Without a configured connection, unknown presence is simulated.");
  status=new TextView(this);l.addView(status);
  button(l,"I am home",()->manual(World.HOME));button(l,"I am away",()->manual(World.AWAY));button(l,"Use automatic / unknown",()->manual(World.UNKNOWN));
  note(l,"Automatic location: stand at home, grant precise location, then allow background location in Android app permissions. The home zone is 200 metres.");
  button(l,"1. Grant location permission",()->requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},101));
  button(l,"2. Allow background location in Settings",()->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));
  button(l,"3. Set home here and enable geofence",this::setHome);button(l,"Disable geofence / erase home coordinates",()->{Presence.disable(this);showStatus();});
  note(l,"Optional remote household relay. Use the HTTPS address and separate read/write tokens from your own deployed relay. Choose the phone owner's role. Maddie must opt in on her phone before you connect.");
  role=new Spinner(this);role.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"My phone (owner)","Maddie's phone"}));role.setSelection(Presence.role(this).equals("partner")?1:0);l.addView(role);
  relay=field(l,"HTTPS relay origin",prefs.getString("relay",""),false);read=field(l,"Household read token",prefs.getString("read",""),true);write=field(l,"My private write token",prefs.getString("write",""),true);
  consent=new CheckBox(this);consent.setText("We both agree to share home/away");consent.setChecked(prefs.getBoolean("partner_consent",false));l.addView(consent);
  sharing=new CheckBox(this);sharing.setText("Share my own home/away status");sharing.setChecked(prefs.getBoolean("sharing",false));l.addView(sharing);
  button(l,"Save and connect",()->{String url=relay.getText().toString().trim();if(!url.isEmpty()&&!url.startsWith("https://")){Toast.makeText(this,"Use an HTTPS relay origin",Toast.LENGTH_LONG).show();return;}prefs.edit().putString("role",role.getSelectedItemPosition()==0?"self":"partner").putString("relay",url).putString("read",read.getText().toString().trim()).putString("write",write.getText().toString().trim()).putBoolean("partner_consent",consent.isChecked()).putBoolean("sharing",sharing.isChecked()).remove("remote_self").remove("remote_partner").apply();Presence.publish(this,Presence.self(this));Presence.refresh(this);showStatus();});
  button(l,"Stop sharing and disconnect",()->{Presence.publish(this,World.UNKNOWN);prefs.edit().putBoolean("sharing",false).putBoolean("partner_consent",false).remove("read").remove("write").remove("relay").remove("remote_self").remove("remote_partner").apply();consent.setChecked(false);sharing.setChecked(false);relay.setText("");read.setText("");write.setText("");showStatus();});
  setContentView(scroll);showStatus();
 }
 private void note(LinearLayout l,String t){TextView v=new TextView(this);v.setText(t);v.setPadding(0,12,0,12);l.addView(v);}
 private void button(LinearLayout l,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setOnClickListener(v->action.run());l.addView(b);}
 private EditText field(LinearLayout l,String hint,String value,boolean secret){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setInputType(secret?InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);e.setText(value);l.addView(e);return e;}
 private void manual(int value){prefs.edit().putInt("self_manual",value).apply();Presence.publish(this,Presence.self(this));showStatus();}
 private void showStatus(){if(status!=null)status.setText("This phone: "+Presence.name(Presence.self(this))+" · Other phone: "+Presence.name(Presence.partner(this))+"\n"+prefs.getString("fence_status","Geofence not registered")+" · "+prefs.getString("sync_status","Not connected"));}
 private void setHome(){if(!Presence.allowed(this)){Toast.makeText(this,"Grant precise and background location first",Toast.LENGTH_LONG).show();return;}try{LocationServices.getFusedLocationProviderClient(this).getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,new CancellationTokenSource().getToken()).addOnSuccessListener(location->{if(location==null||location.getAccuracy()>200){Toast.makeText(this,"No accurate fix. Try again outdoors or near a window.",Toast.LENGTH_LONG).show();return;}prefs.edit().putLong("latitude",Double.doubleToLongBits(location.getLatitude())).putLong("longitude",Double.doubleToLongBits(location.getLongitude())).putFloat("radius",200).putBoolean("auto",true).putInt("self_manual",World.UNKNOWN).apply();Presence.put(this,"local",World.HOME);Presence.register(this,this::showStatus);Presence.publish(this,World.HOME);showStatus();}).addOnFailureListener(e->Toast.makeText(this,"Location unavailable",Toast.LENGTH_LONG).show());}catch(SecurityException e){Toast.makeText(this,"Location permission missing",Toast.LENGTH_LONG).show();}}
 @Override protected void onResume(){super.onResume();Presence.refresh(this);ui.post(update);}
 @Override protected void onPause(){ui.removeCallbacks(update);super.onPause();}
}

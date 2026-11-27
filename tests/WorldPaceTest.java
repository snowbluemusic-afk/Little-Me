package com.littleme.wallpaper;
import java.util.*;
public final class WorldPaceTest {
 public static void main(String[] args){
  Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,13);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);long t=cal.getTimeInMillis();
  World w=new World(51,t);w.previewActivity(true,0);w.previewActivity(false,1);long normal=w.man.dwell;if(normal<70000)throw new AssertionError("normal pace");w.setLively(true);
  if(w.man.dwell>24000||w.dog.dwell>18000)throw new AssertionError("old timer not capped");
  Set<Integer> h=new HashSet<>(),d=new HashSet<>();boolean walking=false;for(int i=1;i<=1800;i++){w.advance(t+i*1000L);h.add(w.man.job);d.add(w.dog.job);walking|=!w.man.route.isEmpty();if(w.man.route.isEmpty()&&!w.man.away&&w.man.job!=6&&w.man.until-w.time>24000)throw new AssertionError("ordinary human dwell too long");}
  if(h.size()<5||d.size()<5||!walking)throw new AssertionError("insufficient variety");
  String man=w.man.encode(),dog=w.dog.encode();World restored=new World(63,w.time);restored.man.decode(man,true);restored.dog.decode(dog,false);restored.setLively(true);long before=restored.time;restored.advance(before+3600000L);if(restored.time!=before+3600000L)throw new AssertionError("hidden catch-up failed");
  w.setLively(false);w.man.route.clear();w.previewActivity(true,0);if(w.man.dwell<70000)throw new AssertionError("real time option broken");
  System.out.println("PASS: lively pacing, old timer cap, activity variety, saved-state catch-up and real-time option");
 }
}
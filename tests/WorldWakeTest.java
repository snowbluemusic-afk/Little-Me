package com.littleme.wallpaper;
import java.util.*;
public class WorldWakeTest {
 public static void main(String[] args){
  Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,13);long t=c.getTimeInMillis();
  World w=new World(72,t);w.setLively(true);Set<Integer> groups=new HashSet<>();boolean away=false,pet=false;
  for(int i=0;i<500;i++){int before=w.visualGroup();w.wakeScene(t+i*1000L,before);int after=w.visualGroup();if(before==after)throw new AssertionError("same visible scene");groups.add(after);away|=w.man.away;pet|=w.petting();if(!w.man.route.isEmpty()||!w.dog.route.isEmpty())throw new AssertionError("not settled");for(World.Actor a:new World.Actor[]{w.man,w.dog}){if(a.x!=World.POS[a.node][0]||a.y!=World.POS[a.node][1]||a.until<=w.time)throw new AssertionError("invalid anchor or timer");}if(w.man.away&&w.man.node!=World.DOOR)throw new AssertionError("away anchor");if(w.man.job==4&&!w.petting())throw new AssertionError("unpaired play");}
  if(groups.size()<8||!away||!pet)throw new AssertionError("low variety");
  String m=w.man.encode(),d=w.dog.encode();World r=new World(91,w.time);r.man.decode(m,true);r.dog.decode(d,false);r.setLively(true);int prev=r.visualGroup();r.wakeScene(w.time+2000,prev);if(r.visualGroup()==prev)throw new AssertionError("repeated restored scene");r.advance(r.time+180000L);
  System.out.println("PASS: 500 wake scenes, "+groups.size()+" visual groups, no consecutive repeats, consistent positions, paired activities, state restoration and continued motion");
 }
}
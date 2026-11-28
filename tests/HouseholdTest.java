package com.littleme.wallpaper;
import java.util.*;
public class HouseholdTest {
 public static void main(String[] args){
  Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,13);long t=cal.getTimeInMillis();World w=new World(93,t);w.setHousehold(true);w.setLively(true);
  int[] counts=new int[9];w.setPresence(World.HOME,World.HOME);
  for(int i=0;i<400;i++){int prev=w.visualGroup();w.wakeScene(t+i*1000L,prev);if(w.man.away||w.maddie.away)throw new AssertionError("known home disappeared");int g=w.visualGroup();if(g==prev)throw new AssertionError("repeat");counts[g]++;if(w.man.node==w.maddie.node)throw new AssertionError("shared human anchor");}
  int min=1000,max=0;for(int i=0;i<8;i++){min=Math.min(min,counts[i]);max=Math.max(max,counts[i]);}if(max-min>2)throw new AssertionError("unbalanced: "+Arrays.toString(counts));
  for(int self=0;self<=1;self++)for(int partner=0;partner<=1;partner++){w.setPresence(self,partner);for(int i=0;i<50;i++){int prev=w.visualGroup();w.wakeScene(w.time+1000,prev);if(w.man.away!=(self==0)||w.maddie.away!=(partner==0))throw new AssertionError("presence mismatch");w.advance(w.time+5000);if(w.man.away!=(self==0)||w.maddie.away!=(partner==0))throw new AssertionError("presence changed while visible");}}
  World r=new World(42,w.time);r.restoreRotation(w.rotation());if(!r.rotation().equals(w.rotation()))throw new AssertionError("rotation restore");
  System.out.println("PASS household presence matrix and balanced wake groups "+Arrays.toString(counts));
 }
}
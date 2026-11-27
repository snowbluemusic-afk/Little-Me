package com.littleme.wallpaper;
import java.util.*;
public class WorldAnchorTest {
 public static void main(String[] args){
  if(World.POS[World.SINK][1]<463||World.POS[World.SINK][0]>73)throw new AssertionError("sink feet not on floor");
  if(World.POS[World.WINDOW][1]<280||World.POS[World.WINDOW][0]<143)throw new AssertionError("window feet on cabinet");
  if(World.POS[World.SOFA][1]<318||World.POS[World.SOFA_DOG][1]>310)throw new AssertionError("sofa anchors");
  long t=System.currentTimeMillis();World w=new World(32,t);w.setLively(true);
  for(int i=0;i<500;i++){int last=w.visualGroup();w.wakeScene(t+i*1000L,last);if(w.visualGroup()==last)throw new AssertionError("repeat");
   for(World.Actor a:new World.Actor[]{w.man,w.dog})if(a.x!=World.POS[a.node][0]||a.y!=World.POS[a.node][1])throw new AssertionError("anchor mismatch");
   if(!w.man.away&&w.man.node==w.dog.node)throw new AssertionError("shared anchor");
  }
  System.out.println("PASS: revised floor and seat anchors, wake variety and actor separation");
 }
}
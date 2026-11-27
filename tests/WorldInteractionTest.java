package com.littleme.wallpaper;
public class WorldInteractionTest {
 public static void main(String[] args){long start=System.currentTimeMillis();World w=new World(42,start);w.demo=true;boolean pet=false,zoom=false;for(int i=0;i<36000;i++){w.advance(start+i*1000L);pet|=w.petting();zoom|=w.dog.job==8&&!w.dog.route.isEmpty();if(w.man.away&&w.man.node!=World.DOOR)throw new AssertionError("away outside door");}if(!pet||!zoom)throw new AssertionError("interaction not observed: "+pet+" "+zoom);System.out.println("PASS coordinated petting and zoomies routes");}
}

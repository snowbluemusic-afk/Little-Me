package com.littleme.wallpaper;
public class WorldV3Test {
 public static void main(String[] args){
 for(int i=0;i<World.POS.length;i++)for(int j=0;j<World.POS.length;j++){int last=i;for(int n:World.path(i,j)){boolean edge=false;for(int q:World.EDGES[last])if(q==n)edge=true;if(!edge)throw new AssertionError("invalid route");last=n;}if(last!=j)throw new AssertionError("disconnected");}
 if(!World.path(World.LOUNGE,World.RUG).contains(World.STAIR_TOP)||!World.path(World.LOUNGE,World.RUG).contains(World.STAIR_BOTTOM))throw new AssertionError("stairs bypassed");
 if(World.companionNode(World.SOFA)!=World.SOFA_DOG||World.companionNode(World.SINK)!=World.KITCHEN)throw new AssertionError("companion anchor");
 long t=System.currentTimeMillis();World w=new World(44,t);w.demo=true;w.previewActivity(true,0);w.previewActivity(false,10);boolean together=false,returned=false;for(int i=1;i<36000;i++){w.advance(t+i*1000L);together|=w.man.node==World.SOFA&&w.dog.node==World.SOFA_DOG&&w.man.route.isEmpty()&&w.dog.route.isEmpty();returned|=w.man.job==16;if(w.man.away&&w.man.node!=World.DOOR)throw new AssertionError("away anchor");}if(!together||!returned)throw new AssertionError("missing company or return");System.out.println("PASS: all connected routes, staircase nodes, company anchors and returning home");
 }
}
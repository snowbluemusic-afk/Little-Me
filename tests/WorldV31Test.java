package com.littleme.wallpaper;
public class WorldV31Test {
 public static void main(String[] args){
  for(int from=0;from<World.POS.length;from++)for(int to=0;to<World.POS.length;to++){
   int last=from; for(int n:World.path(from,to)){if(World.furniture(n)&&n!=to)throw new AssertionError("route crosses furniture "+from+" "+to+" via "+n);boolean valid=false;for(int e:World.EDGES[last])if(e==n)valid=true;if(!valid)throw new AssertionError("invalid edge");last=n;}if(last!=to)throw new AssertionError("unreachable");
  }
  if(World.transition(World.SOFA_ENTRY,World.SOFA)!=World.SIT_SOFA||World.transition(World.SOFA_DOG,World.SOFA_DOG_ENTRY)!=World.JUMP_SOFA||World.transition(World.TABLE_ENTRY,World.TABLE)!=World.SIT_TABLE||World.transition(World.BED_ENTRY,World.BED)!=World.BED_TRANSFER)throw new AssertionError("wrong transition");
  long t=System.currentTimeMillis();World w=new World(42,t);w.demo=true;w.previewActivity(true,0);w.previewActivity(false,10);boolean sit=false,jump=false,together=false;for(int i=0;i<6000;i++){w.advance(t+i*50L);if(!w.man.route.isEmpty()&&World.transition(w.man.node,w.man.route.peek())==World.SIT_SOFA)sit=true;if(!w.dog.route.isEmpty()&&World.transition(w.dog.node,w.dog.route.peek())==World.JUMP_SOFA)jump=true;if(w.man.node==World.SOFA&&w.dog.node==World.SOFA_DOG&&w.man.route.isEmpty()&&w.dog.route.isEmpty())together=true;if(World.progress(w.man)<0||World.progress(w.man)>1)throw new AssertionError("invalid transition progress");}
  if(!sit||!jump||!together)throw new AssertionError("missing transitions "+sit+" "+jump+" "+together);
  System.out.println("PASS: furniture destinations, route connectivity, sit/jump transitions and progress");
 }
}
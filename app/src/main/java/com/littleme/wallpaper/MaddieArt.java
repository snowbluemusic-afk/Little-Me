package com.littleme.wallpaper;
import android.graphics.*;
/** Original compact pixel-art character, navy graphic shirt with pink trim and silver-blonde wavy hair. */
final class MaddieArt {
 static final Paint P=new Paint();static final int OUT=0xff30282d,HAIR=0xff77736c,SILVER=0xffbab4a5,LIGHT=0xffd8ceba,SKIN=0xffd89c85,FACE=0xffedb8a0,SHIRT=0xff263044,SHADOW=0xff182334,PINK=0xffdf91b1,JEANS=0xff53647b,SHOE=0xff393038,TAT=0xff596b63;
 static void box(Canvas c,float x,float y,float w,float h,int col){P.setColor(col);P.setAntiAlias(false);c.drawRect(Math.round(x),Math.round(y),Math.round(x+w),Math.round(y+h),P);}
 static void poly(Canvas c,int col,float... pts){Path p=new Path();p.moveTo(pts[0],pts[1]);for(int i=2;i<pts.length;i+=2)p.lineTo(pts[i],pts[i+1]);p.close();P.setColor(col);c.drawPath(p,P);}
 static void hair(Canvas c,boolean back){
  poly(c,OUT,-10,-52,-6,-57,5,-57,10,-53,13,-45,13,-35,9,-30,-10,-30,-14,-36,-12,-48);
  poly(c,HAIR,-10,-51,-5,-55,6,-55,10,-50,11,-39,8,-32,4,-37,-6,-33,-11,-35,-10,-42);
  poly(c,SILVER,-9,-51,-5,-54,2,-54,-3,-49,-8,-45,-8,-37,-11,-39);
  poly(c,SILVER,3,-54,8,-52,11,-46,10,-39,7,-34,8,-44,5,-49);
  box(c,-7,-52,2,5,LIGHT);box(c,7,-47,2,8,LIGHT);
  if(!back){poly(c,SKIN,-6,-48,3,-50,8,-46,8,-39,4,-35,-3,-35,-8,-40,-8,-45);poly(c,FACE,-5,-47,4,-48,7,-44,6,-39,3,-36,-3,-37,-6,-41);box(c,-5,-44,2,1,OUT);box(c,3,-44,2,1,OUT);box(c,0,-42,2,2,SKIN);box(c,-2,-39,5,1,0xff804f50);box(c,-1,-39,3,1,0xfff1dac9);box(c,6,-41,1,2,0xffddd0be);}
  else {box(c,-2,-52,2,14,SILVER);box(c,4,-49,2,12,HAIR);box(c,-7,-43,2,10,LIGHT);}
 }
 static void draw(Canvas c,World w,World.Actor a,long now){
  float x=a.x,y=a.y;boolean moving=!a.route.isEmpty(),sit=!moving&&(a.node==World.M_SOFA||a.node==World.M_TABLE),sleep=!moving&&a.job==6,pet=!moving&&a.job==4,back=a.node==World.M_SOFA||a.node==World.M_SINK||a.node==World.M_WINDOW;
  if(moving){int next=a.route.peek();back=World.POS[next][1]<y-3;if(World.transition(a.node,next)!=World.WALK){float t=World.progress(a);sit=(next==World.M_SOFA||next==World.M_TABLE)?t>.5:a.node==World.M_SOFA||a.node==World.M_TABLE?t<.5:false;}}
  if(sleep){c.save();c.translate(272,432);c.rotate(-25);c.scale(.40f,.40f);hair(c,false);c.restore();poly(c,0xffa99ab1,270,433,289,444,261,458,242,448);poly(c,0xffc4b4ca,270,432,287,443,261,456,244,447);return;}
  P.setColor(0x49302529);if(!sit)c.drawOval(x-10,y-2,x+10,y+3,P);
  int frame=(int)(now/130%4),stride=moving?(frame==0?-3:frame==2?3:0):0;
  c.save();c.translate(Math.round(x),Math.round(y));c.scale(a.face*(pet?.91f:.91f),pet?.73f:sit?.68f:.91f);
  if(moving)c.translate(0,-(frame%2));
  // legs and shoes
  poly(c,OUT,-10,-21,10,-21,10,-6+stride,7,0+stride,0,0+stride,-1,-8-stride,-3,-1-stride,-10,-1-stride,-11,-6-stride);
  poly(c,JEANS,-9,-22,-1,-22,-2,-7-stride,-3,-3-stride,-9,-3-stride);
  poly(c,JEANS,1,-22,9,-22,8,-3+stride,2,-3+stride);
  box(c,-11,-4-stride,10,3,SHOE);box(c,1,-3+stride,10,3,SHOE);
  // torso and separate swinging arms
  int swing=moving?stride:(!sit&&(a.job==1||a.job==2||pet)?(int)(now/180%3):0);
  poly(c,OUT,-8,-35,-15,-31,-16,-24+swing,-12,-15+swing,-7,-17+swing,-6,-26,7,-26,11,-16-swing,16,-17-swing,16,-25-swing,12,-33,7,-35);
  poly(c,SHIRT,-8,-34,-14,-30,-14,-25+swing,-9,-23+swing,-7,-28);
  poly(c,SHIRT,8,-34,13,-31,15,-25-swing,10,-23-swing,7,-28);
  box(c,-14,-25+swing,6,3,PINK);box(c,10,-25-swing,5,3,PINK);box(c,-13,-22+swing,4,6,SKIN);box(c,11,-22-swing,4,6,FACE);box(c,-13,-20+swing,2,2,TAT);box(c,12,-20-swing,2,2,TAT);
  poly(c,OUT,-9,-36,9,-36,12,-24,11,-17,-11,-17,-12,-24);
  poly(c,SHIRT,-8,-35,8,-35,10,-25,9,-19,-9,-19,-10,-25);
  box(c,-8,-20,16,2,SHADOW);box(c,-3,-35,6,2,PINK);
  if(!back){box(c,-4,-32,1,5,0xffbfb6ad);box(c,3,-32,1,5,0xffbfb6ad);box(c,-1,-27,3,3,0xffc9c6ba);poly(c,0xffe692ae,-7,-25,-4,-28,0,-25,-1,-22,-6,-22);poly(c,0xff60a879,1,-26,5,-29,7,-27,5,-23,2,-22);}
  hair(c,back);
  if(sit&&a.job==3){box(c,-8,-22,16,8,0xff9e704c);box(c,-7,-22,7,5,0xffe5cfaa);box(c,1,-22,6,5,0xfff5dfbb);}
  if(sit&&a.job==5||a.job==2){box(c,8,-22,5,5,0xffece0cb);box(c,13,-21,2,3,0xffece0cb);}
  if(sit&&a.job==13){box(c,-9,-22,18,8,0xff393743);box(c,-7,-21,14,5,0xff92a5c5);}
  c.restore();
 }
}

package com.littleme.wallpaper;
import android.content.Context;
import android.graphics.*;
import java.io.IOException;
import java.util.*;
final class Scene {
    final Bitmap bg,person,pet;final Bitmap[] walk=new Bitmap[4],dogwalk=new Bitmap[4];final Map<String,Bitmap> poses=new HashMap<>();
    final Paint p=new Paint(); final RectF dst=new RectF();final Rect src=new Rect(); int spriteAlpha=255;
    Scene(Context c){bg=load(c,"lm_bg.png");person=load(c,"lm_man.png");pet=load(c,"lm_dog.png");for(int i=0;i<4;i++){walk[i]=load(c,"walk"+i+".png");dogwalk[i]=load(c,"dogwalk"+i+".png");}for(String n:new String[]{"wash","seat","pet","rear","dogsit","dogplay","dogsleep","dogbelly","wash0","wash1","wash2","seat0","seat1","seat2","pet0","pet1","pet2"})poses.put(n,load(c,n+".png"));p.setAntiAlias(false);p.setFilterBitmap(false);}
    Bitmap load(Context c,String n){try(java.io.InputStream s=c.getAssets().open(n)){Bitmap b=BitmapFactory.decodeStream(s);if(b==null)throw new IOException(n);return b;}catch(IOException e){throw new IllegalStateException("Missing artwork: "+n,e);}}
    void draw(Canvas c,World w,int width,int height,long now){if(width<=0||height<=0)return;Calendar cal=Calendar.getInstance();cal.setTimeInMillis(now);int hour=cal.get(Calendar.HOUR_OF_DAY);int shade=hour<6||hour>=21?46:hour>=19||hour<8?18:0;
      c.drawColor(Color.rgb(119,96,118));float s=Math.min(width/309f,height/553f),left=(width-309*s)/2,top=height-553*s;
      c.save();c.translate(left,top);c.scale(s,s);p.setColor(Color.WHITE);dst.set(0,0,309,553);c.drawBitmap(bg,null,dst,p);environment(c,w,now);
      // Paint upper-room actors before foreground objects, then lower room. Feet always sort in depth order.
      World.Actor first=w.man.y<=w.dog.y?w.man:w.dog,second=first==w.man?w.dog:w.man;
      layeredActor(c,w,first,now);layeredActor(c,w,second,now);
      if(shade>0){p.setColor((shade<<24)|0x25304d);c.drawRect(0,0,309,553,p);}c.restore();
      if(shade>0){p.setColor((shade<<24)|0x25304d);if(top>0)c.drawRect(0,0,width,top,p);if(left>0){c.drawRect(0,top,left,height,p);c.drawRect(width-left,top,width,height,p);}}
    }
    void layeredActor(Canvas c,World w,World.Actor a,long now){
      if(a==w.man&&a.away)return;
      // Clip only this actor; repainting the background here used to erase the other character.
      boolean sofa=a.node==World.SOFA||a.node==World.SOFA_DOG||(!a.route.isEmpty()&&(a.route.peek()==World.SOFA||a.route.peek()==World.SOFA_DOG));
      boolean table=a.node==World.TABLE||(!a.route.isEmpty()&&a.route.peek()==World.TABLE);
      c.save();
      if(sofa||a.y<323&&a.x>=55&&a.x<=160)c.clipOutPath(path(54,319,77,331,82,332,153,296,156,325,83,359,55,346));
      if(a.y<324&&a.x>143&&a.x<200)c.clipOutPath(path(158,281,171,276,181,283,182,295,174,306,173,318,161,324,151,318,150,303,147,293));
      if(table||a.y<493&&a.y>445&&a.x<160)c.clipOutPath(path(49,483,56,477,83,470,112,470,132,477,143,490,137,503,116,514,86,515,61,507,48,495));
      boolean bed=a==w.man&&(a.node==World.BED||(!a.route.isEmpty()&&a.route.peek()==World.BED));
      if(!bed&&a.y<485&&a.x>200&&a.y>404)c.clipOutPath(path(224,448,252,430,283,436,309,454,309,498,263,525,218,498,215,463));
      actor(c,w,a,now);
      c.restore();
      if(a==w.man&&a.route.isEmpty()&&a.node==World.TABLE)tableAction(c,a.job,now);
    }
    void environment(Canvas c,World w,long now){
      // TV is permanently on the original console stand, not a replacement room.
      poly(c,0xff29242d,69,255,110,237,110,263,69,281);poly(c,0xff494251,72,255,107,240,107,261,72,277);
      boolean gaming=!w.man.away&&w.man.job==0&&w.man.route.isEmpty();if(gaming){poly(c,0xff55548b,73,256,106,242,106,260,73,275);int tick=(int)(now/180%15);for(int i=0;i<4;i++)rect(c,77+i*7,258-i*3+(i%2),3,2,0xffc9d4e6);rect(c,77+tick,267-tick/2,4,3,0xffeac070);rect(c,88,261,3,4,0xff91cb8c);}
      // Flicker in the existing fireplace, steam from chimney, subtle window stars.
      int flame=(int)(now/130%4);rect(c,38,289-flame,3,8+flame,0xffffbd58);rect(c,43,285+flame,2,10-flame,0xfff86c3a);rect(c,48,288-(flame%2),2,6,0xffffd67d);
      for(int i=0;i<3;i++){int off=(int)((now/180+i*13)%33);rect(c,32+off/3,99-off,3+i,2+i,0x329bd0d9);}
      if(!w.man.away&&((w.man.job==8&&w.man.y<330)||w.man.job==16&&w.man.node==World.DOOR))poly(c,0xff3c3034,250,276,268,287,268,316,250,307);
      int candle=(int)(now/170%3);rect(c,94,473-candle,2,3+candle,0xffffdf91);rect(c,95,475,1,2,0xfffff4cd);
      // Window illumination follows local time; don't cover the window frame.
      Calendar clock=Calendar.getInstance();clock.setTimeInMillis(now);int hour=clock.get(Calendar.HOUR_OF_DAY);
      if(hour>=7&&hour<18){poly(c,0x226ac8ec,79,232,91,226,91,247,79,253);poly(c,0x226ac8ec,97,223,113,216,113,238,97,245);}
      if(hour>=19||hour<7){p.setColor(0x20ffb45e);c.drawOval(248,377,298,423,p);}
    }
    void actor(Canvas c,World w,World.Actor a,long now){boolean human=a==w.man;if(human&&a.away)return;boolean move=!a.route.isEmpty();float x=Math.round(a.x),y=Math.round(a.y);int phase=(int)(now/120%4);int face=a.face;
      if(move){
        int to=a.route.peek(),kind=World.transition(a.node,to);
        if(kind!=World.WALK){transfer(c,w,a,now,kind,to);return;}
        Bitmap b=human?walk[phase]:dogwalk[phase];float dy=World.POS[to][1]-a.y,dx=World.POS[to][0]-a.x;
        if(human&&dy< -3&&Math.abs(dy)>=Math.abs(dx)*.65f)b=poses.get("rear");
        shadow(c,x,y,human?24:14);sprite(c,b,x,y-(phase%2),human?65:26,face,1);return;}
      if(human){switch(a.job){
        case 0: case 3: case 9: case 18:
          sofaPose(c,a.job,now);break;
        case 1: case 2: sprite(c,poses.get(a.job==1?"wash"+(now/180%3):"wash"),78,453,65,1,1);if(a.job==1)water(c,40,439,now);else coffee(c,41,438,now);break;
        case 4: shadow(c,x,y,24);sprite(c,poses.get("pet"+(now/210%3)),x,y,52,1,1);if(w.petting()){int off=(int)(now/220%2);rect(c,177,414+off,4,2,0xffd49a77);if(now/700%3==0)heart(c,189,398);}else ball(c,190,439);break;
        case 6: sleepHuman(c,now);break;
        case 5: case 11: case 13: case 14: case 15:
          sprite(c,poses.get("seat"+(now/220%3)),x,y,57,-1,1);break;
        case 7: sprite(c,poses.get("rear"),x,y,65,1,1+(float)Math.sin(now/600.0)*.012f);break;
        case 10: stretch(c,x,y,now);break;
        default: shadow(c,x,y,24);sprite(c,walk[1],x,y,65,face,1+(float)Math.sin(now/550.0)*.012f);break;
      }}else{
        if(w.petting()){sprite(c,poses.get("dogbelly"),x,y,27,1,1+(float)Math.sin(now/280.0)*.035f);return;}
        if(w.sleeping(a)){if(a.node==World.SOFA_DOG||a.node==World.SOFA){x=85;y=314;}else if(a.node==World.FOOT){x=a.x;y=a.y;}shadow(c,x,y,15);sprite(c,poses.get("dogsleep"),x,y,15,1,1+(float)Math.sin(now/470.0)*.025f);if(now/550%4==0)zzz(c,x+8,y-24);return;}
        if(a.job==0){float off=(float)Math.sin(now/430.0)*6;shadow(c,x+off,y,15);sprite(c,poses.get("dogplay"),x+off,y,28,face,1);ball(c,x+face*(18+(float)Math.sin(now/430.0)*6),y-1);return;}
        if(a.job==8){float off=(float)Math.sin(now/180.0)*6;sprite(c,dogwalk[phase],x+off,y,27,face,1);return;}
        if(a.job==6&&!w.man.away&&w.man.route.isEmpty()&&a.node==w.man.node){x+=16;y+=5;}
        if(a.job==3){shadow(c,x,y,15);c.save();c.translate(x,y);c.rotate((float)Math.sin(now/260.0)*3);sprite(c,poses.get("dogplay"),0,0,23,-1,1);c.restore();return;}
        if(a.job==11){shadow(c,x,y,14);sprite(c,poses.get("dogplay"),x,y,24,face,1);ball(c,x+face*12,y-5);return;}
        if(a.job==10||a.node==World.SOFA_DOG){x=85;y=314;face=1;}
        if(a.job==2){x=a.x;y=a.y;}
        if(a.job==12||a.job==6)face=w.man.x>x?1:-1;
        shadow(c,x,y,13);sprite(c,poses.get("dogsit"),x,y,26,face,1+(float)Math.sin(now/620.0)*.015f);
      }
    }
    void transfer(Canvas c,World w,World.Actor a,long now,int kind,int to){
      float t=World.progress(a),ease=t*t*(3-2*t);float x=World.POS[a.node][0]+(World.POS[to][0]-World.POS[a.node][0])*ease;
      float y=World.POS[a.node][1]+(World.POS[to][1]-World.POS[a.node][1])*ease;
      boolean entering=World.furniture(to),human=a==w.man;
      float seated=entering?ease:1-ease;int job=entering?a.job:a.lastJob;
      if(kind==World.JUMP_SOFA){
        float jump=19*(float)Math.sin(Math.PI*t);
        shadow(c,x,y,12);Bitmap b=t<.2f||t>.8f?poses.get("dogsit"):poses.get("dogplay");
        sprite(c,b,x,y-jump,24,a.face,1);return;
      }
      if(kind==World.SIT_SOFA){
        // Walk animation ends at the approach; the entire transfer is a sit/stand motion.
        Bitmap b=poses.get("rear");c.save();c.translate(x,y);c.rotate(-5*seated);
        src.set(0,0,b.getWidth(),(int)(b.getHeight()*(1-.28f*seated)));
        dst.set(-19,-65+20*seated,19,0);p.setColor(Color.WHITE);c.drawBitmap(b,src,dst,p);c.restore();return;
      }
      if(kind==World.SIT_TABLE){
        shadow(c,x,y,20);sprite(c,seated>.45f?poses.get("seat"):walk[1],x,y,65-8*seated,-1,1);return;
      }
      if(kind==World.BED_TRANSFER){
        if(seated<.72f){shadow(c,x,y,20);sprite(c,walk[1],x,y,65-14*seated,a.face,1-.22f*seated);}
        else sleepHuman(c,now);
      }
    }
    void sofaPose(Canvas c,int job,long now){
      // Sofa faces the TV, so the back of the hoodie and head should be visible.
      Bitmap b=poses.get("rear");int lean=job==18?7:job==9?4:0;
      c.save();c.translate(111,312);c.rotate(lean+(float)Math.sin(now/540.0)*(job==0?1.8f:1.1f));src.set(0,0,b.getWidth(),(int)(b.getHeight()*.72f));dst.set(-19,-45,19,0);p.setColor(Color.WHITE);c.drawBitmap(b,src,dst,p);c.restore();
      if(job==0){float shift=(float)Math.sin(now/220.0)*1.5f;rect(c,93+shift,297+(now/160%2),7,3,0xff29262c);rect(c,95+shift,297,2,1,0xffbdced4);rect(c,90+shift,296,3,3,0xffd4a079);}
      if(job==3)book(c,94,297,now);
    }
    void stretch(Canvas c,float x,float y,long now){
      shadow(c,x,y,24);Bitmap b=walk[1];sprite(c,b,x,y,65,1,1);
      // Raised sleeve shapes and hands; shoulders stay on the reference sprite.
      int q=(int)(now/200%6);float lift=3+(q<3?q:6-q);
      poly(c,0xff1b1820,x-12,y-42,x-19,y-44,x-26,y-57-lift,x-21,y-61-lift,x-10,y-49);
      poly(c,0xff29252e,x+10,y-44,x+18,y-46,x+25,y-59-lift,x+20,y-62-lift,x+9,y-50);
      rect(c,x-26,y-64-lift,5,5,0xffd89a79);rect(c,x+20,y-65-lift,5,5,0xffd89a79);
    }
    void sleepHuman(Canvas c,long now){ // head rests on pillow, blanket moves with breathing, body stays under it
      c.save();c.translate(287,427);c.rotate(-26);src.set(22,0,66,40);dst.set(-12,-13,10,7);p.setColor(Color.WHITE);c.drawBitmap(person,src,dst,p);c.restore();int rise=(int)(now/520%3==1?1:0);poly(c,0xffada0b6,265,430-rise,284,440,245,461,222,448);poly(c,0xffc6b8cb,265,429-rise,282,439,245,458,224,447);if(now/600%4==0)zzz(c,288,410);
    }
    void sofaFront(Canvas c){c.save();Path mask=path(54,319,77,331,82,332,153,296,156,325,83,359,55,346);c.clipPath(mask);p.setColor(Color.WHITE);dst.set(0,0,309,553);c.drawBitmap(bg,null,dst,p);c.restore();}
    void tableFront(Canvas c){c.save();Path mask=path(49,483,56,477,83,470,112,470,132,477,143,490,137,503,116,514,86,515,61,507,48,495);c.clipPath(mask);p.setColor(Color.WHITE);dst.set(0,0,309,553);c.drawBitmap(bg,null,dst,p);c.restore();}
    void tableAction(Canvas c,int job,long t){int n=(int)(t/220%3);switch(job){case 13:poly(c,0xff302a38,106,488,124,479,135,484,118,493);poly(c,0xff849ac8,105,486,105,474,123,466,123,478);rect(c,109,477+n,8,1,0xffdbe3f7);rect(c,122,486,3,2,0xffd8a381);break;case 14:rect(c,112,488,7,3,0xff3b4145);rect(c,114,483,3,5,0xff698b68);rect(c,118+n,482,9,1,0xffc59d70);break;case 15:for(int i=0;i<3;i++){poly(c,0xff6b4040,104+i*8,489-i*2,110+i*8,486-i*2,116+i*8,490-i*2,109+i*8,494-i*2);rect(c,108+i*8,489-i*2,3,2,0xffe7d8a7);}break;case 5:rect(c,116,484+n,3,4,0xffefce9a);break;case 11:rect(c,110+n*3,487,7,3,0xffb6c9a4);break;}}
    void water(Canvas c,float x,float y,long t){int n=(int)(t/95%6);rect(c,x+4,y-10,2,7,0xffa9d6e8);for(int i=0;i<5;i++){int q=(n+i*3)%11;rect(c,x-7+i*4,y-q,2,2,0xffe8f3ef);}rect(c,x+10,y+2+(n%2),5,2,0xffd6a080);}
    void coffee(Canvas c,float x,float y,long t){rect(c,x+8,y,6,5,0xffd5c6ae);rect(c,x+14,y+1,2,3,0xffd5c6ae);for(int i=0;i<3;i++){int q=(int)(t/180+i*5)%12;rect(c,x+10+i,y-3-q,2,2,0x77eaded6);}}
    void controller(Canvas c,float x,float y,long t){rect(c,x-3,y+1,10,4,0xff26232b);rect(c,x-1,y+2,2,1,0xffbed0d5);rect(c,x+4,y+2,1,1,0xfff5a4a4);if(t/160%2==0)rect(c,x+6,y,3,2,0xffd4a079);}
    void book(Canvas c,float x,float y,long t){poly(c,0xff655448,x-6,y+1,x,y-3,x+7,y,x+1,y+4);poly(c,0xfff2d5ad,x-6,y,x,y-4,x+1,y+2,x-5,y+3);poly(c,0xffdbc8a1,x,y-4,x+7,y-1,x+2,y+4,x+1,y+2);if(t/850%7==0)rect(c,x,y-5,2,6,0xfffae4c0);}
    void ball(Canvas c,float x,float y){rect(c,x-2,y-3,5,5,0xff69417e);rect(c,x-3,y-2,7,3,0xff8d56a9);rect(c,x-1,y-2,2,2,0xffca91d2);}
    void heart(Canvas c,float x,float y){rect(c,x-3,y,3,4,0xffe58da1);rect(c,x+1,y,3,4,0xffe58da1);rect(c,x-2,y+3,5,2,0xffe58da1);rect(c,x,y+5,1,1,0xffe58da1);}
    void zzz(Canvas c,float x,float y){p.setColor(0xfff0ddcf);p.setTextSize(8);p.setTypeface(Typeface.MONOSPACE);c.drawText("zZ",x,y,p);}
    void shadow(Canvas c,float x,float y,float w){p.setColor(0x55302529);c.drawOval(x-w/2,y-2,x+w/2,y+3,p);}
    void sprite(Canvas c,Bitmap b,float x,float y,float height,int face,float breath){float width=height*b.getWidth()/b.getHeight();c.save();c.translate(Math.round(x),Math.round(y));c.scale(face,breath);dst.set(-width/2,-height,width/2,0);p.setColor(Color.WHITE);p.setAlpha(spriteAlpha);c.drawBitmap(b,null,dst,p);p.setAlpha(255);c.restore();}
    void rect(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(Math.round(x),Math.round(y),Math.round(x+w),Math.round(y+h),p);}
    Path path(float... xy){Path a=new Path();a.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)a.lineTo(xy[i],xy[i+1]);a.close();return a;}
    void poly(Canvas c,int color,float... xy){p.setColor(color);c.drawPath(path(xy),p);}
}

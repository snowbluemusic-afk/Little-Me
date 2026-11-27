package com.littleme.wallpaper;
import android.content.Context;
import android.graphics.*;
import java.io.IOException;
import java.util.Calendar;
final class Scene {
    private final Bitmap bg,person,pet;
    private final Paint paint=new Paint();
    private final RectF rect=new RectF();
    Scene(Context c){bg=load(c,"lm_bg.png");person=load(c,"lm_man.png");pet=load(c,"lm_dog.png");paint.setFilterBitmap(false);}
    private Bitmap load(Context c,String n){try(java.io.InputStream s=c.getAssets().open(n)){Bitmap b=BitmapFactory.decodeStream(s);if(b==null)throw new IOException(n);return b;}catch(IOException e){throw new IllegalStateException("Missing artwork: "+n,e);}}
    void draw(Canvas c,World w,int width,int height,long now){
        if(width<=0||height<=0)return; Calendar cal=Calendar.getInstance();cal.setTimeInMillis(now);int h=cal.get(Calendar.HOUR_OF_DAY);boolean dark=h<6||h>=20;
        c.drawColor(Color.rgb(122,99,123));float scale=Math.min(width/309f,height/553f);float left=(width-309*scale)/2f,top=height-553*scale;
        c.save();c.translate(left,top);c.scale(scale,scale);paint.setColor(Color.WHITE);rect.set(0,0,309,553);c.drawBitmap(bg,null,rect,paint);
        if(w.man.y<=w.dog.y){actor(c,w,w.man,now);actor(c,w,w.dog,now);}else{actor(c,w,w.dog,now);actor(c,w,w.man,now);}
        if(dark){paint.setColor(0x33253055);c.drawRect(0,0,309,553,paint);}c.restore();
        if(top>0&&dark){paint.setColor(0x33253055);c.drawRect(0,0,width,top,paint);}
        if(left>0&&dark){paint.setColor(0x33253055);c.drawRect(0,top,left,height,paint);c.drawRect(width-left,top,width,height,paint);}
    }
    private void actor(Canvas c,World w,World.Actor a,long now){boolean human=a==w.man;if(human&&a.away)return;Bitmap b=human?person:pet;boolean moving=!a.route.isEmpty(),sleep=w.sleeping(a);String action=w.name(a);float x=Math.round(a.x),y=Math.round(a.y);if(human&&!moving&&a.node==World.BED&&sleep){x=269;y=454;}if(!human&&!moving&&a.node==w.man.node&&!w.man.away){x+=13;y+=8;}float width=human?50:34,height=width*b.getHeight()/b.getWidth();
        c.save();c.translate(x,y);paint.setColor(0x66322429);c.drawOval(-width*.32f,-3,width*.32f,3,paint);
        if(sleep){c.translate(0,-4);c.rotate(human?-65:-12);width*=.86f;height*=.86f;}
        else if(!moving&&human&&(action.equals("Gaming")||action.equals("Reading")||action.equals("Relaxing")||action.equals("Playing with the dog"))){c.scale(1,.78f);}
        else if(moving){c.translate(0,(float)Math.round(Math.sin(now/95.0)*1.8));}
        else if(action.equals("Zoomies")){c.translate((float)Math.sin(now/130.0)*10,-(float)Math.abs(Math.sin(now/120.0))*7);}
        c.scale(a.face,1);paint.setColor(Color.WHITE);rect.set(-width/2,-height,width/2,0);c.drawBitmap(b,null,rect,paint);c.restore();
        if(sleep){label(c,"zZ",x+9,y-25-(float)Math.sin(now/450.0)*3);return;}
        if(moving)return;
        switch(action){case "Gaming": game(c,x,y);break;case "Washing dishes": bubbles(c,x,y,now);break;case "Making coffee":label(c,"~",x+9,y-45);break;case "Reading":book(c,x,y);break;case "Playing with the dog":label(c,"♥",x+14,y-48);break;case "Chasing a toy":paint.setColor(0xff935ab9);c.drawCircle(x+15+(float)Math.sin(now/300.0)*6,y-2,4,paint);break;case "Eating":label(c,"·",x+13,y-38);break;default:break;}
    }
    private void label(Canvas c,String s,float x,float y){paint.setColor(0xffffe3be);paint.setTypeface(Typeface.MONOSPACE);paint.setTextSize(11);c.drawText(s,x,y,paint);}
    private void game(Canvas c,float x,float y){paint.setColor(0xff252132);c.drawRoundRect(x+2,y-26,x+17,y-19,2,2,paint);paint.setColor(0xff95c2de);c.drawRect(x+5,y-24,x+7,y-22,paint);paint.setColor(0xffe7a0a4);c.drawRect(x+12,y-24,x+14,y-22,paint);}
    private void book(Canvas c,float x,float y){paint.setColor(0xff654e38);c.drawRect(x-3,y-27,x+14,y-18,paint);paint.setColor(0xfff2d4a5);c.drawRect(x-2,y-28,x+6,y-20,paint);c.drawRect(x+7,y-28,x+15,y-20,paint);}
    private void bubbles(Canvas c,float x,float y,long now){paint.setColor(0xffc9e5ed);for(int i=0;i<4;i++){float off=(now/90+i*5)%18;c.drawCircle(x+8+i*3,y-30-off,1+i%2,paint);}}
}

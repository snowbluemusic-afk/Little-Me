package com.littleme.wallpaper;
import java.util.*;
public final class World {
    public static final int LOUNGE=0, SOFA=1, DOOR=2, STAIRS=3, RUG=4, SINK=5, TABLE=6, BED=7, WINDOW=8, FOOT=9, HALL=10, KITCHEN=11, RUG_LEFT=12;
    // Anchors are feet on the floor, not furniture centers. Sitting/sleeping poses have separate render anchors.
    public static final float[][] POS={{168,354},{151,345},{257,320},{226,395},{174,439},{58,469},{147,481},{211,474},{124,278},{198,485},{185,322},{117,441},{147,427}};
    static final int[][] EDGES={{1,10,3},{0},{10},{0,4},{3,9,11,12},{11},{11,9},{9},{10},{4,6,7},{0,2,8},{4,5,6,12},{4,11}};
    public static final class Job { public final String name; public final int node,min,max,weight; Job(String n,int p,int a,int b,int w){name=n;node=p;min=a;max=b;weight=w;} }
    public static final Job[] HUMAN={new Job("Gaming",SOFA,70,240,9),new Job("Washing dishes",SINK,25,80,5),new Job("Making coffee",SINK,20,65,4),new Job("Reading",SOFA,60,190,5),new Job("Playing with the dog",RUG,35,110,8),new Job("Eating",TABLE,45,120,4),new Job("Sleeping",BED,300,1800,1),new Job("Looking outside",WINDOW,15,45,2),new Job("Going out",DOOR,180,1200,3),new Job("Relaxing",SOFA,40,150,5),new Job("Stretching",RUG,15,55,2),new Job("Tidying",TABLE,20,75,3),new Job("Thinking",FOOT,30,100,2),new Job("Working on laptop",TABLE,60,240,5),new Job("Painting miniatures",TABLE,50,180,4),new Job("Playing Magic cards",TABLE,40,140,4)};
    public static final Job[] DOG={new Job("Chasing a toy",RUG,15,65,5),new Job("Napping",SOFA,60,260,6),new Job("Waiting by the door",DOOR,30,100,3),new Job("Looking for snacks",TABLE,15,55,4),new Job("Watching the window",WINDOW,20,90,4),new Job("Resting",RUG,70,300,5),new Job("Following you",RUG,20,75,6),new Job("Sleeping by the bed",FOOT,90,360,3),new Job("Zoomies",RUG,8,20,2),new Job("Belly rubs",RUG,15,90,0)};
    public static final class Actor {
        public int node,job=-1,face=1; public float x,y; public boolean away; public long until,dwell; public final ArrayDeque<Integer> route=new ArrayDeque<>();
        Actor(int n){node=n;x=POS[n][0];y=POS[n][1];}
        public String encode(){StringBuilder r=new StringBuilder();for(int i:route){if(r.length()>0)r.append(':');r.append(i);}return node+","+job+","+face+","+x+","+y+","+away+","+until+","+r+","+dwell;}
        public void decode(String s,boolean human){if(s==null)return;try{String[] p=s.split(",",-1);int n=Integer.parseInt(p[0]),j=Integer.parseInt(p[1]);if(n<0||n>=POS.length||j< -1||j>=(human?HUMAN:DOG).length)return;float xx=Float.parseFloat(p[3]),yy=Float.parseFloat(p[4]);if(!Float.isFinite(xx)||!Float.isFinite(yy))return;ArrayDeque<Integer> r=new ArrayDeque<>();if(!p[7].isEmpty())for(String k:p[7].split(":")){int z=Integer.parseInt(k);if(z<0||z>=POS.length)return;r.add(z);}node=n;job=j;face=Integer.parseInt(p[2])<0?-1:1;x=xx;y=yy;away=Boolean.parseBoolean(p[5]);until=Long.parseLong(p[6]);dwell=p.length>8?Long.parseLong(p[8]):30000;route.clear();route.addAll(r);}catch(RuntimeException ignored){}}
    }
    public final Actor man=new Actor(RUG),dog=new Actor(RUG_LEFT); private final Random random; public long time; public boolean demo=false;
    public World(long seed,long now){random=new Random(seed);time=now;man.until=now;dog.until=now;}
    public String name(Actor a){if(a==man&&a.away)return "Out of the house";if(a.job<0)return "Wandering";return (a==man?HUMAN:DOG)[a.job].name;}
    public boolean sleeping(Actor a){return a.route.isEmpty()&&(a==man?a.job==6:a.job==1||a.job==5||a.job==7);}
    public boolean petting(){return man.job==4&&!man.away&&man.route.isEmpty()&&dog.job==9&&dog.route.isEmpty()&&dog.node==RUG;}
    static ArrayDeque<Integer> path(int start,int end){int[] prev=new int[POS.length];Arrays.fill(prev,-1);prev[start]=start;ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);while(!q.isEmpty()){int p=q.remove();if(p==end)break;for(int n:EDGES[p])if(prev[n]<0){prev[n]=p;q.add(n);}}ArrayDeque<Integer> out=new ArrayDeque<>();if(prev[end]>=0)for(int p=end;p!=start;p=prev[p])out.addFirst(p);return out;}
    private boolean night(long now){Calendar cal=Calendar.getInstance();cal.setTimeInMillis(now);int h=cal.get(Calendar.HOUR_OF_DAY);return h>=22||h<7;}
    private long duration(Job j,boolean sleep){long d=1000L*(sleep?1200+random.nextInt(3601):j.min+random.nextInt(j.max-j.min+1));return demo?Math.max(3500,Math.min(18000,d/12)):d;}
    private void set(Actor a,int j,int dest,long now){Job job=(a==man?HUMAN:DOG)[j];a.job=j;a.dwell=duration(job,a==man&&j==6&&night(now));a.route.clear();a.route.addAll(path(a.node,dest));if(a==dog&&j==8){a.route.add(RUG_LEFT);a.route.add(RUG);a.route.add(FOOT);a.route.add(RUG);}if(a.route.isEmpty())arrive(a,now);else a.until=now+a.dwell;}
    private void arrive(Actor a,long now){a.until=now+Math.max(1000,a.dwell);if(a==man&&a.job==8)a.away=true;}
    void choose(Actor a,long now){boolean human=a==man;Job[] jobs=human?HUMAN:DOG;boolean night=night(now);
        if(human&&a.away){a.away=false;set(a,9,SOFA,now);return;}
        if(!human&&man.job==4&&!man.away){set(a,9,RUG,now);return;}
        int[] weights=new int[jobs.length];int total=0;for(int i=0;i<jobs.length;i++){int w=jobs[i].weight;if(human&&i==6)w=night?55:2;if(human&&i==8&&night)w=0;if(!human&&i==6&&man.away)w=0;if(!human&&night&&(i==1||i==5||i==7))w*=3;if(i==a.job)w/=4;weights[i]=w;total+=w;}int roll=random.nextInt(total),j=0;for(;j<weights.length-1;j++){roll-=weights[j];if(roll<0)break;}int dest=jobs[j].node;if(!human&&j==6)dest=man.route.isEmpty()?man.node:man.route.peekLast();set(a,j,dest,now);
        if(human&&j==4&&!dog.away&&dog.route.isEmpty())set(dog,9,RUG,now);
    }
    public void next(){if(man.route.isEmpty())choose(man,time);if(dog.route.isEmpty()&&man.job!=4)choose(dog,time);}
    public void step(Actor a,long now,float dt){if(!a.route.isEmpty()){int n=a.route.peek();float dx=POS[n][0]-a.x,dy=POS[n][1]-a.y,d=(float)Math.hypot(dx,dy),s=dt*(a==dog?(a.job==8?54:34):22);if(Math.abs(dx)>0.1)a.face=dx>0?1:-1;if(d<=s){a.x=POS[n][0];a.y=POS[n][1];a.node=n;a.route.remove();if(a.route.isEmpty()){arrive(a,now);if(a==dog&&man.job==4&&!man.away&&a.job!=9)set(a,9,RUG,now);}}else{a.x+=dx*s/d;a.y+=dy*s/d;}}else if(now>=a.until||(a==dog&&a.job==9&&(man.job!=4||man.away)))choose(a,now);}
    public void advance(long now){if(now<time){time=now;man.until=now;dog.until=now;return;}if(now-time>86400000L){time=now-86400000L;man.until=time;dog.until=time;}while(time<now){boolean moving=!man.route.isEmpty()||!dog.route.isEmpty();long next=moving?Math.min(now,time+100):Math.min(now,Math.max(time+1,Math.min(man.until,dog.until)));float dt=(next-time)/1000f;time=next;step(man,time,dt);step(dog,time,dt);}}
}

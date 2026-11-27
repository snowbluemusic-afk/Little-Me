package com.littleme.wallpaper;
import java.util.*;
public final class World {
    public static final int LOUNGE=0, SOFA=1, DOOR=2, STAIRS=3, RUG=4, SINK=5, TABLE=6, BED=7, WINDOW=8, FOOT=9;
    public static final float[][] POS={{176,346},{130,345},{257,306},{228,388},{170,450},{62,450},{110,477},{251,453},{107,278},{201,476}};
    static final int[][] EDGES={{1,2,3,8},{0},{0},{0,4},{3,5,6,9},{4,6},{4,5},{9},{0},{4,7}};
    public static final class Job {
        public final String name; public final int node, min, max, weight;
        Job(String n,int p,int a,int b,int w){name=n;node=p;min=a;max=b;weight=w;}
    }
    public static final Job[] HUMAN={new Job("Gaming",SOFA,70,240,8),new Job("Washing dishes",SINK,25,80,4),new Job("Making coffee",SINK,20,65,4),new Job("Reading",SOFA,60,190,6),new Job("Playing with the dog",RUG,30,110,7),new Job("Eating",TABLE,45,120,5),new Job("Sleeping",BED,300,1800,1),new Job("Looking outside",WINDOW,15,45,2),new Job("Going out",DOOR,180,1200,3),new Job("Relaxing",SOFA,40,150,5),new Job("Stretching",RUG,15,55,2),new Job("Tidying",TABLE,20,75,3),new Job("Thinking",FOOT,30,100,3)};
    public static final Job[] DOG={new Job("Chasing a toy",RUG,15,65,5),new Job("Napping",SOFA,60,260,6),new Job("Waiting by the door",DOOR,30,100,3),new Job("Looking for snacks",TABLE,15,55,4),new Job("Watching the window",WINDOW,20,90,4),new Job("Resting",RUG,70,300,5),new Job("Following you",RUG,20,75,6),new Job("Sleeping by the bed",FOOT,90,360,3),new Job("Zoomies",RUG,8,20,2)};
    public static final class Actor {
        public int node, job=-1, face=1; public float x,y; public boolean away; public long until; public final ArrayDeque<Integer> route=new ArrayDeque<>();
        Actor(int n){node=n;x=POS[n][0];y=POS[n][1];}
        public String encode(){StringBuilder r=new StringBuilder();for(int i:route){if(r.length()>0)r.append(':');r.append(i);}return node+","+job+","+face+","+x+","+y+","+away+","+until+","+r;}
        public void decode(String s,boolean human){if(s==null)return;try{String[] p=s.split(",",-1);int n=Integer.parseInt(p[0]),j=Integer.parseInt(p[1]);if(n<0||n>=POS.length||j< -1||j>=(human?HUMAN:DOG).length)return;float xx=Float.parseFloat(p[3]), yy=Float.parseFloat(p[4]);if(!Float.isFinite(xx)||!Float.isFinite(yy))return;ArrayDeque<Integer> r=new ArrayDeque<>();if(!p[7].isEmpty())for(String k:p[7].split(":")){int z=Integer.parseInt(k);if(z<0||z>=POS.length)return;r.add(z);}node=n;job=j;face=Integer.parseInt(p[2])<0?-1:1;x=xx;y=yy;away=Boolean.parseBoolean(p[5]);until=Long.parseLong(p[6]);route.clear();route.addAll(r);}catch(RuntimeException ignored){}}
    }
    public final Actor man=new Actor(RUG), dog=new Actor(TABLE); private final Random random; public long time;
    public World(long seed,long now){random=new Random(seed);time=now;}
    public String name(Actor a){if(a==man&&a.away)return "Out of the house";if(a.job<0)return "Wandering";return (a==man?HUMAN:DOG)[a.job].name;}
    public boolean sleeping(Actor a){String n=name(a);return a.route.isEmpty()&&(n.equals("Sleeping")||n.equals("Napping")||n.equals("Resting")||n.equals("Sleeping by the bed"));}
    static ArrayDeque<Integer> path(int start,int end){int[] prev=new int[POS.length];Arrays.fill(prev,-1);prev[start]=start;ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);while(!q.isEmpty()){int p=q.remove();if(p==end)break;for(int n:EDGES[p])if(prev[n]<0){prev[n]=p;q.add(n);}}ArrayDeque<Integer> out=new ArrayDeque<>();if(prev[end]>=0)for(int p=end;p!=start;p=prev[p])out.addFirst(p);return out;}
    void choose(Actor a,long now){boolean human=a==man;Job[] jobs=human?HUMAN:DOG;Calendar cal=Calendar.getInstance();cal.setTimeInMillis(now);int hour=cal.get(Calendar.HOUR_OF_DAY);boolean night=hour>=22||hour<7;
        if(human&&a.away){a.away=false;a.job=9;a.route.addAll(path(a.node,LOUNGE));a.until=now+30000;return;}
        int[] weights=new int[jobs.length];int total=0;for(int i=0;i<jobs.length;i++){int w=jobs[i].weight;if(human&&i==6)w=night?55:2;if(human&&i==8&&night)w=0;if(!human&&i==6&&man.away)w=0;if(!human&&night&&(i==1||i==5||i==7))w*=3;if(i==a.job)w/=4;weights[i]=w;total+=w;}int roll=random.nextInt(total),j=0;for(;j<weights.length-1;j++){roll-=weights[j];if(roll<0)break;}a.job=j;Job job=jobs[j];int dest=job.node;if(!human&&j==6)dest=man.node;if(!human&&j==0&&man.job==4&&!man.away)dest=RUG;a.route.addAll(path(a.node,dest));a.until=now+duration(job,night&&human&&j==6);if(a.route.isEmpty()&&human&&j==8)a.away=true;
    }
    long duration(Job j,boolean nightSleep){return 1000L*(nightSleep?1200+random.nextInt(3601):j.min+random.nextInt(j.max-j.min+1));}
    void step(Actor a,long now,float dt){if(!a.route.isEmpty()){int n=a.route.peek();float dx=POS[n][0]-a.x,dy=POS[n][1]-a.y,d=(float)Math.hypot(dx,dy),s=dt*(a==dog?(a.job==8?68:39):27);if(Math.abs(dx)>0.1)a.face=dx>0?1:-1;if(d<=s){a.x=POS[n][0];a.y=POS[n][1];a.node=n;a.route.remove();if(a.route.isEmpty()){Job j=(a==man?HUMAN:DOG)[a.job];a.until=Math.max(a.until,now+j.min*1000L);if(a==man&&a.job==8)a.away=true;}}else{a.x+=dx*s/d;a.y+=dy*s/d;}}else if(now>=a.until)choose(a,now);}
    public void advance(long now){if(now<time){time=now;man.until=now;dog.until=now;return;}if(now-time>86400000L){time=now-86400000L;man.until=time;dog.until=time;}while(time<now){boolean moving=!man.route.isEmpty()||!dog.route.isEmpty();long next=moving?Math.min(now,time+100):Math.min(now,Math.max(time+1,Math.min(man.until,dog.until)));float dt=(next-time)/1000f;time=next;step(man,time,dt);step(dog,time,dt);}}
}

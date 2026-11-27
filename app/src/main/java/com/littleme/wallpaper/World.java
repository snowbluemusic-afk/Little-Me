package com.littleme.wallpaper;
import java.util.*;
public final class World {
    public static final int LOUNGE=0, SOFA=1, DOOR=2, STAIRS=3, RUG=4, SINK=5, TABLE=6, BED=7, WINDOW=8, FOOT=9, HALL=10, KITCHEN=11, RUG_LEFT=12, STAIR_TOP=13, STAIR_BOTTOM=14, SOFA_ENTRY=15, SOFA_DOG=16, SNACKS=17, WINDOW_APPROACH=18, BACK_AISLE=19, SOFA_DOG_ENTRY=20, TABLE_ENTRY=21, BED_ENTRY=22, PET_SPOT=23;
    // Anchors are feet on the floor, not furniture centers. Sitting/sleeping poses have separate render anchors.
    public static final float[][] POS={{174,347},{111,312},{257,315},{226,372},{174,439},{83,453},{144,492},{249,451},{125,268},{198,485},{191,322},{119,450},{147,427},{207,345},{236,402},{138,280},{85,314},{151,474},{151,264},{191,270},{91,282},{166,478},{204,461},{190,452}};
    static final int[][] EDGES={{10,13},{15},{10},{13,14},{14,9,11,12,23},{11},{21},{22},{18},{4,21,22},{0,2,19},{4,5,12,17},{4,11},{0,3},{3,4},{18,1,20},{20},{11,21},{15,8,19},{10,18},{15,16},{9,17,6},{9,7},{4}};
    public static final class Job { public final String name; public final int node,min,max,weight; Job(String n,int p,int a,int b,int w){name=n;node=p;min=a;max=b;weight=w;} }
    public static final Job[] HUMAN={new Job("Gaming",SOFA,70,240,9),new Job("Washing dishes",SINK,25,80,5),new Job("Making coffee",SINK,20,65,4),new Job("Reading",SOFA,60,190,5),new Job("Playing with the dog",RUG,35,110,8),new Job("Eating",TABLE,45,120,4),new Job("Sleeping",BED,300,1800,1),new Job("Looking outside",WINDOW,15,45,2),new Job("Going out",DOOR,180,1200,3),new Job("Relaxing",SOFA,40,150,5),new Job("Stretching",RUG,15,55,2),new Job("Tidying",TABLE,20,75,3),new Job("Thinking",FOOT,30,100,2),new Job("Working on laptop",TABLE,60,240,5),new Job("Painting miniatures",TABLE,50,180,4),new Job("Playing Magic cards",TABLE,40,140,4),new Job("Returning home",LOUNGE,10,25,0),new Job("Standing idle",RUG_LEFT,12,40,3),new Job("Lounging on sofa",SOFA,70,240,4)};
    public static final Job[] DOG={new Job("Chasing a toy",RUG,15,65,5),new Job("Napping",SOFA_DOG,60,260,6),new Job("Waiting by the door",DOOR,30,100,3),new Job("Looking for snacks",SNACKS,15,55,4),new Job("Watching the window",WINDOW,20,90,4),new Job("Resting",RUG,70,300,5),new Job("Following you",RUG,20,75,6),new Job("Sleeping by the bed",FOOT,90,360,3),new Job("Zoomies",RUG,8,20,2),new Job("Belly rubs",PET_SPOT,15,90,0),new Job("Keeping you company",SOFA_DOG,45,180,4),new Job("Bringing a toy",RUG_LEFT,20,70,3),new Job("Watching you",RUG_LEFT,30,100,4)};
    public static final class Actor {
        public int node,job=-1,face=1; public int lastJob=-1; public float x,y; public boolean away; public long until,dwell; public final ArrayDeque<Integer> route=new ArrayDeque<>();
        Actor(int n){node=n;x=POS[n][0];y=POS[n][1];}
        public String encode(){StringBuilder r=new StringBuilder();for(int i:route){if(r.length()>0)r.append(':');r.append(i);}return node+","+job+","+face+","+x+","+y+","+away+","+until+","+r+","+dwell+","+lastJob;}
        public void decode(String s,boolean human){if(s==null)return;try{String[] p=s.split(",",-1);int n=Integer.parseInt(p[0]),j=Integer.parseInt(p[1]);if(n<0||n>=POS.length||j< -1||j>=(human?HUMAN:DOG).length)return;float xx=Float.parseFloat(p[3]),yy=Float.parseFloat(p[4]);if(!Float.isFinite(xx)||!Float.isFinite(yy))return;ArrayDeque<Integer> r=new ArrayDeque<>();if(!p[7].isEmpty())for(String k:p[7].split(":")){int z=Integer.parseInt(k);if(z<0||z>=POS.length)return;r.add(z);}node=n;job=j;face=Integer.parseInt(p[2])<0?-1:1;x=xx;y=yy;away=Boolean.parseBoolean(p[5]);until=Long.parseLong(p[6]);dwell=p.length>8?Long.parseLong(p[8]):30000;lastJob=p.length>9?Integer.parseInt(p[9]):-1;route.clear();route.addAll(r);}catch(RuntimeException ignored){}}
    }
    public final Actor man=new Actor(RUG),dog=new Actor(RUG_LEFT); private final Random random; public long time; public boolean demo=false; private boolean lively=false;
    public World(long seed,long now){random=new Random(seed);time=now;man.until=now;dog.until=now;}
    public String name(Actor a){if(a==man&&a.away)return "Out of the house";if(a.job<0)return "Wandering";return (a==man?HUMAN:DOG)[a.job].name;}
    public boolean sleeping(Actor a){return a.route.isEmpty()&&(a==man?a.job==6:a.job==1||a.job==5||a.job==7);}
    public boolean petting(){return man.job==4&&!man.away&&man.route.isEmpty()&&dog.job==9&&dog.route.isEmpty()&&dog.node==PET_SPOT;}
    static ArrayDeque<Integer> path(int start,int end){int[] prev=new int[POS.length];Arrays.fill(prev,-1);prev[start]=start;ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);while(!q.isEmpty()){int p=q.remove();if(p==end)break;for(int n:EDGES[p])if(prev[n]<0){prev[n]=p;q.add(n);}}ArrayDeque<Integer> out=new ArrayDeque<>();if(prev[end]>=0)for(int p=end;p!=start;p=prev[p])out.addFirst(p);return out;}
    private boolean night(long now){Calendar cal=Calendar.getInstance();cal.setTimeInMillis(now);int h=cal.get(Calendar.HOUR_OF_DAY);return h>=22||h<7;}
    private long duration(Actor a,int index,Job j,boolean sleep){
        long d=1000L*(sleep?1200+random.nextInt(3601):j.min+random.nextInt(j.max-j.min+1));
        if(demo)return Math.max(3500,Math.min(18000,d/12));
        if(!lively)return d;
        // A living wallpaper must be interesting during short lock-screen visits.
        if(a==man&&index==6)return sleep?60000+random.nextInt(120001):30000+random.nextInt(30001);
        if(a==man&&index==8)return 45000+random.nextInt(75001);
        if(a==dog&&(index==1||index==5||index==7))return 18000+random.nextInt(27001);
        if(a==dog&&index==8)return 4000+random.nextInt(4001);
        return a==man?8000+random.nextInt(16001):5000+random.nextInt(13001);
    }
    private long livelyLimit(Actor a){if(a==man&&a.job==6)return 180000;if(a==man&&a.job==8)return 120000;if(a==dog&&sleeping(a))return 45000;return a==man?24000:18000;}
    public void setLively(boolean enabled){
        if(lively==enabled)return;lively=enabled;
        if(enabled){for(Actor a:new Actor[]{man,dog}){long cap=livelyLimit(a);a.dwell=Math.min(a.dwell,cap);if(a.route.isEmpty())a.until=Math.min(a.until,time+cap);}}
    }
    private void set(Actor a,int j,int dest,long now){Job job=(a==man?HUMAN:DOG)[j];a.lastJob=a.job;a.job=j;a.dwell=duration(a,j,job,a==man&&j==6&&night(now));a.route.clear();a.route.addAll(path(a.node,dest));if(a==dog&&j==8){a.route.add(RUG_LEFT);a.route.add(RUG);a.route.add(PET_SPOT);a.route.add(RUG);}if(a.route.isEmpty())arrive(a,now);else a.until=now+a.dwell;}
    private void arrive(Actor a,long now){a.until=now+Math.max(1000,a.dwell);if(a==man&&a.job==8)a.away=true;}
    void choose(Actor a,long now){boolean human=a==man;Job[] jobs=human?HUMAN:DOG;boolean night=night(now);
        if(human&&a.away){a.away=false;set(a,16,LOUNGE,now);return;}
        if(!human&&man.job==4&&!man.away){set(a,9,PET_SPOT,now);return;}
        int[] weights=new int[jobs.length];int total=0;for(int i=0;i<jobs.length;i++){int w=jobs[i].weight;if(human&&i==6)w=night?(lively?24:55):2;if(human&&i==8&&night)w=0;if(!human){if(man.away){if(i==6||i==10||i==12)w=0;if(i==2||i==1)w*=3;}else if(i==10){w=(man.job==0||man.job==3||man.job==9||man.job==18)?20:0;}else if(i==12){w=7;}else if(i==7&&man.job==6)w*=5;else if(i==6&&(man.job==1||man.job==2))w*=3;}if(!human&&night&&(i==1||i==5||i==7))w*=3;if(i==a.job)w=lively?0:w/4;weights[i]=w;total+=w;}int roll=random.nextInt(total),j=0;for(;j<weights.length-1;j++){roll-=weights[j];if(roll<0)break;}int dest=jobs[j].node;if(!human&&(j==6||j==12))dest=companionNode(man.route.isEmpty()?man.node:man.route.peekLast());set(a,j,dest,now);
        if(human&&j==4&&!dog.away&&dog.route.isEmpty())set(dog,9,PET_SPOT,now);if(human&&j==8&&dog.route.isEmpty()&&random.nextBoolean())set(dog,2,DOOR,now);if(human&&j==6&&dog.route.isEmpty()&&random.nextBoolean())set(dog,7,FOOT,now);
    }
    static int companionNode(int n){switch(n){case SOFA: return SOFA_DOG;case SINK:return KITCHEN;case TABLE:return SNACKS;case BED:return FOOT;case WINDOW:return WINDOW_APPROACH;case DOOR:return HALL;case FOOT:case BED_ENTRY:return RUG;case RUG_LEFT:return RUG;case PET_SPOT:return RUG_LEFT;default:return n==RUG?RUG_LEFT:n;}}
    public void previewActivity(boolean human,int j){Actor a=human?man:dog;if(j<0||j>=(human?HUMAN:DOG).length)return; if(!a.route.isEmpty())return;if(human)a.away=false;set(a,j,(human?HUMAN:DOG)[j].node,time);if(human&&j==4&&dog.route.isEmpty())set(dog,9,PET_SPOT,time);}
    public void next(){if(man.route.isEmpty())choose(man,time);if(dog.route.isEmpty()&&man.job!=4)choose(dog,time);}
    public static final int WALK=0, SIT_SOFA=1, JUMP_SOFA=2, SIT_TABLE=3, BED_TRANSFER=4;
    public static int transition(int from,int to){
        if(from==SOFA||to==SOFA)return SIT_SOFA;
        if(from==SOFA_DOG||to==SOFA_DOG)return JUMP_SOFA;
        if(from==TABLE||to==TABLE)return SIT_TABLE;
        if(from==BED||to==BED)return BED_TRANSFER;
        return WALK;
    }
    public static boolean furniture(int n){return n==SOFA||n==SOFA_DOG||n==TABLE||n==BED;}
    public static float progress(Actor a){if(a.route.isEmpty())return 1;int to=a.route.peek();float[] start=POS[a.node],end=POS[to];float total=(float)Math.hypot(end[0]-start[0],end[1]-start[1]);return total<.01f?1:Math.max(0,Math.min(1,1-(float)Math.hypot(end[0]-a.x,end[1]-a.y)/total));}
    public String motion(Actor a){if(a.route.isEmpty())return name(a);int to=a.route.peek(),kind=transition(a.node,to);switch(kind){case JUMP_SOFA:return to==SOFA_DOG?"Jumping onto sofa":"Jumping down";case SIT_SOFA:return to==SOFA?"Sitting down":"Standing up";case SIT_TABLE:return to==TABLE?"Taking a seat":"Leaving the table";case BED_TRANSFER:return to==BED?"Getting into bed":"Getting up";default:return "Walking";}}
    public void step(Actor a,long now,float dt){if(!a.route.isEmpty()){int n=a.route.peek();float dx=POS[n][0]-a.x,dy=POS[n][1]-a.y,d=(float)Math.hypot(dx,dy),s=dt*(a==dog?(a.job==8?65:36):24);int transfer=transition(a.node,n);if(transfer!=WALK){float duration=transfer==JUMP_SOFA?.65f:transfer==BED_TRANSFER?1.3f:.9f;float total=(float)Math.hypot(POS[n][0]-POS[a.node][0],POS[n][1]-POS[a.node][1]);s=dt*total/duration;}if(Math.abs(dx)>0.1)a.face=dx>0?1:-1;if(d<=s){a.x=POS[n][0];a.y=POS[n][1];a.node=n;a.route.remove();if(a.route.isEmpty()){arrive(a,now);if(a==dog&&man.job==4&&!man.away&&a.job!=9)set(a,9,PET_SPOT,now);}}else{a.x+=dx*s/d;a.y+=dy*s/d;}}else if(a==dog&&(a.job==6||a.job==12)&&!man.away&&companionNode(man.route.isEmpty()?man.node:man.route.peekLast())!=a.node&&now<a.until){int target=companionNode(man.route.isEmpty()?man.node:man.route.peekLast());a.route.addAll(path(a.node,target));}else if(now>=a.until||(a==dog&&a.job==9&&(man.job!=4||man.away)))choose(a,now);}
    public void advance(long now){if(now<time){time=now;man.until=now;dog.until=now;return;}if(now-time>86400000L){time=now-86400000L;man.until=time;dog.until=time;}while(time<now){boolean moving=!man.route.isEmpty()||!dog.route.isEmpty()||(dog.job==6||dog.job==12)&&!man.away&&companionNode(man.node)!=dog.node;long next=moving?Math.min(now,time+100):Math.min(now,Math.max(time+1,Math.min(man.until,dog.until)));float dt=(next-time)/1000f;time=next;step(man,time,dt);step(dog,time,dt);}}
}

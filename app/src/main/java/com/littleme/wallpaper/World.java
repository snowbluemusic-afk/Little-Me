package com.littleme.wallpaper;
import java.util.*;
public final class World {
    public static final int LOUNGE=0, SOFA=1, DOOR=2, STAIRS=3, RUG=4, SINK=5, TABLE=6, BED=7, WINDOW=8, FOOT=9, HALL=10, KITCHEN=11, RUG_LEFT=12, STAIR_TOP=13, STAIR_BOTTOM=14, SOFA_ENTRY=15, SOFA_DOG=16, SNACKS=17, WINDOW_APPROACH=18, BACK_AISLE=19, SOFA_DOG_ENTRY=20, TABLE_ENTRY=21, BED_ENTRY=22, PET_SPOT=23, M_SOFA=24, M_TABLE=25, M_BED=26, M_RUG=27, M_SINK=28, M_WINDOW=29;
    // Anchors are feet on the floor, not furniture centers. Sitting/sleeping poses have separate render anchors.
    public static final float[][] POS={{174,347},{111,320},{257,315},{226,372},{174,439},{69,467},{144,492},{249,451},{148,283},{191,479},{191,322},{119,459},{147,427},{207,345},{236,402},{138,280},{85,306},{153,478},{165,278},{184,282},{91,282},{166,478},{201,461},{190,452},{126,313},{111,510},{270,446},{145,446},{89,470},{166,297}};
    static final int[][] EDGES={{10,13},{15},{10},{13,14},{14,9,11,12,23,27},{11},{21},{22},{18},{4,21,22},{0,2,19},{4,5,12,17,27,28},{4,11},{0,3},{3,4},{18,1,20,24},{20},{11,21},{15,8,19,29},{10,18},{15,16},{9,17,6,25},{9,7,26},{4},{15},{21},{22},{4,11},{11},{18}};
    public static final class Job { public final String name; public final int node,min,max,weight; Job(String n,int p,int a,int b,int w){name=n;node=p;min=a;max=b;weight=w;} }
    public static final Job[] HUMAN={new Job("Gaming",SOFA,70,240,9),new Job("Washing dishes",SINK,25,80,5),new Job("Making coffee",SINK,20,65,4),new Job("Reading",SOFA,60,190,5),new Job("Playing with the dog",RUG,35,110,8),new Job("Eating",TABLE,45,120,4),new Job("Sleeping",BED,300,1800,1),new Job("Looking outside",WINDOW,15,45,2),new Job("Going out",DOOR,180,1200,3),new Job("Relaxing",SOFA,40,150,5),new Job("Stretching",RUG,15,55,2),new Job("Tidying",TABLE,20,75,3),new Job("Thinking",FOOT,30,100,2),new Job("Working on laptop",TABLE,60,240,5),new Job("Painting miniatures",TABLE,50,180,4),new Job("Playing Magic cards",TABLE,40,140,4),new Job("Returning home",LOUNGE,10,25,0),new Job("Standing idle",RUG_LEFT,12,40,3),new Job("Lounging on sofa",SOFA,70,240,4)};
    public static final Job[] DOG={new Job("Chasing a toy",RUG,15,65,5),new Job("Napping",SOFA_DOG,60,260,6),new Job("Waiting by the door",DOOR,30,100,3),new Job("Looking for snacks",SNACKS,15,55,4),new Job("Watching the window",WINDOW,20,90,4),new Job("Resting",RUG,70,300,5),new Job("Following you",RUG,20,75,6),new Job("Sleeping by the bed",FOOT,90,360,3),new Job("Zoomies",RUG,8,20,2),new Job("Belly rubs",PET_SPOT,15,90,0),new Job("Keeping you company",SOFA_DOG,45,180,4),new Job("Bringing a toy",RUG_LEFT,20,70,3),new Job("Watching you",RUG_LEFT,30,100,4)};
    public static final class Actor {
        public int node,job=-1,face=1; public int lastJob=-1; public float x,y; public boolean away; public long until,dwell; public final ArrayDeque<Integer> route=new ArrayDeque<>();
        Actor(int n){node=n;x=POS[n][0];y=POS[n][1];}
        public String encode(){StringBuilder r=new StringBuilder();for(int i:route){if(r.length()>0)r.append(':');r.append(i);}return node+","+job+","+face+","+x+","+y+","+away+","+until+","+r+","+dwell+","+lastJob;}
        public void decode(String s,boolean human){if(s==null)return;try{String[] p=s.split(",",-1);int n=Integer.parseInt(p[0]),j=Integer.parseInt(p[1]);if(n<0||n>=POS.length||j< -1||j>=(human?HUMAN:DOG).length)return;float xx=Float.parseFloat(p[3]),yy=Float.parseFloat(p[4]);if(!Float.isFinite(xx)||!Float.isFinite(yy))return;ArrayDeque<Integer> r=new ArrayDeque<>();if(!p[7].isEmpty())for(String k:p[7].split(":")){int z=Integer.parseInt(k);if(z<0||z>=POS.length)return;r.add(z);}node=n;job=j;face=Integer.parseInt(p[2])<0?-1:1;x=xx;y=yy;away=Boolean.parseBoolean(p[5]);until=Long.parseLong(p[6]);dwell=p.length>8?Long.parseLong(p[8]):30000;lastJob=p.length>9?Integer.parseInt(p[9]):-1;route.clear();route.addAll(r);}catch(RuntimeException ignored){}}
    }
    public final Actor man=new Actor(RUG),dog=new Actor(RUG_LEFT),maddie=new Actor(M_RUG); private final Random random; public long time; public boolean demo=false; private boolean lively=false;
    public static final int UNKNOWN=-1, AWAY=0, HOME=1;
    public boolean household=false; public int selfPresence=UNKNOWN,maddiePresence=UNKNOWN; private int lastDogWake=-1; private final int[] wakeUse=new int[9]; private final int[] maddieUse=new int[9]; private final int[] humanUse=new int[HUMAN.length],dogUse=new int[DOG.length]; private final int[] maddieJobs={9,3,2,5,6,7,4,11,13};
    public World(long seed,long now){random=new Random(seed);time=now;man.until=now;dog.until=now;maddie.until=now;maddie.away=true;}
    public String name(Actor a){if(a.away)return "Out of the house";if(a.job<0)return "Wandering";return (a==dog?DOG:HUMAN)[a.job].name;}
    public boolean sleeping(Actor a){return a.route.isEmpty()&&(a!=dog?a.job==6:a.job==1||a.job==5||a.job==7);}
    public boolean petting(){return man.job==4&&!man.away&&man.route.isEmpty()&&dog.job==9&&dog.route.isEmpty()&&dog.node==PET_SPOT;}
    // Distinct visual groups, rather than different labels for identical poses.
    public int visualGroup(){if(man.away){if(household&& !maddie.away)return 20+sceneGroup(maddie.job);if(household)return 40+Math.max(0,dog.job);return 8;}if(!man.route.isEmpty())return 9;return sceneGroup(man.job);}
    static int sceneGroup(int j){switch(j){case 0:return 0;case 3:case 9:case 18:return 1;case 1:case 2:return 2;case 4:return 3;case 5:case 11:case 13:case 14:case 15:return 4;case 6:return 5;case 7:return 6;case 10:case 12:case 17:case 16:return 7;case 8:return 8;default:return 9;}}
    private void placeForWake(Actor a,int j,int node,long now){
        a.lastJob=a.job;a.job=j;a.node=node;a.x=POS[node][0];a.y=POS[node][1];a.route.clear();
        a.away=a!=dog&&j==8;a.face=a==dog&&man.x<a.x?-1:1;
        Job job=(a==dog?DOG:HUMAN)[j];a.dwell=duration(a,j,job,a!=dog&&j==6&&night(now));a.until=now+Math.max(8000,a.dwell);
    }
    // Only called between display sessions. Never jump characters during visible playback.
    public void wakeScene(long now,int previousGroup){
        int current=visualGroup();boolean dark=night(now);
        int[] candidates={0,1,2,3,4,5,6,7,8,9,10,12,13,14,15,18};
        int h=8;
        if(selfPresence!=AWAY){
            ArrayList<Integer> groups=new ArrayList<>();int min=Integer.MAX_VALUE;
            for(int g=0;g<9;g++){if(g==8&&(dark||selfPresence==HOME)||g==previousGroup||g==current)continue;if(wakeUse[g]<min){groups.clear();min=wakeUse[g];}if(wakeUse[g]==min)groups.add(g);}
            int g=groups.get(random.nextInt(groups.size()));wakeUse[g]++;
            ArrayList<Integer> jobs=new ArrayList<>();int least=Integer.MAX_VALUE;
            for(int j:candidates){if(sceneGroup(j)!=g)continue;if(humanUse[j]<least){jobs.clear();least=humanUse[j];}if(humanUse[j]==least)jobs.add(j);}
            h=jobs.get(random.nextInt(jobs.size()));humanUse[h]++;
        }
        time=now;placeForWake(man,h,HUMAN[h].node,now);
        if(household)wakeMaddie(now,previousGroup);
        int[] options;
        switch(h){
            case 4: options=new int[]{9};break;
            case 8: options=new int[]{2,1,5,3};break;
            case 6: options=new int[]{7,1,5};break;
            case 0:case 3:case 9:case 18: options=new int[]{10,1,0,5,11};break;
            case 1:case 2: options=new int[]{3,6,12,5,0};break;
            default: options=new int[]{0,1,2,3,5,6,7,11,12};break;
        }
        if(household&&!maddie.away&&maddie.job==4&&man.job!=4)options=new int[]{9};
        if(household&&man.away&&maddie.away){int prev=previousGroup>=40?previousGroup-40:-1;int currentDog=dog.job;options=Arrays.stream(new int[]{0,1,2,3,4,5,7,8,11}).filter(j->j!=prev&&j!=currentDog).toArray();}
        int d=balancedDog(options);
        int dest=(d==6||d==12)?companionNode(man.node):DOG[d].node;
        if(household&&man.away&&!maddie.away){if(d==6||d==12)dest=companionNode(maddie.node);if(d==9)dest=PET_SPOT;}
        if(dest==man.node&&!man.away){if(d==0||d==11){dest=RUG_LEFT;}else if(d==5){dest=RUG_LEFT;}else{d=12;dest=companionNode(man.node);}if(dest==man.node)dest=RUG_LEFT;}
        if(household&&!maddie.away&&dest==maddie.node){d=5;dest=RUG_LEFT;}
        placeForWake(dog,d,dest,now);
        if(d==9){long end=Math.max(man.until,dog.until);man.until=end;dog.until=end;if(household&&!maddie.away&&maddie.job==4)maddie.until=end;}
        lastDogWake=d;
    }
    int balancedDog(int[] options){ArrayList<Integer> pool=new ArrayList<>();int least=Integer.MAX_VALUE;for(int j:options){if(j==lastDogWake&&options.length>1)continue;if(dogUse[j]<least){least=dogUse[j];pool.clear();}if(dogUse[j]==least)pool.add(j);}int j=pool.get(random.nextInt(pool.size()));dogUse[j]++;return j;}
    int maddieNode(int job){switch(job){case 0:case 3:case 9:case 18:return M_SOFA;case 1:case 2:return man.node==SINK&&!man.away?M_RUG:M_SINK;case 5:case 11:case 13:return M_TABLE;case 6:return M_BED;case 7:return M_WINDOW;case 4:return M_RUG;default:return M_RUG;}}
    int pickMaddie(int previous){ArrayList<Integer> pool=new ArrayList<>();int min=Integer.MAX_VALUE;for(int i=0;i<maddieJobs.length;i++){int j=maddieJobs[i];if(j==maddie.job||20+sceneGroup(j)==previous)continue;if(j==4&&!man.away&&man.job==4)continue;if(maddieUse[i]<min){min=maddieUse[i];pool.clear();}if(maddieUse[i]==min)pool.add(i);}int i=pool.get(random.nextInt(pool.size()));maddieUse[i]++;return maddieJobs[i];}
    void wakeMaddie(long now,int previous){
        if(maddiePresence==AWAY||(maddiePresence==UNKNOWN&&random.nextInt(6)==0&&previous<40)){placeForWake(maddie,8,DOOR,now);return;}
        int j=pickMaddie(previous);placeForWake(maddie,j,maddieNode(j),now);
    }
    public void setHousehold(boolean enabled){if(household==enabled)return;household=enabled;if(enabled){maddie.away=false;maddie.until=time;}else{maddie.away=true;maddie.route.clear();}}
    public void setPresence(int self,int partner){
        if(self!=selfPresence){selfPresence=self;if(self==AWAY)placeForWake(man,8,DOOR,time);else if(self==HOME&&(man.away||man.job==8)){placeForWake(man,16,DOOR,time);set(man,16,LOUNGE,time);greet();}}
        if(partner!=maddiePresence){maddiePresence=partner;if(household){if(partner==AWAY)placeForWake(maddie,8,DOOR,time);else if(partner==HOME&&maddie.away){placeForWake(maddie,16,DOOR,time);maddie.route.addAll(path(DOOR,M_RUG));greet();}}}
    }
    void greet(){if(dog.route.isEmpty())set(dog,2,DOOR,time);}
    public boolean maddiePetting(){return household&&!maddie.away&&maddie.job==4&&maddie.route.isEmpty()&&dog.job==9&&dog.route.isEmpty();}
    private void chooseMaddie(long now){
        if(!household)return;
        if(maddiePresence==AWAY){maddie.away=true;maddie.until=now+60000;return;}
        if(maddiePresence==UNKNOWN&&!maddie.away&&random.nextInt(15)==0){maddie.lastJob=maddie.job;maddie.job=8;maddie.route.addAll(path(maddie.node,DOOR));maddie.dwell=45000;return;}
        if(maddie.away){maddie.away=false;maddie.x=POS[DOOR][0];maddie.y=POS[DOOR][1];maddie.node=DOOR;}
        int j=pickMaddie(-1);maddie.lastJob=maddie.job;maddie.job=j;maddie.dwell=duration(maddie,j,HUMAN[j],j==6&&night(now));maddie.route.addAll(path(maddie.node,maddieNode(j)));maddie.until=now+maddie.dwell;
        if(j==4&&man.job!=4&&dog.route.isEmpty())set(dog,9,PET_SPOT,now);
    }
    private void stepMaddie(long now,float dt){
        if(!household)return;
        if(!maddie.route.isEmpty()){
            int n=maddie.route.peek();float dx=POS[n][0]-maddie.x,dy=POS[n][1]-maddie.y,d=(float)Math.hypot(dx,dy),speed=dt*24;
            if(Math.abs(dx)>.1f)maddie.face=dx>0?1:-1;
            if(d<=speed){maddie.x=POS[n][0];maddie.y=POS[n][1];maddie.node=n;maddie.route.remove();if(maddie.route.isEmpty()){maddie.until=now+Math.max(1000,maddie.dwell);if(maddie.job==8)maddie.away=true;}}
            else {maddie.x+=dx*speed/d;maddie.y+=dy*speed/d;}
        }else if(now>=maddie.until)chooseMaddie(now);
    }
    public String rotation(){return Arrays.toString(wakeUse)+";"+Arrays.toString(humanUse)+";"+Arrays.toString(dogUse)+";"+Arrays.toString(maddieUse)+";"+lastDogWake;}
    public void restoreRotation(String value){if(value==null)return;try{String[] chunks=value.split(";");int[][] all={wakeUse,humanUse,dogUse,maddieUse};for(int i=0;i<all.length;i++){String[] parts=chunks[i].replace("[","").replace("]","").split(",");if(parts.length!=all[i].length)return;for(int j=0;j<parts.length;j++)all[i][j]=Math.max(0,Math.min(1000000,Integer.parseInt(parts[j].trim())));}lastDogWake=Integer.parseInt(chunks[4]);}catch(RuntimeException ignored){}}
    static ArrayDeque<Integer> path(int start,int end){int[] prev=new int[POS.length];Arrays.fill(prev,-1);prev[start]=start;ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);while(!q.isEmpty()){int p=q.remove();if(p==end)break;for(int n:EDGES[p])if(prev[n]<0){prev[n]=p;q.add(n);}}ArrayDeque<Integer> out=new ArrayDeque<>();if(prev[end]>=0)for(int p=end;p!=start;p=prev[p])out.addFirst(p);return out;}
    private boolean night(long now){Calendar cal=Calendar.getInstance();cal.setTimeInMillis(now);int h=cal.get(Calendar.HOUR_OF_DAY);return h>=22||h<7;}
    private long duration(Actor a,int index,Job j,boolean sleep){
        long d=1000L*(sleep?1200+random.nextInt(3601):j.min+random.nextInt(j.max-j.min+1));
        if(demo)return Math.max(3500,Math.min(18000,d/12));
        if(!lively)return d;
        // A living wallpaper must be interesting during short lock-screen visits.
        if(a!=dog&&index==6)return sleep?60000+random.nextInt(120001):30000+random.nextInt(30001);
        if(a==man&&index==8)return 45000+random.nextInt(75001);
        if(a==dog&&(index==1||index==5||index==7))return 18000+random.nextInt(27001);
        if(a==dog&&index==8)return 4000+random.nextInt(4001);
        return a!=dog?8000+random.nextInt(16001):5000+random.nextInt(13001);
    }
    private long livelyLimit(Actor a){if(a==man&&a.job==6)return 180000;if(a==man&&a.job==8)return 120000;if(a==dog&&sleeping(a))return 45000;return a==man?24000:18000;}
    public void setLively(boolean enabled){
        if(lively==enabled)return;lively=enabled;
        if(enabled){for(Actor a:new Actor[]{man,dog}){long cap=livelyLimit(a);a.dwell=Math.min(a.dwell,cap);if(a.route.isEmpty())a.until=Math.min(a.until,time+cap);}}
    }
    private void set(Actor a,int j,int dest,long now){Job job=(a==man?HUMAN:DOG)[j];a.lastJob=a.job;a.job=j;a.dwell=duration(a,j,job,a==man&&j==6&&night(now));a.route.clear();a.route.addAll(path(a.node,dest));if(a==dog&&j==8){a.route.add(RUG_LEFT);a.route.add(RUG);a.route.add(PET_SPOT);a.route.add(RUG);}if(a.route.isEmpty())arrive(a,now);else a.until=now+a.dwell;}
    private void arrive(Actor a,long now){a.until=now+Math.max(1000,a.dwell);if(a==man&&a.job==8)a.away=true;}
    void choose(Actor a,long now){boolean human=a==man;Job[] jobs=human?HUMAN:DOG;boolean night=night(now);
        if(human&&selfPresence==AWAY){a.away=true;a.until=now+60000;return;}
        if(human&&a.away){a.away=false;set(a,16,LOUNGE,now);return;}
        if(!human&&((man.job==4&&!man.away)||(household&&!maddie.away&&maddie.job==4&&man.job!=4))){set(a,9,PET_SPOT,now);return;}
        int[] weights=new int[jobs.length];int total=0;for(int i=0;i<jobs.length;i++){int w=jobs[i].weight;if(human&&i==6)w=night?(lively?24:55):2;if(human&&i==8&&(night||selfPresence==HOME))w=0;if(!human){if(man.away){if(i==6||i==10||i==12)w=0;if(i==2||i==1)w*=3;}else if(i==10){w=(man.job==0||man.job==3||man.job==9||man.job==18)?20:0;}else if(i==12){w=7;}else if(i==7&&man.job==6)w*=5;else if(i==6&&(man.job==1||man.job==2))w*=3;}if(!human&&night&&(i==1||i==5||i==7))w*=3;if(i==a.job)w=lively?0:w/4;if(lively&&w>0)w=Math.max(1,10/(1+(human?humanUse[i]:dogUse[i])));weights[i]=w;total+=w;}int roll=random.nextInt(total),j=0;for(;j<weights.length-1;j++){roll-=weights[j];if(roll<0)break;}(human?humanUse:dogUse)[j]++;int dest=jobs[j].node;if(!human&&(j==6||j==12))dest=companionNode(man.route.isEmpty()?man.node:man.route.peekLast());set(a,j,dest,now);
        if(human&&j==4&&!dog.away&&dog.route.isEmpty())set(dog,9,PET_SPOT,now);if(human&&j==8&&dog.route.isEmpty()&&random.nextBoolean())set(dog,2,DOOR,now);if(human&&j==6&&dog.route.isEmpty()&&random.nextBoolean())set(dog,7,FOOT,now);
    }
    static int companionNode(int n){switch(n){case SOFA: return SOFA_DOG;case M_SOFA:return SOFA_DOG;case M_TABLE:return SNACKS;case M_BED:return FOOT;case M_RUG:return PET_SPOT;case M_SINK:return KITCHEN;case M_WINDOW:return WINDOW_APPROACH;case SINK:return KITCHEN;case TABLE:return SNACKS;case BED:return FOOT;case WINDOW:return WINDOW_APPROACH;case DOOR:return HALL;case FOOT:case BED_ENTRY:return RUG;case RUG_LEFT:return RUG;case PET_SPOT:return RUG_LEFT;default:return n==RUG?RUG_LEFT:n;}}
    public void previewActivity(boolean human,int j){Actor a=human?man:dog;if(j<0||j>=(human?HUMAN:DOG).length)return; if(!a.route.isEmpty())return;if(human)a.away=false;set(a,j,(human?HUMAN:DOG)[j].node,time);if(human&&j==4&&dog.route.isEmpty())set(dog,9,PET_SPOT,time);}
    public void next(){if(man.route.isEmpty())choose(man,time);if(dog.route.isEmpty()&&man.job!=4)choose(dog,time);}
    public static final int WALK=0, SIT_SOFA=1, JUMP_SOFA=2, SIT_TABLE=3, BED_TRANSFER=4;
    public static int transition(int from,int to){
        if(from==SOFA||to==SOFA||from==M_SOFA||to==M_SOFA)return SIT_SOFA;
        if(from==SOFA_DOG||to==SOFA_DOG)return JUMP_SOFA;
        if(from==TABLE||to==TABLE||from==M_TABLE||to==M_TABLE)return SIT_TABLE;
        if(from==BED||to==BED||from==M_BED||to==M_BED)return BED_TRANSFER;
        return WALK;
    }
    public static boolean furniture(int n){return n==SOFA||n==SOFA_DOG||n==TABLE||n==BED||n==M_SOFA||n==M_TABLE||n==M_BED;}
    public static float progress(Actor a){if(a.route.isEmpty())return 1;int to=a.route.peek();float[] start=POS[a.node],end=POS[to];float total=(float)Math.hypot(end[0]-start[0],end[1]-start[1]);return total<.01f?1:Math.max(0,Math.min(1,1-(float)Math.hypot(end[0]-a.x,end[1]-a.y)/total));}
    public String motion(Actor a){if(a.route.isEmpty())return name(a);int to=a.route.peek(),kind=transition(a.node,to);switch(kind){case JUMP_SOFA:return to==SOFA_DOG?"Jumping onto sofa":"Jumping down";case SIT_SOFA:return to==SOFA?"Sitting down":"Standing up";case SIT_TABLE:return to==TABLE?"Taking a seat":"Leaving the table";case BED_TRANSFER:return to==BED?"Getting into bed":"Getting up";default:return "Walking";}}
    public void step(Actor a,long now,float dt){if(!a.route.isEmpty()){int n=a.route.peek();float dx=POS[n][0]-a.x,dy=POS[n][1]-a.y,d=(float)Math.hypot(dx,dy),s=dt*(a==dog?(a.job==8?65:36):24);int transfer=transition(a.node,n);if(transfer!=WALK){float duration=transfer==JUMP_SOFA?.65f:transfer==BED_TRANSFER?1.3f:.9f;float total=(float)Math.hypot(POS[n][0]-POS[a.node][0],POS[n][1]-POS[a.node][1]);s=dt*total/duration;}if(Math.abs(dx)>0.1)a.face=dx>0?1:-1;if(d<=s){a.x=POS[n][0];a.y=POS[n][1];a.node=n;a.route.remove();if(a.route.isEmpty()){arrive(a,now);if(a==dog&&man.job==4&&!man.away&&a.job!=9)set(a,9,PET_SPOT,now);}}else{a.x+=dx*s/d;a.y+=dy*s/d;}}else if(a==dog&&(a.job==6||a.job==12)&&!man.away&&companionNode(man.route.isEmpty()?man.node:man.route.peekLast())!=a.node&&now<a.until){int target=companionNode(man.route.isEmpty()?man.node:man.route.peekLast());a.route.addAll(path(a.node,target));}else if(now>=a.until||(a==dog&&a.job==9&&(man.job!=4||man.away)&&!(household&&!maddie.away&&maddie.job==4&&man.job!=4)))choose(a,now);}
    public void advance(long now){if(now<time){time=now;man.until=now;dog.until=now;maddie.until=now;return;}if(now-time>86400000L){time=now-86400000L;man.until=time;dog.until=time;maddie.until=time;}while(time<now){boolean moving=(household&&!maddie.route.isEmpty())||!man.route.isEmpty()||!dog.route.isEmpty()||(dog.job==6||dog.job==12)&&!man.away&&companionNode(man.node)!=dog.node;long next=moving?Math.min(now,time+100):Math.min(now,Math.max(time+1,Math.min(Math.min(man.until,dog.until),household?maddie.until:Long.MAX_VALUE)));float dt=(next-time)/1000f;time=next;step(man,time,dt);stepMaddie(time,dt);step(dog,time,dt);}}
}

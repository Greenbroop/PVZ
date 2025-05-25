import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip;

public class Plant<T> extends Actor{
    private T type;
    private boolean idle=true, threaten=false, exploded=false;    // trạng thái
    private Timer timer, timer2, timer3;     // dùng cho Peashooter, Repeater, Sunflower
    private int x, y;     // vị trí cây trong lưới 5x9
    private int cw=74, ch=76;     // kích thước cây (dùng cho Cherry)
    private static int[][] occ = new int[5][10];    // trạng thái chiếm chỗ trong lưới
    private static Point[][] coor = new Point[5][9];     // tọa độ pixel tương ứng với lưới
    private Clip clip, clip2;    // âm thanh (cherry)
    private Thread tcherry;     // thread chờ nổ cherry bomb
    public Plant(T type, int x, int y){
        this.type=type;
        this.x=x;
        this.y=y;
        if(type.equals(1)){ //Sunflower
            super.health = 50;
        }else if(type.equals(2)){ //Peashooter
            super.health = 50;
        }else if(type.equals(3)){ //Repeater
            super.health = 70;
        }else if(type.equals(4)){ //Wallnut
            super.health = 300;
        }else if(type.equals(5)){ //Cherrybomb
            super.health = 200;
            tcherry = new Thread(new CherryWaits()); 
            try{
                clip = AudioSystem.getClip();    // tiếng "phồng lên"
                clip2 = AudioSystem.getClip();    // tiếng nổ
                clip.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Cherry_enlarge.wav")))); 
                clip2.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Cherrybomb.wav")))); 
            }catch(Exception ex){ 
                ex.printStackTrace();
            }
        }else{}
    }

    {
       // Peashooter bắn mỗi 2 giây
        timer=new Timer(2000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                World.peas.add(new Pea((int)type, x, y));
            }
        });
        
        // Repeater bắn viên thứ hai sau 2.2 giây
        timer2=new Timer(2000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                World.peas.add(new Pea(3, x, y));
            }
        });
        timer2.setInitialDelay(2200);

        // Sunflower tạo mặt trời mỗi 10 giây
        timer3=new Timer(10000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                World.suns.add(new Sun(x, y));
            }
        });
    }

    //getter
    public int getX(){return x;}
    public int getY(){return y;}
    public T getType(){return type;}
    public int getHealth(){return health;}
    public boolean isThreaten(){return threaten;}
    public static int getOcc(int x, int y){return occ[x][y];}
    public static Point getCoor(int x, int y){return coor[x][y];}
    public boolean isIdle(){return idle;}
    
    //setter
    public static void setOcc(int i, int j){
        occ[i][j]=0;
    }
    public static void setCoor(int i, int j){
        coor[i][j]=new Point(296+j*81,117+i*98);
    }
    public void setThreat(boolean threat){
        threaten=threat;
    }

    public boolean put(int x, int y, T type){
        if(occ[x][y]==0){ // nếu ô trống
            occ[x][y]=(int)type;
            World.plants.add(new Plant<Integer>((int)type, x, y)); // thêm vào danh sách cây
            return true;
        }else{
            return false;    // ô đã có cây
        }
    }
    public void attack(){
        timer.start();
        if(type.equals(3)){ // Peashooter bắn
            timer2.start();
        }
        idle=false;
    }
    public void act(){
        timer3.start();
    }
    public void stop(){
        timer.stop();
        timer2.stop();
        timer3.stop();
        idle=true;
    }


    //cherrybomb
    private class CherryWaits implements Runnable { 
        public void run() { 
            try{
                Thread.sleep(800); //đợi 800ms rồi nổ
            } catch (InterruptedException e) {}
        }
    } 
    public void startTimer(){
        tcherry.start();
    }

    public void enlarge(){
        cw+=1; ch+=1;
    }
    public int getCw(){return cw;}
    public int getCh(){return ch;}
    public boolean isExploded(){return exploded;}
    public boolean isTcherryAlive(){return tcherry.isAlive();}
    public void setExplode(){
        exploded=true;
    }
    public void cherry_enlarge(){ // phát âm thanh cherry phồng lên
        clip.start();
    }
    public void cherrybomb(){ // phát âm thanh nổ
        clip.stop();
        clip2.start();
    }
}

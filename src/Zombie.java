import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;
import java.util.Collections;
import javax.swing.Timer;
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 

public class Zombie extends Actor implements Comparable<Zombie>{
    private int type, zombieDamage, lane, coorY, yp;
    private float zombieSpeed, coorX; 
    private int[] column = {296,377,458,539,620,701,782,863,944}; 
    private static int[] arrY = new int[5]; 
    private static int n=0, max=50, interval, random, wave=20;
    private static boolean gameOver=false;
    private static Timer timer; //bộ đếm thời gian zombie
    private Timer timer2; 
    private Clip clip;

    public Zombie(int type){
        this.type=type;
        coorX=1020f;
        coorY=arrY[setLane()];
        if(type==1){ //zombie thường
            super.health=50;
            zombieDamage=12;
            zombieSpeed=0.3f;
        }else if(type==2){ //Football zombie
            super.health=90;
            zombieDamage=15;
            zombieSpeed=0.5f;
        }else if(type==3){ //Flying zombie
	        super.health=60;
            zombieSpeed=0.4f;
        }
    }

    {
       //tấn công cây mỗi 1 giây
        timer2=new Timer(1000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                for(Plant plant: World.plants){
                    if(plant.getX()==lane && plant.getY()==yp){ 
                        if(!Audio.isEating() && !gameOver){
                            Audio.eat();
                        }
                        plant.hit(zombieDamage); 
                    }
                }
            }
        });
        timer2.setInitialDelay(200);
    }

    static{
        for(int i=0;i<5;i++){
            arrY[i]=117+i*98-82; 
        }
    }

    //tự động sinh ra zombie
    public static void start(int inter){
        interval=inter;
        timer=new Timer(interval*1000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(n<max){
                    n++; //tăng số lượng zombie
                    World.zombies.add(new Zombie(setType())); //triển khai zombie
                    //sắp xếp zombie dựa trên làn đường
                    Collections.sort(World.zombies);
                    
                    playAudio();
                }
            }
        });
        timer.start();
        timer.setDelay(interval*900);
    }
    public static void stop(){
        timer.stop(); //dừng triển khai zombie
    }

    @Override
	public int compareTo(Zombie z) { //sắp xếp zombie dựa trên làn đường
		return lane-z.getLane();
	}


    //getter
    public static int getN(){return n;}
    public static int getMax(){return max;}
    public static int getWave(){return wave;}
    public int getType(){return type;}
    public int getDamage(){return zombieDamage;}
    public int getHealth(){return health;}
    public float getCoorX(){return coorX;}
    public int getCoorY(){return coorY;}
    public int getLane(){return lane;}
    
    public int getColumn(){ 
        int c=9;
        if(type==2){ //football zombie
            A: for(int i=8;i>=1;i--){
                if(coorX<=column[i]-18 && coorX>column[i-1]-18){
                    c=i;
                    break A;
                }else if(coorX<=column[0]-18){
                    c=0;
                }
            }
        }else{
            A: for(int i=8;i>=1;i--){
                if(coorX<=column[i] && coorX>column[i-1]){
                    c=i;
                    break A;
                }else if(coorX<=column[0]){
                    c=0;
                }
            }
        }
        return c;
    }
   
    public int getColumnEat(){ 
        int c=9;
        if(type==2){ //football zombie
            A: for(int i=8;i>=0;i--){
                if(coorX<=column[i]-14 && coorX>column[i]-78){
                    c=i;
                    break A;
                }
            }
        }else{
            A: for(int i=8;i>=0;i--){
                if(coorX<=column[i]+4 && coorX>column[i]-60){
                    c=i;
                    break A;
                }
            }
        }
        return c;
    }
    
    //setter
    public int setLane(){
        lane=(int)(Math.random() * 5); //tạo làn đường zombie từ 0 đến 4
        return lane;
    }
    
    public static int setType(){
        if(n<=3){ //dễ
            timer.setDelay(interval*550);
            return 1; //zombie thường
        }else if(n<=6){ //thường
            timer.setDelay(interval*250);
            if((int)(Math.random() * 3)==2){ //tạo ra loại zombie từ 0 đến 2
                return 2; //football zombie
            }else{
                return 1; //zombie thường
            }
        }else if(n<=wave){ //khó
            timer.setDelay(interval*200);
            if(n==wave){ //dừng lại khi đợt zombie cuối cùng sắp tới
                timer.stop(); //wait for final wave
            }
            random=(int)(Math.random() * 4); //tạo ra loại zombie từ 0 đến 3
            if(random<=1){ //0 or 1
                return 1; //normal zombie
            }else if(random==2){ //2
                return 2; //football zombie
            }else{ //3
                return 3; //Zombie bay
            }
        }else{ //(n<=max) extreme
            timer.setDelay(interval*100);
            random=(int)(Math.random() * 6); //tạo ra loại zombie từ 0 đến 5
            if(random<=2){ 
                return 2; //football zombie
            }else if(random<=4){ 
                return 3; //zombie bay
            }else{ //5
                return 1; //zombie thường
            }
        }
    }
    
    public void attack(){
        yp=getColumnEat();
       //kiểm tra xem zombie có giao nhau với cây không
        if(Plant.getOcc(lane, yp)!=0){ //intersect plant
            A: for(Plant plant: World.plants){
                if(plant.getX()==lane && plant.getY()==yp){
                    timer2.start();
                    if(plant.isDead()){ //cây chết
                        plant.stop(); //dừng hoạt động của cây
                        Plant.setOcc(lane, yp); //đặt chỗ trống
                        timer2.stop(); //dừng tấn công cây
                        World.plants.remove(plant);
                        break A;
                    }
                }
            }
        }else{ //trường trống
            move();
        }
    }
    public void move(){
        coorX-=zombieSpeed; //di chuyển
    }
    public void stopEat(){
        timer2.stop(); //ngừng ăn thực vật
    }

    public static void startWave(){ //bắt đầu đợt sóng cuối cùng
        Audio.wave(); //phát wave audio
        timer.setInitialDelay(4000);
        timer.start();
        World.setWave(1); //đặt wave to 1
    }
    
    public boolean gameOver(){
        if(coorX>210){ //zombie vẫn chưa tới nhà
            return false;
        }else{ //zombie tới nhà
            gameOver=true;
            return true;
        }
    }
    public static void resetGameOver(){gameOver=false;}
    public static void resetN(){n=0;}
    

    //Audio
    public static void playAudio(){
        if(n==1){
            Audio.groan1();
        }else if(n==2){
            Audio.brain1();
        }else if(n==3){
            Audio.groan2();
        }else if(n==5){
            Audio.brain2();
        }else if(n==6){
            Audio.brain3();
        }else if(n==8){
            Audio.groan3();
        }else if(n==10){
            Audio.groan2();
        }else if(n==15){
            Audio.brain1();
        }else if(n==18){
            Audio.groan1();
        }else if(n==20){
            Audio.groan2();
        }

        if(n==wave+1){
            Audio.siren(); //phát siren audio
            World.setWave(2); //set wave to 2
        }else if(n==wave+2){
            Audio.brain1();
            World.setWave(0); //set wave to 0
        }else if(n==wave+5){
            Audio.groan3();
        }else if(n==wave+8){
            Audio.groan1();
        }
    }

    public void yuck(){ //phát yuck sound
        try{
            // tạo tham chiếu clip 
            clip = AudioSystem.getClip(); 
            // mở audioInputStream vào clip 
            clip.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Yuck.wav")))); 
        }catch(Exception ex){ 
            ex.printStackTrace();
        } 
        clip.start();
    }
    public void yuck2(){ //phát yuck2 sound
        try{
            // tạo tham chiếu clip 
            clip = AudioSystem.getClip(); 
            // mở audioInputStream vào clip
            clip.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Yuck2.wav")))); 
        }catch(Exception ex){ 
            ex.printStackTrace();
        } 
        clip.start();
    }
}

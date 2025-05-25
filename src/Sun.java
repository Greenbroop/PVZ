import java.awt.geom.Ellipse2D;
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;
import java.lang.Math;
import javax.swing.Timer;
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 

public class Sun{
    private int sunX, sunY, limitSunY; // tọa độ (x, y) hiện tại của đồng tiền mặt trời và vị trí giới hạn rơi
    private boolean sunflower, waiting=false; // xác định đây là đồng tiền từ sunflower hay đồng tiền rơi + trạng thái chờ
    private Ellipse2D e_sun; // hình elip biểu diễn mặt trời để vẽ trên màn hình
    private static Timer timer; // timer để tạo đồng tiền mặt trời rơi tự động mỗi 5 giây
    private Clip clip;    // dùng để phát âm thanh
    private Thread tsun; // thread dùng cho thời gian chờ

    public Sun(){
        setX();    // random vị trí x
        sunY=-85;    // bắt đầu rơi từ ngoài màn hình (phía trên)
        setLimit();    // random vị trí y giới hạn rơi (vị trí dừng)
        sunflower=false;    // không phải mặt trời do sunflower tạo
    }

    public Sun(int x, int y){
        sunX=Plant.getCoor(x,y).getX()-15;
        sunY=Plant.getCoor(x,y).getY()-30;
        sunflower=true;    // đánh dấu đây là đồng tiền từ sunflower
    }

    {
        tsun = new Thread(new SunWaits()); 
    }

    public static void start(){
        timer=new Timer(5000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                World.suns.add(new Sun());    // mỗi 5 giây tạo 1 đồng tiền mặt trời mới
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    public static void stop(){
        timer.stop();
    }

    //private class Threading
    private class SunWaits implements Runnable { 
        public void run() { 
            try{
                Thread.sleep(3000); // đợi 3 giây 
            } catch (InterruptedException e) {}
        }
    } 
    public void startTimer(){
        tsun.start();
    }

    //getter
    public int getX(){return sunX;}
    public int getY(){return sunY;}
    public int getLimit(){return limitSunY;}
    public boolean isSunflower(){return sunflower;}
    public Ellipse2D getE(){return e_sun;}
    public boolean isTsunAlive(){return tsun.isAlive();}
    public boolean isWaiting(){return waiting;}

    //setter
    public void setE(Ellipse2D e_sun){
        this.e_sun=e_sun;
    }
    public void setX(){
        sunX=(int)(Math.random() * (900-270+1)+270); // random x từ 270 đến 900
    }
    public void setLimit(){ 
        limitSunY=(int)(Math.random() * (470-200+1)+200); // random y từ 200 đến 470 (giới hạn rơi)
    }
    public void setWaiting(){
        waiting=true;
    }

    public void lower(){ // làm mặt trời rơi xuống (tăng tọa độ y)
        sunY+=2;
    }

    public void points(){ //âm thanh khi nhận điểm
        try{ 
            clip = AudioSystem.getClip(); 
            clip.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Points.wav")))); 
        }catch(Exception ex){ 
            ex.printStackTrace();
        }
        clip.start();
    }
}

import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 

public class Pea{
    private int type, damage;
    private int x, y; // Vị trí logic trên lưới 5x9
    private int coorX, coorY; // Tọa độ thực trên màn hình
    private static Point[][] pea_f = new Point[5][9]; // Ma trận chứa tọa độ của từng ô
    private Clip clip;  // Dùng để phát âm thanh
    
    public Pea(int type, int x, int y){
        this.type=type;
        this.x=x;
        this.y=y;
        convert(x,y); // chuyển x, y thành coorX, coorY
        if(type==2){ //Peashooter
        	damage=6;
        }
        else if(type==3){ //Repeater
        	damage=5;
        }
    }

    static{
        for(int i=0;i<5;i++){
            for(int j=0;j<9;j++){
                // tọa độ chính xác để viên đạn bay ra từ từng vị trí cây trồng
                pea_f[i][j] = new Point(296+j*81+28, 117+i*98-19);
            }
        }
    }

    //getter
    public int getCoorX(){return coorX;}
    public int getCoorY(){return coorY;}
    public int getX(){return x;}
    public int getY(){return y;}
    public int getType(){return type;}
    public int getDamage(){return damage;}

    public void move(){
        coorX+=6; //speed = 6
    }
    public void convert(int i, int j){ //chuyển đổi vị trí hạt đậu thành tọa độ
        coorX=pea_f[i][j].getX();
        coorY=pea_f[i][j].getY();
    }

    public void splat(){ //va chạm bình thường
        try{  
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Splat.wav")))); 
        }catch(Exception ex){ 
            ex.printStackTrace();
        }
        clip.start();
    }
    public void shieldhit(){ //va chạm với zombie có khiên
        try{
            // tạo clip 
            clip = AudioSystem.getClip();
            // mở audioInputStream  
            clip.open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Shieldhit.wav")))); 
        }catch(Exception ex){ 
            ex.printStackTrace();
        }
        clip.start();
    }
}

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 

public class Audio{
    private static Clip[] clip = new Clip[20]; 
    private static Timer timer; //Đặt thời gian

    static{
        try{
            
            for(int i=0;i<20;i++){
                clip[i] = AudioSystem.getClip(); 
            }
            //Nhạc+mở 20 file âm thanh
            clip[0].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Menu.wav")))); 
            clip[1].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Background.wav")))); 
            clip[2].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Win.wav")))); 
            clip[3].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Lose.wav")))); 
            clip[4].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Zombies_coming.wav")))); 
            clip[5].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Seedlift.wav")))); 
            clip[6].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Plant.wav")))); 
            clip[7].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Eat.wav")))); 
            clip[8].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Buzzer.wav")))); 
            clip[9].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Evillaugh.wav")))); 
            clip[10].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Shovel.wav")))); 
            clip[11].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Remove.wav")))); 
            clip[12].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Wave.wav")))); 
            clip[13].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Siren.wav")))); 
            clip[14].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Groan_brains1.wav")))); 
            clip[15].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Groan_brains2.wav")))); 
            clip[16].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Groan_brains3.wav")))); 
            clip[17].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Groan1.wav")))); 
            clip[18].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Groan2.wav")))); 
            clip[19].open(AudioSystem.getAudioInputStream(Audio.class.getResource(("Assets/wav/Groan3.wav")))); 
        }catch(Exception ex)  { 
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot open audio!"); //thông báo lỗi
        } 

        //12 giây phát âm thanh zombie đến
        timer = new Timer(12000, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                clip[4].setMicrosecondPosition(0);
                clip[4].start(); 
                timer.stop();
            }
        });
    }
    //Phát nhạc menu
    public static void menu(){
        clip[0].start(); 
        clip[0].loop(Clip.LOOP_CONTINUOUSLY);
    }
    //Phát tiếng cười ác quỷ
    public static void evillaugh(){
        clip[0].stop();   // Dừng nhạc menu
        clip[0]=null;
        clip[9].start();     //// Phát tiếng cười (Evillaugh)
    }
    //Bắt đầu game
    public static void begin(){
        clip[9]=null;        // xoá Evillaugh
        clip[2].stop();      // dừng win
        clip[3].stop();      // dừng lose
        
        clip[1].start();     // phát nhạc nền
        clip[1].loop(Clip.LOOP_CONTINUOUSLY);
        timer.start();        // khởi động timer để 12 giây sau phát "zombie coming"
    }

    public static void win(){
        clip[1].stop();     // dừng nhạc nền
        clip[1].setMicrosecondPosition(0);    // về đầu
        
        clip[2].setMicrosecondPosition(0);    // bắt đầu lại
        clip[2].start();    // phát âm thanh thắng
    }
    //Phát âm thanh thua
    public static void lose(){
        clip[1].stop(); 
        clip[1].setMicrosecondPosition(0);
        
        clip[3].setMicrosecondPosition(0);
        clip[3].start();
    }
    //Phát âm thanh khi người chơi nhấc gói hạt giống
    public static void seedlift(){
        clip[5].setMicrosecondPosition(0);
        clip[5].start();
    }
    //Phát âm thanh khi gieo/trồng cây.
    public static void plant(){
        clip[6].setMicrosecondPosition(0);
        clip[6].start();
    }
    //Phát âm thanh khi chọn xẻng
    public static void shovel(){
        clip[10].setMicrosecondPosition(0);
        clip[10].start();
    }
    //Phát âm thanh khi đào/xoá cây bằng xẻng
    public static void remove(){
        clip[11].setMicrosecondPosition(0);
        clip[11].start();
    }
    //Phát âm thanh khi zombie ăn cây
    public static void eat(){
        clip[7].setMicrosecondPosition(0);
        clip[7].start();
    }
    public static boolean isEating(){
        return clip[7].isActive();
    }
    //Phát âm thanh cảnh báo buzzer
    public static void buzzer(){
        clip[8].setMicrosecondPosition(0);
        clip[8].start();
    }
    //Phát âm thanh khi "wave" zombie xuất hiện
    public static void wave(){
        clip[12].setMicrosecondPosition(0);
        clip[12].start();
    }
    //Phát tiếng còi hú cảnh báo.
    public static void siren(){
        clip[13].setMicrosecondPosition(0);
        clip[13].start();
    }
    //Zombie nói "brains"
    public static void brain1(){
        clip[14].setMicrosecondPosition(0);
        clip[14].start();
    }
    public static void brain2(){
        clip[15].setMicrosecondPosition(0);
        clip[15].start();
    }
    public static void brain3(){
        clip[16].setMicrosecondPosition(0);
        clip[16].start();
    }
    //Zombie rên rỉ
    public static void groan1(){
        clip[17].setMicrosecondPosition(0);
        clip[17].start();
    }
    public static void groan2(){
        clip[18].setMicrosecondPosition(0);
        clip[18].start();
    }
    public static void groan3(){
        clip[19].setMicrosecondPosition(0);
        clip[19].start();
    }
}

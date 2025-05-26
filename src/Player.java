import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

public class Player {
    private int sunCredits, temp, choice=0;
    private boolean shovel=false;
    private Font font;

    public Player(){
        sunCredits=50;    // khởi tạo 50 mặt trời lúc bắt đầu
        temp=sunCredits;
        try{
            //tạo phông chữ để sử dụng
            font=Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("Assets/font/Chalkboard.ttc").openStream()).deriveFont(Font.BOLD, 20f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot open font!"); 
        }
    }

    public void draw(Graphics2D g){ //điểm mặt trời
        g.setFont(font); 
        g.setColor(Color.BLACK);
        // lấy thông tin kích thước font
        FontMetrics metrics = g.getFontMetrics(font); 
        if(sunCredits==temp){
            // vẽ số nếu giá trị không thay đổi
            g.drawString(Integer.toString(temp), 91-(metrics.stringWidth(Integer.toString(temp))/2), 136);
            // giảm temp để tạo hiệu ứng chuyển động
        }else if(sunCredits<temp){
            temp-=5;
            g.drawString(Integer.toString(temp), 91-(metrics.stringWidth(Integer.toString(temp))/2), 136);
        }else{ 
            // tăng temp nếu cần hiển thị giá trị cao hơn
            temp+=5;
            g.drawString(Integer.toString(temp), 91-(metrics.stringWidth(Integer.toString(temp))/2), 136);
        }
    }

    public int getCredits(){
        return sunCredits;
    }
    public void addSunCredits(){
        sunCredits+=25;
    }
    //Đặt lại số điểm mặt trời về 50
    public void resetCredits(){
        sunCredits=50;
    }
    
    public int getChoice(){
        return choice;
    }
    public void setChoice(int choice){
        this.choice=choice;
    }
    //trả về giá trị true/false xem người chơi có đang chọn xẻng
    public boolean getShovel(){
        return shovel;
    }
    public void setShovel(boolean shovel){
        this.shovel=shovel;
    }

    public void plant(){
        switch(choice){
            case 1: sunCredits-=50; break; //sunflower
            case 2: sunCredits-=100; break; //peashooter
            case 3: sunCredits-=200; break; //repeater
            case 4: sunCredits-=50; break; //wallnut
            case 5: sunCredits-=150; break; //cherrybomb
        }
    }
}

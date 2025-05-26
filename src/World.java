import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class World extends JPanel implements ActionListener{
    private static final long serialVersionUID = 1L;

    private int pwidth=62, pheight=66, swidth=pwidth, sheight=pheight+5, rwidth=pwidth+2, rheight=pheight+2;
    private Shape[][] field = new Shape[5][9]; // ô vuông cho vùng cắm cây (5 hàng, 9 cột)
    
    private Image[] img = new Image[39];
    private Rectangle[] rec = new Rectangle[8]; 
    private Ellipse2D e_shovel; //hình elip cho cây xẻng 
    private Point mouse = new Point(); //Vị trí chuột
    private int xp, yp, i, j;  // dùng làm tọa độ hoặc biến tạm
    private float fxp; // biến float cho tọa độ, có thể để xử lý vị trí chính xác hơn
    private boolean start=false, play=true, win=false, end_sound=true, sun_clicked=false;
    private static int wave=0; // số đợt zombie hiện tại
    private Timer timer; // Timer chạy game loop 
    private Toolkit t = Toolkit.getDefaultToolkit();

    private Player player;  
    private Plant<Integer> plant = new Plant<Integer>(0, 0, 0);
    private Pea pea;
    private Sun sun;

    public static List<Plant<Integer>> plants = new ArrayList<Plant<Integer>>();
    public static List<Zombie> zombies = new ArrayList<Zombie>();
    public static List<Sun> suns = new ArrayList<Sun>();
    public static List<Pea> peas = new ArrayList<Pea>();
      

    public World(){
        timer = new Timer(25, this); // Timer chạy mỗi 25ms

        try{ //load main menu
            img[0]=t.getImage(getClass().getResource("Assets/image/Menu.jpg"));
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot open image!"); //báo lỗi
        }

        addMouseListener(new MListener()); //lắng nghe click chuột 
        addMouseMotionListener(new MouseMotionAdapter() { //lắng nghe di chuyển chuột
            public void mouseMoved(MouseEvent e) {
                mouse.setX(e.getX());
                mouse.setY(e.getY());
            }
        });

        rec[0] = new Rectangle(445, 525, 135, 42);

        Audio.menu();
        timer.start();
    }


    public void start(){
        player = new Player();
        Sun.start();
        Zombie.start(16);
        
        getImg(); //tải ảnh
        init();
        
        Audio.begin();
        timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();
        repaint();
    }
    

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if(!start){
            //Vẽ main menu
            g.drawImage(img[0], 0, 0, 1024, 625, this);
            
        }else{
            Graphics2D g2 = (Graphics2D) g;

            //Vẽ background
            g.drawImage(img[0], 0, 0, 1024, 625, this);

            //Vẽ thanh tiến độ sóng zombie
            xp = Math.round((205.0f/Zombie.getMax())*Zombie.getN());
            yp = Math.round((190.0f/Zombie.getMax())*Zombie.getN());
            g.drawImage(img[27], 498+205-xp, 588, xp, 16, this); //thanh xanh lá tiến độ (max 205px)
            g.drawImage(img[26], 490, 572, 215, 40, this); //thanh nền
            if(Zombie.getN() <= Zombie.getMax()-5){
                g.drawImage(img[25], 489, 564, 261, 49, this); //lá cờ bình thường
            }else{ // Lá cờ di chuyển lên trên khi còn 5 zombie cuối
                g.drawImage(img[25], 489, 552+Math.round((12.0f/5)*(Zombie.getMax()-Zombie.getN())), 261, 49, this);
            }
            g.drawImage(img[24], 675-yp, 574, 35, 38, this); // đầu zombie di chuyển trên thanh tiến độ
            
            // Vẽ tất cả cây trồng
            Iterator<Plant<Integer>> itpl = plants.iterator(); 
            while (itpl.hasNext()){
                plant=itpl.next();

                xp=Plant.getCoor(plant.getX(),plant.getY()).getX(); 
                yp=Plant.getCoor(plant.getX(),plant.getY()).getY(); 

                if(plant.getType().equals(1)){ //// Sunflower (ảnh động)
                    g.drawImage(img[5], xp-swidth/2, yp-sheight/2, swidth, sheight, this);
                    plant.act();

                }else if(plant.getType().equals(4)){ //Wallnut (hạt dẻ)
                    if(plant.getHealth()>=150){ 
                        g.drawImage(img[37], xp-(pwidth+2)/2, yp-(pheight+4)/2, pwidth+2, pheight+5, this);
                    }else{ //wallnut half life
                        g.drawImage(img[38], xp-(pwidth+2)/2, yp-(pheight+4)/2, pwidth+2, pheight+5, this);
                    }

                }else if(plant.getType().equals(5)){ //cherrybomb
                    if(plant.getCw()<110){ //enlarge
                        g.drawImage(img[30], xp-plant.getCw()/2-4, yp-plant.getCh()/2-4, plant.getCw(), plant.getCh(), this);
                        plant.enlarge();
                        plant.cherry_enlarge(); // phát âm thanh phóng to
                    }else{ // nổ cherrybomb
                        i=plant.getX();
                        j=plant.getY();
                        if(!plant.isExploded()){
                            plant.cherrybomb(); // âm thanh nổ
                            plant.setExplode(); 
                            plant.startTimer(); 
                            Plant.setOcc(i, j); // dọn vị trí trống
                            
                            // diệt zombie trong vùng 3x3 ô xung quanh cherrybomb
                            Iterator<Zombie> itz = zombies.iterator(); 
                            while (itz.hasNext()){
                                Zombie zombie=itz.next();
                                if(zombie.getLane()<=(i+1) && zombie.getLane()>=(i-1) 
                                && zombie.getColumn()<=(j+1) && zombie.getColumn()>=(j-1)){ 
                                    zombie.stopEat(); //stop eating plant
                                    itz.remove();
                                }
                            }
                        }
                        if(plant.isTcherryAlive()){ 
                            // vẽ hiệu ứng nổ
                            g.drawImage(img[31], xp-150, yp-125, 300, 250, this);
                        }else{ // xóa cherrybomb sau khi nổ
                            itpl.remove();
                        }
                    }      

                }else{
                    if(plant.getType().equals(2)){ //peashooter gif
                        g.drawImage(img[6], xp-(pwidth+2)/2, yp-(pheight+2)/2, pwidth+2, pheight+2, this);
                    }else if(plant.getType().equals(3)){ //repeater gif
                        g.drawImage(img[7], xp-(rwidth+20)/2, yp-(rheight+9)/2, rwidth+26, rheight+13, this);
                    }

                    // Kiểm tra zombie phía trước để cây có bắn không
                    xp=plant.getX(); //hàng
                    yp=plant.getY(); //cột

                    A: for(Zombie zombie: zombies){
                        if(xp==zombie.getLane() && yp<=zombie.getColumn()){ 
                            if(plant.isIdle()){
                                plant.attack();
                            }
                            plant.setThreat(true);
                            break A;
                        }else{
                            plant.setThreat(false);
                        }
                    }
                    if(zombies.isEmpty()){ 
                        plant.setThreat(false);
                    }
                    if(!plant.isThreaten()){ 
                        plant.stop();
                    }
                }
            }

            // Vẽ và cập nhật trạng thái zombie
            Iterator<Zombie> itz = zombies.iterator(); 
            while (itz.hasNext()){
                Zombie zombie=itz.next();
                
                // Zombie ăn hoặc di chuyển
                if(zombie.getType()!=3){ // Không phải zombie bay
                    zombie.attack();
                }

                fxp=zombie.getCoorX();
                yp=zombie.getLane(); 

                // Vẽ zombie theo loại
                if(zombie.getType()==1){ // zombie thường
                    g.drawImage(img[8], Math.round(fxp), zombie.getCoorY(), pwidth+11, pheight+53, this);   
                }else if(zombie.getType()==2){ // zombie bóng bầu dục
                    if(zombie.getHealth()>=45){ //zombie đội mũ
                        g.drawImage(img[9], Math.round(fxp), zombie.getCoorY(), this);
                    }else{ //zombie doesn't use helmet
                        g.drawImage(img[20], Math.round(fxp), zombie.getCoorY(), this);
                    }
                }else if(zombie.getType()==3){ // zombie bay
                    g.drawImage(img[33], Math.round(fxp), zombie.getCoorY()-15, 101, 120, this);
                    zombie.move();
                }

                // Kiểm tra va chạm giữa zombie và viên đạn Pea
                Iterator<Pea> itpea = peas.iterator(); 
                while (itpea.hasNext()){
                    pea=itpea.next();
                    if(pea.getX()==yp){ // cùng hàng
                        if(zombie.getType()==1){ // zombie thường
                            if((pea.getCoorX()>=fxp-6) && (pea.getCoorX()<=fxp+92)){
                                pea.splat(); // âm thanh va chạm
                                zombie.hit(pea.getDamage()); //damage zombie
                                itpea.remove(); // xóa viên đạn
                            }
                        }else if(zombie.getType()==2){ // zombie bóng bầu dục
                            if((pea.getCoorX()>=fxp+7) && (pea.getCoorX()<=fxp+105)){
                                pea.shieldhit(); 
                                zombie.hit(pea.getDamage()); 
                                itpea.remove(); 
                            }
                        }else if(zombie.getType()==3){ // zombie bay
                            if((pea.getCoorX()>=fxp+24) && (pea.getCoorX()<=fxp+92)){
                                pea.splat(); 
                                zombie.hit(pea.getDamage()); //gây damage lên zombie
                                itpea.remove(); 
                            }
                        }
                    }
                }

                //Kiểm tra zombie chết
                if(zombie.isDead()){
                    zombie.stopEat(); //dừng ăn 
                    if(zombie.getType()==2){ //football zombie
                        zombie.yuck2(); //animation chết của football zombie
                    }else{
                        zombie.yuck();
                    }
                    itz.remove();	// Xóa zombie khỏi danh sách
                }
                
                if(zombie.gameOver()){ // kết thúc game
                    play=false;
                    if(fxp<=23){ // nếu zombie đã tới nhà
                        itz.remove(); // xóa zombie
                    }
                }
            }

            if(wave==0 && Zombie.getN()==Zombie.getWave() && zombies.isEmpty()){
                Zombie.startWave(); // Bắt đầu đợt sóng tiếp theo
            }

            if(Zombie.getN()==Zombie.getMax() && zombies.isEmpty()){
                play=false;
                win=true;	// Người chơi thắng
            }

            // Vẽ menu cây
            g.drawImage(img[34], 15, 22, 150, 580, this);

            // Vẽ điểm số (sun)
            player.draw(g2);

            if(player.getCredits()<200){
                g.drawImage(img[15], 33, 339, rwidth+2, rheight+2, this); //vẽ repeater 
                if(player.getCredits()<150){
                    g.drawImage(img[32], 30, 512, rwidth+7, rheight+6, this); //vẽ cherrybomb 
                    if(player.getCredits()<100){
                        g.drawImage(img[14], 34, 255, pwidth+2, pheight, this); //vẽ peashooter 
                        if(player.getCredits()<50){
                            g.drawImage(img[13], 34, 164, swidth, sheight, this); //vẽ sunflower 
                            g.drawImage(img[36], 32, 426, swidth-1, sheight-2, this); //vẽ wallnut 
                        }
                    }
                }
            }

            //Vẽ xẻng
            if(!player.getShovel()){ // Xẻng ở vị trí
                g.drawImage(img[22], 171, 548, 70, 70, this);
            }else{ // Đang cầm xẻng
                g.drawImage(img[23], 171, 548, 70, 70, this);
                // Xẻng theo chuột
                g.drawImage(img[21], mouse.getX(), mouse.getY()-70, 68, 70, this);
            }

            if(player.getChoice()==1){ //sunflower
                g2.setComposite(AlphaComposite.SrcOver.derive(0.7f)); 
                g2.drawImage(img[2], mouse.getX()-swidth/2, mouse.getY()-sheight/2, swidth, sheight, this);
                g2.setComposite(AlphaComposite.SrcOver.derive(1f)); 
            }else if(player.getChoice()==2){ //peashooter
                g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
                g2.drawImage(img[3], mouse.getX()-pwidth/2+1, mouse.getY()-pheight/2, pwidth+2, pheight, this);
                g2.setComposite(AlphaComposite.SrcOver.derive(1f));
            }else if(player.getChoice()==3){ //repeater
                g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
                g2.drawImage(img[4], mouse.getX()-rwidth/2+2, mouse.getY()-rheight/2+2, rwidth+2, rheight+2, this);
                g2.setComposite(AlphaComposite.SrcOver.derive(1f));
            }else if(player.getChoice()==4){ //wallnut
                g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
                g2.drawImage(img[35], mouse.getX()-32, mouse.getY()-36, 61, 69, this);
                g2.setComposite(AlphaComposite.SrcOver.derive(1f));
            }else if(player.getChoice()==5){ //cherrybomb
                g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
                g2.drawImage(img[30], mouse.getX()-37, mouse.getY()-38, 74, 76, this);
                g2.setComposite(AlphaComposite.SrcOver.derive(1f));
            }
            
            if(play){
                //vẽ pea
                Iterator<Pea> itpea_p = peas.iterator();
                while (itpea_p.hasNext()){
                    pea=itpea_p.next();
                    if(pea.getType()==2){ //peashooter
                        g.drawImage(img[10], pea.getCoorX(), pea.getCoorY(), this);
                    }else{ //repeater
                        g.drawImage(img[19], pea.getCoorX(), pea.getCoorY(), this);
                    }
                    pea.move();	 // Di chuyển
                        
                    if(pea.getCoorX()>1030){ 
                        itpea_p.remove();	// Xóa nếu bay ra khỏi màn
                    }
                }
            
                //Mặt trời rơi
                Iterator<Sun> its = suns.iterator(); 	
                while (its.hasNext()){
                    sun=its.next();
                    if(sun.isSunflower()){ // Mặt trời do Sunflower tạo ra
                        if(!sun.isWaiting()){ 
                            sun.startTimer(); 	// Bắt đầu đếm thời gian tồn tại của mặt trời
                            sun.setWaiting(); 
                        }
                        if(sun.isTsunAlive()){ 	// Nếu mặt trời chưa bị nhặt
                            g.drawImage(img[1],sun.getX(),sun.getY(),80,80,this);	// Vẽ mặt trời tại vị trí (x,y)
                            sun.setE(new Ellipse2D.Float(sun.getX(), sun.getY(), 80, 80));
                        }else{ 
                            its.remove();	// Nếu mặt trời hết thời gian, loại bỏ khỏi danh sách mặt trời
                        }
                    }else{ //mặt trời từ trời
                        if(sun.getY()<sun.getLimit()){ //mặt trời rơi xuống
                            g.drawImage(img[1],sun.getX(),sun.getY(),80,80,this);
                            sun.setE(new Ellipse2D.Float(sun.getX(), sun.getY(), 80, 80));
                            sun.lower();
                        }else if(sun.getY()<(sun.getLimit()+300)){ 
                            if(!sun.isWaiting()){ 
                                sun.startTimer(); 
                                sun.setWaiting(); 
                            }
                            if(sun.isTsunAlive()){ 
                                g.drawImage(img[1],sun.getX(),sun.getY(),80,80,this);
                                sun.setE(new Ellipse2D.Float(sun.getX(), sun.getY(), 80, 80));
                            }else{ //xóa mặt trời rơi xuồng
                                its.remove();
                            }
                        }
                    }
                }

                if(wave==1){ //1 đợt sóng zombie tới
                    g.drawImage(img[28], 160, 290, 743, 42, this);
                }else if(wave==2){ 
                    g.drawImage(img[29], 380, 280, 300, 61, this);
                }

            }else{ 
                player.setChoice(0);
                for(Plant plant: plants){
                    plant.stop();
                }
                Zombie.stop();
                Sun.stop();
                suns.clear();
                peas.clear();

                if(win){
                    if(end_sound){
                        Audio.win(); //phát âm thanh thắng
                        end_sound=false;
                    }
                    g2.setColor(Color.WHITE);
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
                    g2.fill(rec[2]);
                    g2.setComposite(AlphaComposite.SrcOver.derive(1f));
                    rec[1] = new Rectangle(442, 410, 140, 65);

                    g.drawImage(img[16],263,130,500,250,this); //ảnh thắng
                    g.drawImage(img[17],442,410,140,65,this); 
                    
                }else{ //thua
                    if(end_sound){
                        Audio.lose(); //âm thanh chơi thua
                        end_sound=false;
                    }
                    g2.setColor(Color.WHITE);
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
                    g2.fill(rec[2]);
                    g2.setComposite(AlphaComposite.SrcOver.derive(1f));
                    rec[1] = new Rectangle(400, 395, 220, 45);
                    
                    g.drawImage(img[18],425,85,180,210,this); 
                    g.drawImage(img[11],365,190,this); 
                    g.drawImage(img[12],410,405,200,25,this); 
                }
            }
        }

        g.dispose();
    }
    

    private class MListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) { 
            if(!start){
                if(rec[0].contains(e.getPoint())) { //click bắt đầu
                    Audio.evillaugh();
                    start=true;
                    rec[0]=null;
                    start();
                }
            }else{

                if(play){ // Xử lý khi đang chơi 
                    Iterator<Sun> its = suns.iterator(); 
                    A: while (its.hasNext()){
                        sun=its.next();
                        try{
                            if(sun.getE().contains(e.getPoint())){ //click mặt trời rơi xuống
                                sun.points(); //phát âm thanh lấy sun
                                player.addSunCredits(); //cộng 25 điểm sun
                                sun_clicked=true;	 // Đánh dấu đã nhặt mặt trời
                                its.remove();	 // xóa sun khỏi danh sách
                                break A;
                            }
                        }catch(Exception ex){}
                    }
                    if(!sun_clicked){ //không nhấn vào mặt trời
                        // Kiểm tra xem người chơi click vào ô chọn cây nào (sunflower, peashooter, repeater,...)
                        if(rec[3].contains(e.getPoint())) { //click vào sunflower
                            if(player.getCredits()>=50){	// Kiểm tra đủ điểm sun để mua
                                Audio.seedlift(); //phát seedlift sound
                                player.setChoice((player.getChoice()==1) ? 0:1);	// Chọn hoặc hủy chọn sunflower
                            }else{
                                Audio.buzzer(); //phát buzzer sound
                                player.setChoice(0);	// Không chọn cây nào
                            }
                        }else if(rec[4].contains(e.getPoint())) { //nhấn peashooter
                            if(player.getCredits()>=100){
                                Audio.seedlift(); //phát seedlift sound
                                player.setChoice((player.getChoice()==2) ? 0:2);
                            }else{
                                Audio.buzzer(); //phát buzzer sound
                                player.setChoice(0);
                            }
                        }else if(rec[5].contains(e.getPoint())) { //nhấn repeater
                            if(player.getCredits()>=200){
                                Audio.seedlift(); //phát seedlift sound
                                player.setChoice((player.getChoice()==3) ? 0:3);
                            }else{
                                Audio.buzzer(); //phát buzzer sound
                                player.setChoice(0);
                            }
                        }else if(rec[6].contains(e.getPoint())) { //nhấn wallnut
                            if(player.getCredits()>=50){
                                Audio.seedlift(); //phát seedlift sound
                                player.setChoice((player.getChoice()==4) ? 0:4);
                            }else{
                                Audio.buzzer(); //phát buzzer sound
                                player.setChoice(0);
                            }
                        }else if(rec[7].contains(e.getPoint())) { //nhấn cherrybomb
                            if(player.getCredits()>=150){
                                Audio.seedlift(); //phát seedlift sound
                                player.setChoice((player.getChoice()==5) ? 0:5);
                            }else{
                                Audio.buzzer(); //phát buzzer sound
                                player.setChoice(0);
                            }
                        }else if(player.getChoice()!=0){ 
				// Nếu đã chọn loại cây, xử lý trồng cây trên field
                            A: for(i=0;i<5;i++){
                                for(j=0;j<9;j++){
                                    if(field[i][j].contains(e.getPoint())){ //trồng cây trong ruộng
                                        if(plant.put(i,j,player.getChoice())){ //chỗ trống
                                            Audio.plant(); //phát plant sound
                                            player.plant();	// gọi hàm xử lý khi trồng cây (ví dụ trừ sun)
                                        }
                                        player.setChoice(0);
                                        break A;
                                    }
                                }
                            }
                            if(i==5){	// Nếu không trồng được cây
                                player.setChoice(0);	// Reset lựa chọn cây
                            }
                        }
                    }else{sun_clicked=false;}

                    //kiểm tra xẻng
                    if(player.getShovel()){
                        A: for(i=0;i<5;i++){
                            for(j=0;j<9;j++){
                                if(field[i][j].contains(e.getPoint())){ 
                                    if(Plant.getOcc(i, j)!=0){ 
                                        Plant.setOcc(i, j); //xóa plant
                                        B: for(Plant plant: plants){
                                            if(plant.getX()==i && plant.getY()==j){
                                                plant.stop(); 
                                                Audio.remove(); //phát remove sound
                                                plants.remove(plant);
                                                break B;
                                            }
                                        }
                                        for(Zombie zombie: zombies){
                                            if(zombie.getLane()==i && zombie.getColumn()==j){
                                                zombie.stopEat(); //dừng ăn plant
                                            }
                                        }
                                    }
                                    break A; //chỗ trống
                                }
                            }
                        }
                        player.setShovel(false);

                    }else if(e_shovel.contains(e.getPoint())){ //nhấn vào xẻng
                        player.setShovel(true);
                        Audio.shovel(); //phát shovel sound
                    }

                }else{ //trò chơi không diễn ra
                    if (rec[1].contains(e.getPoint())) { //nhấn để chơi lại
                        play=true;
                        win=false;
                        end_sound=true;
			            for(Zombie zombie: zombies){
                            zombie.stopEat(); //dừng ăn plant
                        }
                        plants.clear();
                        zombies.clear();
                        Zombie.resetN();
                        Zombie.resetGameOver();
			            player.resetCredits();
			            for(i=0;i<5;i++){
                            for(j=0;j<9;j++){
                                Plant.setOcc(i, j);
                            }
                        }
                        
                        Audio.begin();
                        Sun.start();
                        Zombie.start(16);   
                    }
                }
            }
        }
    }


    private void getImg(){
        try{ //tải ảnh
            img[0]=t.getImage(getClass().getResource("Assets/image/Background.jpg"));
            img[1]=t.getImage(getClass().getResource("Assets/image/Sun.png"));
            img[2]=t.getImage(getClass().getResource("Assets/image/Sunflower.png"));
            img[3]=t.getImage(getClass().getResource("Assets/image/Peashooter.png"));
            img[4]=t.getImage(getClass().getResource("Assets/image/Repeater.png"));
            img[5]=t.getImage(getClass().getResource("Assets/gif/Sunflower.gif"));
            img[6]=t.getImage(getClass().getResource("Assets/gif/Peashooter.gif"));
            img[7]=t.getImage(getClass().getResource("Assets/gif/Repeater.gif"));
            img[8]=t.getImage(getClass().getResource("Assets/gif/Zombie.gif"));
            img[9]=t.getImage(getClass().getResource("Assets/gif/Zombief.gif"));
            img[10]=t.getImage(getClass().getResource("Assets/image/Pea_p.png"));
            img[11]=t.getImage(getClass().getResource("Assets/image/Wasted.png"));
            img[12]=t.getImage(getClass().getResource("Assets/image/Tryagain.png"));
            img[13]=t.getImage(getClass().getResource("Assets/image/Sunflower_g.png"));
            img[14]=t.getImage(getClass().getResource("Assets/image/Peashooter_g.png"));
            img[15]=t.getImage(getClass().getResource("Assets/image/Repeater_g.png"));
            img[16]=t.getImage(getClass().getResource("Assets/image/Win.png"));
            img[17]=t.getImage(getClass().getResource("Assets/image/Playagain.png"));
            img[18]=t.getImage(getClass().getResource("Assets/image/Brain.png"));
            img[19]=t.getImage(getClass().getResource("Assets/image/Pea_r.png"));
            img[20]=t.getImage(getClass().getResource("Assets/gif/Zombief_half.gif"));
            img[21]=t.getImage(getClass().getResource("Assets/image/Shovel1.png"));
            img[22]=t.getImage(getClass().getResource("Assets/image/Shovel2.png"));
            img[23]=t.getImage(getClass().getResource("Assets/image/Shovel3.png"));
            img[24]=t.getImage(getClass().getResource("Assets/image/Progress1.png"));
            img[25]=t.getImage(getClass().getResource("Assets/image/Progress2.png"));
            img[26]=t.getImage(getClass().getResource("Assets/image/Progress3.png"));
            img[27]=t.getImage(getClass().getResource("Assets/image/Progress4.png"));
            img[28]=t.getImage(getClass().getResource("Assets/image/HugeWave.png"));
            img[29]=t.getImage(getClass().getResource("Assets/image/FinalWave.png"));
            img[30]=t.getImage(getClass().getResource("Assets/image/Cherry.png"));
            img[31]=t.getImage(getClass().getResource("Assets/image/Powie.png"));
            img[32]=t.getImage(getClass().getResource("Assets/image/Cherry_g.png"));
            img[33]=t.getImage(getClass().getResource("Assets/gif/Zombie_fly.gif"));
            img[34]=t.getImage(getClass().getResource("Assets/image/Background_menu.png"));
            img[35]=t.getImage(getClass().getResource("Assets/image/Wallnut.png"));
            img[36]=t.getImage(getClass().getResource("Assets/image/Wallnut_g.png"));
            img[37]=t.getImage(getClass().getResource("Assets/gif/Wallnut_full.gif"));
            img[38]=t.getImage(getClass().getResource("Assets/gif/Wallnut_half.gif"));
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot open image!"); //hiển thị hộp thoại lỗi
        }
    }

    private void init(){    
        //tạo hình chữ nhật cho menu cây và kết thúc trò chơi
        rec[2] = new Rectangle(0, 0, 1024, 626); //end
        rec[3] = new Rectangle(23, 156, pwidth+73, pheight+21); //sunflower
        rec[4] = new Rectangle(23, 249, pwidth+73, pheight+12); //peashooter
        rec[5] = new Rectangle(23, 333, pwidth+73, pheight+14); //repeater
        rec[6] = new Rectangle(23, 419, pwidth+73, pheight+17); //wallnut
        rec[7] = new Rectangle(23, 508, pwidth+73, pheight+19); //cherrybomb

        //tạo hình elip cho xẻng
        e_shovel = new Ellipse2D.Float(171, 548, 70, 70);

        //tạo vùng hình chữ nhật có thể nhấp vào cho trường
        int[] fw = {0,90,165,250,330,410,492,570,651,749}; 
        int[] fh = {0,118,215,323,405,516}; 
        for(i=0;i<5;i++){
            for(j=0;j<9;j++){
                
                field[i][j] = new Rectangle(245+fw[j], 50+fh[i], fw[j+1]-fw[j], fh[i+1]-fh[i]);

                Plant.setOcc(i, j);
                Plant.setCoor(i, j);
            }
        }
    }

    public static void setWave(int w){
        wave=w;
    }
}

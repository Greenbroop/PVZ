public abstract class Actor{
    protected int health;

    //Khai báo lớp trừu tượng
    public abstract void attack();

    public void hit(int damage){
        health-=damage;
    }

    //Kiểm tra zombie chết chưa
    public boolean isDead(){
        return health<=0;
    }
}
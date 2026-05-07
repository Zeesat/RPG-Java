package fantasyrpg.interfaces;

public interface Attackable {
    void receiveDamage(int damage);
    boolean isAlive();
    String getName();
}


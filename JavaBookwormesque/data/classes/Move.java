package data.classes;

// Move class that determines what kind of action enemy will do
public class Move {
    final private String name;
    final private int damage;

    // Constructor
    public Move(String name, int damage) {
        this.name = name;
        this.damage = damage;
    }

    // Applies damage to Player
    public void damagePlayer(Player target) {
        target.takeDamage(damage);
        System.out.println("Move " + name + " hits " + target.getName() + " for " + damage + " damage!");
    } 
    
    // Getters
    public String getName() { return name; }
    public int getDamage() { return damage; }
}

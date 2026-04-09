package data.classes;

// Player class which just houses player data as well as method for player to take damage
public class Player {
    final private String name = "Hero";
    final private int maxHP = 50;
    private int currentHP = maxHP;
    private final String imagePath = "data/entityImages/player.png";

    // Takes damage based on received damgage from enemy move
    public void takeDamage(int dmg) {
        currentHP = Math.max(0, currentHP - dmg);
        System.out.println(name + " now has " + currentHP + " HP left.");
    }

    // Getters
    public String getName() { return name; }
    public int getMaxHP() { return maxHP; }
    public int getCurrentHP() {return  currentHP;}
    public String getImagePath() { return imagePath; }
    public boolean isAlive() {
        return currentHP > 0;
    }
}

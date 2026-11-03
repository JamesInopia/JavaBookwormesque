package model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Abstract enemy class to be used for every enemy-type entity
public abstract class Enemy {
    final private String name;
    final private String type;
    final private int maxHP;
    private int currentHP;
    final private String imagePath;
    final private List<Move> moves;
    private Random random;

    // Constructor
    public Enemy(String name, String type, int maxHP, String imagePath, List<Move> moves) {
        this.name = name;
        this.type = type;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.imagePath = imagePath;
        this.moves = new ArrayList<>(moves);
        this.random = new Random();
    }

    public abstract void intro();

    // From a list of moves, performs a random one
    public void performMove(Player target) {
        int index = random.nextInt(moves.size());
        Move chosen = moves.get(index);
        System.out.println(name + " uses " + chosen.getName() + "!");
        chosen.damagePlayer(target);
    }

    // Takes damage based on player input
    public void takeDamage(int dmg) {
        currentHP = Math.max(0, currentHP - dmg);
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public int getMaxHP() { return maxHP; }
    public int getCurrentHP() { return currentHP; }
    public String getImagePath() { return imagePath; }
    public boolean isAlive() {
        return currentHP > 0;
    }

}

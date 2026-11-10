package model.entity;

// Move class that determines what kind of action enemy will do
public class Move {
    // Icons shown for the intent above an enemy and in the move guide
    public static final String ATTACK_ICON = "public/icons/attack_icon.png";
    public static final String UNKNOWN_ICON = "public/icons/unknown-action_icon.png";

    final private String name;
    final private int damage;
    final private String iconPath;

    // Constructor (a move uses the attack icon unless told otherwise)
    public Move(String name, int damage) {
        this(name, damage, ATTACK_ICON);
    }

    public Move(String name, int damage, String iconPath) {
        this.name = name;
        this.damage = damage;
        this.iconPath = iconPath;
    }

    // Applies damage to Player
    public void damagePlayer(Player target) {
        target.takeDamage(damage);
        System.out.println("Move " + name + " hits " + target.getName() + " for " + damage + " damage!");
    }

    // Getters
    public String getName() { return name; }
    public int getDamage() { return damage; }
    public String getIconPath() { return iconPath; }
}

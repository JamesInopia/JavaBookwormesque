package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.entity.Enemy;
import model.entity.Player;
import model.level.Level;

// Runs the waves of one level: loads the enemies of each wave, tells when a wave is cleared, and heals the player when a wave ends
public class WaveHandler {
    // Share of the player's max HP restored whenever a wave ends
    private static final double WAVE_CLEAR_HEAL_RATIO = 0.5;

    private final Level level;
    private List<Enemy> enemies = new ArrayList<>();

    public WaveHandler(Level level) {
        if (level == null || !level.hasWaves()) {
            throw new IllegalArgumentException("A level needs at least one wave to be played.");
        }
        this.level = level;
    }

    // Begins the next wave of the level and returns its (fresh) enemies
    public List<Enemy> startNextWave() {
        enemies = level.nextWave().spawnEnemies();
        System.out.println("Level " + level.getNumber() + " - Wave " + level.getCurrentWave() + "/" + level.getMaxWave()
                + " begins with " + enemies.size() + " enemy(ies).");
        return enemies;
    }

    // A wave is cleared once every enemy in it has been defeated
    public boolean isWaveCleared() {
        if (enemies.isEmpty()) return false;
        for (Enemy e : enemies) {
            if (e.isAlive()) return false;
        }
        return true;
    }

    // Enemies of the current wave that are still standing
    public List<Enemy> getLivingEnemies() {
        List<Enemy> living = new ArrayList<>();
        for (Enemy e : enemies) {
            if (e.isAlive()) living.add(e);
        }
        return living;
    }

    // 50% of max HP, always the same amount regardless of current HP, with any decimal truncated (e.g. 51 max HP heals 25)
    public static int healAmountFor(int maxHP) {
        return (int) (maxHP * WAVE_CLEAR_HEAL_RATIO);
    }

    // Heals the player at the end of a wave (HP can never go above max HP), returns the heal amount
    public int healPlayer(Player player) {
        int amount = healAmountFor(player.getMaxHP());
        player.heal(amount);
        return amount;
    }

    // Getters
    public List<Enemy> getEnemies() { return Collections.unmodifiableList(enemies); }
    public Level getLevel() { return level; }
    public int getCurrentWave() { return level.getCurrentWave(); }
    public int getMaxWave() { return level.getMaxWave(); }
    public boolean isLastWave() { return level.isLastWave(); }
}

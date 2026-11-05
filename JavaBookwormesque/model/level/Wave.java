package model.level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import model.entity.Enemy;

// A single wave of a level: the group of enemies the player has to defeat before the wave ends
public class Wave {
    // Most enemies the battle screen can lay out side by side
    public static final int MAX_ENEMIES = 3;

    private final List<Supplier<? extends Enemy>> enemyFactories;

    // A wave always has at least one enemy
    public Wave(List<Supplier<? extends Enemy>> enemyFactories) {
        if (enemyFactories == null || enemyFactories.isEmpty()) {
            throw new IllegalArgumentException("A wave needs at least one enemy.");
        }
        if (enemyFactories.size() > MAX_ENEMIES) {
            throw new IllegalArgumentException("A wave can have at most " + MAX_ENEMIES + " enemies.");
        }
        this.enemyFactories = new ArrayList<>(enemyFactories);
    }

    // Shorthand for building a wave, e.g. Wave.of(Slime::new, Slime::new)
    @SafeVarargs
    public static Wave of(Supplier<? extends Enemy>... enemies) {
        List<Supplier<? extends Enemy>> factories = new ArrayList<>();
        for (Supplier<? extends Enemy> factory : enemies) {
            factories.add(factory);
        }
        return new Wave(factories);
    }

    // Creates a fresh set of enemies every time, so replaying a wave never reuses damaged enemies
    public List<Enemy> spawnEnemies() {
        List<Enemy> enemies = new ArrayList<>();
        for (Supplier<? extends Enemy> factory : enemyFactories) {
            enemies.add(factory.get());
        }
        return enemies;
    }

    public int getEnemyCount() { return enemyFactories.size(); }
}

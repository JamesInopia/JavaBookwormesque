package model.level;

import model.entity.EnemyList.*;

// Defines every level of the game. Levels are created fresh each time so a replay always starts from wave 1 with full-health enemies
public final class LevelCatalog {
    public static final int TOTAL_LEVELS = 10;

    private LevelCatalog() { }

    public static Level create(int number) {
        if (number < 1 || number > TOTAL_LEVELS) {
            throw new IllegalArgumentException("Level must be between 1 and " + TOTAL_LEVELS + ": " + number);
        }

        switch (number) {
            case 1:
                return new Level(1,
                    Wave.of(Slime::new),
                    Wave.of(Wolf::new),
                    Wave.of(Slime::new, Slime::new),
                    Wave.of(Slime::new, Wolf::new),
                    Wave.of(Slime::new, Slime::new, Slime::new)
                );

            // Levels 2-10 are empty for now
            default:
                return new Level(number);
        }
    }
}

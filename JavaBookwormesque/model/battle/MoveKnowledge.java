package model.battle;

import java.util.HashSet;
import java.util.Set;
import model.entity.Enemy;
import model.entity.Move;

// Remembers which enemy moves the player has already seen during one level.
public class MoveKnowledge {
    private final Set<String> revealed = new HashSet<>();

    private static String key(Enemy enemy, Move move) {
        return enemy.getName() + "::" + move.getName();
    }

    // Called when an enemy has used a move
    public void reveal(Enemy enemy, Move move) {
        revealed.add(key(enemy, move));
    }

    public boolean isRevealed(Enemy enemy, Move move) {
        return revealed.contains(key(enemy, move));
    }
}

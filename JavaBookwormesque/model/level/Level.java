package model.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// A level is a fixed number of waves (max wave), tracks which wave the player is on (current wave), and knows the enemies of every wave
public class Level {
    private final int number;
    private final List<Wave> waves;
    private int currentWave = 0;

    // A level without waves is an empty level
    public Level(int number, Wave... waves) {
        this.number = number;
        this.waves = new ArrayList<>(Arrays.asList(waves));
    }

    // Moves on to the next wave and returns it
    public Wave nextWave() {
        if (currentWave >= waves.size()) {
            throw new IllegalStateException("Level " + number + " has no more waves.");
        }
        currentWave++;
        return waves.get(currentWave - 1);
    }

    // Waves are numbered from 1
    public Wave getWave(int waveNumber) {
        return waves.get(waveNumber - 1);
    }

    // Getters
    public int getNumber() { return number; }
    public int getMaxWave() { return waves.size(); }
    public int getCurrentWave() { return currentWave; }
    public boolean hasWaves() { return !waves.isEmpty(); }
    public boolean isLastWave() { return hasWaves() && currentWave >= waves.size(); }
}

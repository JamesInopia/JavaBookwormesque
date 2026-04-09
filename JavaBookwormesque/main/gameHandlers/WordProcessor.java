package main.gameHandlers;
import data.classes.Tile;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class WordProcessor {
    private static final int ROWS = 4, COLS = 4;
    private final Tile[][] columns = new Tile[COLS][ROWS];
    private final List<Tile> holdingRow = new ArrayList<>();
    private JPanel gridPanel, answerRow;
    private final Random rnd = new Random();
    private volatile boolean busy = false;
    private Consumer<Boolean> borderUpdater;

    // Sets of partitions to determine combinations of words of different word-length
    private static final String[][] PARTITIONS_16 = {
        {"16"}, {"13","3"}, {"12","4"}, {"11","5"},
        {"10","6"}, {"10","3","3"}, {"9","7"}, {"9","4","3"},
        {"8","8"}, {"8","5","3"}, {"8","4","4"}, {"7","6","3"},
        {"7","5","4"}, {"7","3","3","3"}, {"6","6","4"}, {"6","5","5"},
        {"6","4","3","3"}, {"5","4","4","3"}, {"5","5","3","3"},
        {"4","4","4","4"}, {"4","3","3","3","3"}
    };
    private static String[] lastPartition = null;
    private String lastPartitionLabel = null;

    // UI creation
    public JPanel createAnswerRow() {
        answerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        answerRow.setOpaque(false);
        return answerRow;
    }

    public JPanel createLetterGrid(JPanel answerRow) {
        gridPanel = new JPanel(new GridLayout(ROWS, COLS, 4, 4));
        gridPanel.setPreferredSize(new Dimension(COLS * 50, ROWS * 50));
        spawnUntilFull();
        return gridPanel;
    }

    public void setBorderUpdater(Consumer<Boolean> updater) {
        this.borderUpdater = updater;
    }

    // Defines behaviour of tile when clicked: adds them to answerRow if in grid, and returns them to grid if in answerRow
    private void attachTileListeners(Tile t) {
        resetListeners(t, e -> {
            if (busy) return;
            busy = true;
            SwingUtilities.invokeLater(() -> {
                try {
                    // Adds Tile from grid to answerRow
                    if (!t.selected) {
                        t.selected = true;
                        holdingRow.add(t);
                        rebuildHoldingPanel();
                        refreshGrid();

                        if (borderUpdater != null) {
                            borderUpdater.accept(isValidWord());
                        }

                    } 
                    else {
                        // Returns tile from answerRow to grid
                        int idx = holdingRow.indexOf(t);
                        if (idx >= 0) {
                            for (int i = idx; i < holdingRow.size(); i++) holdingRow.get(i).selected = false;
                            holdingRow.subList(idx, holdingRow.size()).clear();
                            rebuildHoldingPanel();
                            refreshGrid();

                            if (borderUpdater != null) {
                                borderUpdater.accept(isValidWord());
                            }
                        }
                    }
                } finally {
                    // Small delay to ensure no issues occur during a double click
                    new Timer(40, ev -> { busy = false; ((Timer)ev.getSource()).stop(); }).start();
                }
            });
        });
    }
    
    // Removes all existing ActionListeners from a button and attaches the specified one
    private static void resetListeners(AbstractButton b, ActionListener l) {
        for (ActionListener a : b.getActionListeners()) b.removeActionListener(a);
        b.addActionListener(l);
    }

    // Ensures that only tiles that were called to removed are removed from answerRow
    private void rebuildHoldingPanel() {
        if (answerRow == null) return;
        answerRow.removeAll();
        for (Tile t : holdingRow) answerRow.add(t);
        answerRow.revalidate(); answerRow.repaint();
    }

    // Spawn letters using partition logic
    private void spawnUntilFull() {
        SwingUtilities.invokeLater(() -> {
            int emptySlots = 0;
            for (int c = 0; c < COLS; c++) {
                for (int r = 0; r < ROWS; r++) {
                    if (columns[c][r] == null) emptySlots++;
                }
            }

            // Generate new letters exactly for empty slots
            List<List<Character>> newBlocks = generateLettersFromPartition(emptySlots);

            // Place letters into empty slots sequentially
            for (List<Character> block : newBlocks) {
                for (char c : block) {
                    outer:
                    for (int col = 0; col < COLS; col++) {
                        for (int row = ROWS - 1; row >= 0; row--) {
                            if (columns[col][row] == null) {
                                Tile t = new Tile(c, col, row); 
                                t.selected = false;
                                attachTileListeners(t);
                                columns[col][row] = t;
                                break outer;
                            }
                        }
                    }
                }
            }

            // If empty slots still remain, fill with random letters
            for (int c = 0; c < COLS; c++) {
                for (int r = 0; r < ROWS; r++) {
                    if (columns[c][r] == null) {
                        Tile t = new Tile(randomLetter(), c, r);
                        t.selected = false;
                        attachTileListeners(t);
                        columns[c][r] = t;
                    }
                }
            }

            refreshGrid();
        });
    }

    // Produces random letter
    private char randomLetter() { return (char)('a' + rnd.nextInt(26)); }

    // Applies gravity to the grid, ensuring that a tile in a column will go to the bottom-most slot if vacant
    private void applyGravityAndUpdateHomes() {
        for (int c = 0; c < COLS; c++) {
            List<Tile> compact = new ArrayList<>();
            for (int r = 0; r < ROWS; r++) {
                Tile t = columns[c][r];
                if (t != null && !t.selected) compact.add(t);
            }
            int start = ROWS - compact.size();
            for (int r = 0; r < ROWS; r++) {
                columns[c][r] = (r < start) ? null : compact.get(r - start);
            }
        }
    }

    // Generate blocks of words that exactly fit or partially fit the open slots
    private List<List<Character>> generateLettersFromPartition(int slotsRemaining) {
        Map<Character,Integer> freq = new HashMap<>();
        List<List<Character>> newBlocks = new ArrayList<>();

        // Collect leftovers to maximize reuse
        List<Character> leftovers = collectLeftoverLetters();

        // Choose partition that is greater than or equal to slotsRemaining
        String[] chosen = choosePartitionForSlots(slotsRemaining);
        lastPartition = chosen;
        lastPartitionLabel = String.join("+", chosen);
        System.out.println("[WordProcessor] Partition chosen: " + lastPartitionLabel);

        for (String lenStr : chosen) {
            int len = Integer.parseInt(lenStr);

            // If no slots left, stop
            if (slotsRemaining <= 0) break;

            List<Character> block = new ArrayList<>();
            List<Character> reused = new ArrayList<>();
            List<Character> added = new ArrayList<>();

            String word = fitWordWithLeftovers(len, leftovers, freq);
            if (word == null && MasterDictionary.isReady()) {
                word = MasterDictionary.getRandomWord(len);
            }

            if (word != null && word.length() == len) {
                System.out.println("[WordProcessor] Word chosen: " + word);

                for (char c : word.toCharArray()) {
                    if (leftovers.contains(c)) {
                        leftovers.remove((Character)c);
                        reused.add(c);
                    } else if (slotsRemaining > 0) {
                        block.add(c);
                        added.add(c);
                        freq.put(c, freq.getOrDefault(c, 0) + 1);
                        slotsRemaining--;
                    }
                }
            }

            // Only adds missing letters of a chosen word
            if (!block.isEmpty()) {
                System.out.println("[WordProcessor] Block generated (reused: " + reused + ", added: " + added + ")");
                newBlocks.add(block);
            }
        }

        // If slots remain, try filler word or random letters
        while (slotsRemaining > 0) {
            String filler = MasterDictionary.getRandomWord(slotsRemaining);
            List<Character> block = new ArrayList<>();
            if (filler != null && filler.length() == slotsRemaining) {
                System.out.println("[WordProcessor] Filler word chosen: " + filler);
                for (char c : filler.toCharArray()) {
                    block.add(c);
                    freq.put(c, freq.getOrDefault(c, 0) + 1);
                }
                slotsRemaining = 0;
            } else {
                while (slotsRemaining > 0) {
                    char c = randomLetter();
                    block.add(c);
                    freq.put(c, freq.getOrDefault(c, 0) + 1);
                    slotsRemaining--;
                }
            }
            newBlocks.add(block);
        }

        return newBlocks;
    }

    // Limits the occurence of one same letter to no more than 4 times
    private static boolean canUseWord(String word, Map<Character,Integer> freq) {
        for (char c : word.toCharArray()) if (freq.getOrDefault(c,0)+1 > 4) return false;
        return true;
    }

    // Chooses a partition that can fill the remaining empty slots in the grid.
    private String[] choosePartitionForSlots(int slotsRemaining) {
        List<String[]> valid = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int total = 0;

        for (String[] p : PARTITIONS_16) {
            int sum = Arrays.stream(p).mapToInt(Integer::parseInt).sum();
            if (sum >= slotsRemaining) { // changed from == to >=
                boolean hasLarge = Arrays.stream(p).anyMatch(len -> Integer.parseInt(len) >= 8);
                // Prevents successive use of one partition that has contains a number 8 or higher (still allows successive use of partitions whose highest number is seven or lower)
                if (hasLarge && p == lastPartition) continue;

                int maxLen = Arrays.stream(p).mapToInt(Integer::parseInt).max().orElse(0);
                int w = Math.max(1, 20 - maxLen);
                valid.add(p);
                weights.add(w);
                total += w;
            }
        }

        if (valid.isEmpty()) return PARTITIONS_16[new Random().nextInt(PARTITIONS_16.length)];

        int r = new Random().nextInt(total);
        int cumulativeSum = 0;
        for (int i = 0; i < valid.size(); i++) {
            cumulativeSum += weights.get(i);
            if (r < cumulativeSum) return valid.get(i);
        }
        return valid.get(0);
    }

    // Collect all leftover letters from the grid
    private List<Character> collectLeftoverLetters() {
        List<Character> leftovers = new ArrayList<>();
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                Tile t = columns[c][r];
                if (t != null && !t.selected) {
                    leftovers.add(t.getText().charAt(0));
                }
            }
        }
        return leftovers;
    }

    // Try to fit dictionary words with leftover letters, maximizing reuse
    private String fitWordWithLeftovers(int len, List<Character> leftovers, Map<Character,Integer> freq) {
        if (!MasterDictionary.isReady()) return null;

        String bestWord = null;
        int bestScore = -1;

        int attempts = 0;
        while (attempts++ < 200) {
            String candidate = MasterDictionary.getRandomWord(len);
            if (candidate == null || candidate.isEmpty()) continue;

            // Count how many leftover letters appear in candidate
            int matchCount = 0;
            List<Character> temp = new ArrayList<>(leftovers);
            for (char c : candidate.toCharArray()) {
                if (temp.contains(c)) {
                    temp.remove((Character)c);
                    matchCount++;
                }
            }

            // Accept only if frequency rule is respected
            if (canUseWord(candidate, freq)) {
                if (matchCount > bestScore) {
                    bestScore = matchCount;
                    bestWord = candidate;
                }
            }
        }

        if (bestWord != null) {
            System.out.println("[WordProcessor] Word chosen with leftover reuse (" + bestScore + " reused): " + bestWord);
        }

        return bestWord;
    }

    // Renders grid
    private void refreshGrid() {
        if (gridPanel == null) return;
        gridPanel.removeAll();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile t = columns[c][r];
                if (t == null) gridPanel.add(new JLabel(""));
                else gridPanel.add(t.selected ? new JLabel("") : t);
            }
        }
        gridPanel.revalidate(); gridPanel.repaint();
    }

    // Checks if player built word is a valid word
    public boolean isValidWord() {
        StringBuilder sb = new StringBuilder();
        for (Tile t : holdingRow) sb.append(t.getText());
        String word = sb.toString().trim().toLowerCase();
        if (!word.isEmpty() && MasterDictionary.isReady()) return MasterDictionary.contains(word);
        return !word.isEmpty();
    }

    // Submits the player's built word
    public String commitSelectedAndRefill() {
        StringBuilder sb = new StringBuilder();
        for (Tile t : holdingRow) sb.append(t.getText());
        String committed = sb.toString().trim().toLowerCase();

        // Console tracking
        System.out.println("[WordProcessor] Committed word: \"" + committed + "\" (partition: " + lastPartitionLabel + ")");

        int damage = (int) ApplyDamage.calculateDamage(committed);
        System.out.println("[Damage] " + committed + " dealt " + damage + " damage!");

        // Remove selected tiles
        for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) {
            Tile t = columns[c][r];
            if (t != null && t.selected) columns[c][r] = null;
        }

        // clear holding UI
        holdingRow.clear();
        if (answerRow != null) { answerRow.removeAll(); answerRow.revalidate(); answerRow.repaint(); }

        // gravity + refill
        applyGravityAndUpdateHomes();
        spawnUntilFull();

        return committed;
    }
}
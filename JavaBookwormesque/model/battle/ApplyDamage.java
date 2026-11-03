package model.battle;

import java.util.Set;

public class ApplyDamage {

    // Categories of Letters
    private static final Set<Character> EASY = Set.of('A','E','I','O','U','L','N','R','S','T','D','G');
    private static final Set<Character> MODERATE = Set.of('B','C','M','P','F','H','V','W','Y');
    private static final Set<Character> COMPLEX = Set.of('K','J','X','Q','Z');

    // Damage calculation based on the formula: Damage = Total Number of Letters + 1 ((1 * Number of Easy Letters) + (1.5 * Number of Moderate Letters) + (2 * Number of Complex Letters))
    public static double calculateDamage(String submittedLetters) {
        if (submittedLetters == null || submittedLetters.isEmpty()) {
            return 0;
        }

        int totalLetters = submittedLetters.length();
        int easyCount = 0, moderateCount = 0, complexCount = 0;

        for (char c : submittedLetters.toUpperCase().toCharArray()) {
            if (EASY.contains(c)) {
                easyCount++;
            }
            else if (MODERATE.contains(c)) {
                moderateCount++;
            }
            else if (COMPLEX.contains(c)) {
                complexCount++;
            }
        }

        // Ensures that damage accepts decimal, but truncates the decimal value upon return
        double rawDamage = totalLetters + ((1 * easyCount) + (1.5 * moderateCount) + (2 * complexCount));
        return (int) rawDamage;
    }
}

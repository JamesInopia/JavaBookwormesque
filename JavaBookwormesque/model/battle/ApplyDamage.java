package model.battle;

import java.util.Set;

public class ApplyDamage {

    // Categories of Letters
    private static final Set<Character> Point1 = Set.of('A','E','I','O','U','L','N','R','S','T');
    private static final Set<Character> Point2 = Set.of('D','G');
    private static final Set<Character> Point3 = Set.of('B','C','M','P');
    private static final Set<Character> Point4 = Set.of('F','H','V','W','Y');
    private static final Set<Character> Point5 = Set.of('K');
    private static final Set<Character> Point8 = Set.of('J','X');
    private static final Set<Character> Point10 = Set.of('Q','Z');

    // Damage calculation based on the
    public static double calculateDamage(String submittedLetters) {
        if (submittedLetters == null || submittedLetters.isEmpty()) {
            return 0;
        }

        int totalLetters = submittedLetters.length();
        int p1_Count = 0, p2_Count = 0, p3_Count = 0, p4_Count = 0, p5_Count = 0, p8_Count = 0, p10_Count = 0;

        for (char c : submittedLetters.toUpperCase().toCharArray()) {
            if (Point1.contains(c)) {
                p1_Count++;
            }
            else if (Point2.contains(c)) {
                p2_Count++;
            }
            else if (Point3.contains(c)) {
                p3_Count++;
            }
            else if (Point4.contains(c)) {
                p4_Count++;
            }
            else if (Point5.contains(c)) {
                p5_Count++;
            }
            else if (Point8.contains(c)) {
                p8_Count++;
            }
            else if (Point10.contains(c)) {
                p10_Count++;
            }
        }

        // Ensures that damage accepts decimal, but truncates the decimal value upon return
        double rawDamage = totalLetters + ((1 * p1_Count) + (1.5 * p2_Count) + (2 * p3_Count) + (2.5 * p4_Count) + (3 * p5_Count) + (3.5 * p8_Count) + (4 * p10_Count));
        return (int) rawDamage;
    }
}

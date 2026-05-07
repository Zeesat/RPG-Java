package fantasyrpg.util;

public final class ConsoleFormatter {
    private ConsoleFormatter() {
    }

    public static void printSection(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    public static void printRound(int round) {
        System.out.println();
        System.out.println("--- Round " + round + " ---");
    }
}

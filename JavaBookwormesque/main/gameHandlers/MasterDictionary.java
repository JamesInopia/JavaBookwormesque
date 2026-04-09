package main.gameHandlers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public final class MasterDictionary {
    private static final String EXTERNAL_PATH = "data/wordList/masterWordList.txt";
    private static final String CLASSPATH_RESOURCE = "/data/wordList/masterWordList.txt";
    private static volatile Set<String> masterSet = Collections.emptySet();
    private static volatile Map<Integer, List<String>> byLength = Collections.emptyMap();
    private static volatile boolean ready = false;

    public interface ReadyListener { void onReady(); }
    private static final List<ReadyListener> listeners = new CopyOnWriteArrayList<>();

    // Loads masterWordList
    public static synchronized void loadAsync() {
        if (ready) return;

        Runnable loaderTask = () -> {
            Set<String> set = new HashSet<>(300_000);
            Map<Integer, List<String>> map = new HashMap<>();

            try (InputStream in = openMasterStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    set.add(line);
                    map.computeIfAbsent(line.length(), k -> new ArrayList<>()).add(line);
                }
            } catch (FileNotFoundException fnf) {
                System.err.println("MasterDictionary: master list not found: " + fnf.getMessage());
            } catch (Exception ex) {
                System.err.println("MasterDictionary load error: " + ex.getMessage());
            }

            masterSet = Collections.unmodifiableSet(set);
            Map<Integer, List<String>> imm = new HashMap<>();
            for (Map.Entry<Integer, List<String>> e : map.entrySet()) {
                imm.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
            byLength = Collections.unmodifiableMap(imm);
            ready = true;

            // Notify listeners once loading is complete
            for (ReadyListener l : listeners) {
                try { l.onReady(); } catch (Exception ignore) {}
            }
        };

        Thread loaderThread = new Thread(loaderTask, "DictionaryLoaderThread");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    // Opens masterWordList
    private static InputStream openMasterStream() throws IOException {
        InputStream in = MasterDictionary.class.getResourceAsStream(CLASSPATH_RESOURCE);
        if (in != null) return in;
        File f = new File(EXTERNAL_PATH);
        if (f.exists()) return new FileInputStream(f);
        throw new FileNotFoundException("masterWordList not found on classpath or at " + EXTERNAL_PATH);
    }

    // Adds ready if the MasterDictionary has completely loaded
    public static void addReadyListener(ReadyListener l) {
        if (ready) { l.onReady(); return; }
        listeners.add(l);
    }

    // Getters
    public static boolean isReady() { return ready; }

    public static boolean contains(String word) {
        return ready && masterSet.contains(word);
    }

    public static String getRandomWord(int length) {
        if (!ready) return "";
        List<String> list = byLength.get(length);
        if (list == null || list.isEmpty()) return "";
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}

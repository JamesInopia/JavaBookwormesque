package model.word;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public final class MasterDictionary {
    private static final String EXTERNAL_PATH = "public/wordList/masterWordList.txt";
    private static final String CLASSPATH_RESOURCE = "/public/wordList/masterWordList.txt";
    private static final String GENERATE_EXTERNAL_PATH = "public/wordList/generateWordList.txt";
    private static final String GENERATE_CLASSPATH_RESOURCE = "/public/wordList/generateWordList.txt";

    // masterSet (from masterWordList) is only used to check which words are correct, byLength (from generateWordList) is only used to generate the words for the letter grid
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
                }
            } catch (FileNotFoundException fnf) {
                System.err.println("MasterDictionary: master list not found: " + fnf.getMessage());
            } catch (Exception ex) {
                System.err.println("MasterDictionary load error: " + ex.getMessage());
            }

            // generateWordList is only read, never written to
            int generated = 0, notInMaster = 0;
            try (InputStream in = openGenerateStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    map.computeIfAbsent(line.length(), k -> new ArrayList<>()).add(line);
                    generated++;
                    if (!set.contains(line)) notInMaster++;
                }
            } catch (FileNotFoundException fnf) {
                System.err.println("MasterDictionary: generate list not found: " + fnf.getMessage());
            } catch (Exception ex) {
                System.err.println("MasterDictionary generate list load error: " + ex.getMessage());
            }

            if (generated == 0) {
                System.err.println("MasterDictionary: generateWordList has no words, the letter grid will use random letters until it does.");
            } else if (!set.isEmpty() && notInMaster > 0) {
                System.err.println("MasterDictionary: " + notInMaster + " of " + generated
                        + " generateWordList words are not in masterWordList, so they can't be submitted as correct words.");
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
        return openWordList(CLASSPATH_RESOURCE, EXTERNAL_PATH, "masterWordList");
    }

    // Opens generateWordList
    private static InputStream openGenerateStream() throws IOException {
        return openWordList(GENERATE_CLASSPATH_RESOURCE, GENERATE_EXTERNAL_PATH, "generateWordList");
    }

    // Opens a word list from the classpath, or from its file path if it isn't on the classpath
    private static InputStream openWordList(String classpathResource, String externalPath, String name) throws IOException {
        InputStream in = MasterDictionary.class.getResourceAsStream(classpathResource);
        if (in != null) return in;
        File f = new File(externalPath);
        if (f.exists()) return new FileInputStream(f);
        throw new FileNotFoundException(name + " not found on classpath or at " + externalPath);
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
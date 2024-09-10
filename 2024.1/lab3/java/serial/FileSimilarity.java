import java.io.*;
import java.util.*;

public class FileSimilarity {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();

        Map<String, Thread> threads = new HashMap<>();

        // Calculate the fingerprint for each file
        for (String path : args) {
            Thread thread = Thread.ofVirtual().unstarted(new FileSum(path, fileFingerprints));
            threads.put(path, thread);
            thread.start();
        }

        for (String path : args){
            threads.get(path).join();
        }

        Map<String, Thread> similarityThreads = new HashMap<>();


        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            for (int j = i + 1; j < args.length; j++) {
                String file1 = args[i];
                String file2 = args[j];
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                Thread threadSimilarity = Thread.ofVirtual().start(new Similarity(file1, file2, fingerprint1, fingerprint2));              
                similarityThreads.put(file1 + "-" + file2, threadSimilarity);

            }
        }

        // Wait for all similarity threads to finish
        for (Thread thread : similarityThreads.values()) {
            thread.join();
        }
    }

    public static class FileSum implements Runnable {

        private final String path;
        private Map<String, List<Long>> fileFingerprints = new HashMap<>();


        public FileSum(String path, Map<String, List<Long>> fileFingerprints){
            this.fileFingerprints = fileFingerprints;
            this.path = path;
        }

        @Override
        public void run() {
            try {
                List<Long> fingerprint = fileSum(this.path);
                fileFingerprints.put(path, fingerprint);
            } catch (IOException e) {
            }
        }

        private static List<Long> fileSum(String filePath) throws IOException {
            File file = new File(filePath);
            List<Long> chunks = new ArrayList<>();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[100];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    long sum = sum(buffer, bytesRead);
                    chunks.add(sum);
                }
            }
            return chunks;
        }
    
        private static long sum(byte[] buffer, int length) {
            long sum = 0;
            for (int i = 0; i < length; i++) {
                sum += Byte.toUnsignedInt(buffer[i]);
            }
            return sum;
        }
    }

    public static class Similarity implements Runnable {

        private final String file1;
        private final String file2;
        private final List<Long> base;
        private final List<Long> target;

        public Similarity(String file1, String file2, List<Long> base, List<Long> target){
            this.base = base;
            this.target = target;
            this.file1 = file1;
            this.file2 = file2;
        }

        @Override
        public void run() {
            try {
                float similarityScore = similarity(base, target);
                System.out.println("Similarity between " + this.file1 + " and " + this.file2 + ": " + (similarityScore * 100) + "%");
            } catch (IOException e) {
            }
        }

        private static float similarity(List<Long> base, List<Long> target) throws IOException{
    
            int counter = 0;
            List<Long> targetCopy = new ArrayList<>(target);
    
            for (Long value : base) {
                if (targetCopy.contains(value)) {
                    counter++;
                    targetCopy.remove(value);
                }
            }
    
            return (float) counter / base.size();
        }
    }
}
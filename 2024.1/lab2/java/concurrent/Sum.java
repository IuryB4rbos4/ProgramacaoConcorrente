import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.*;
import java.util.Arrays;

public class Sum {

    public static int sum(FileInputStream fis) throws IOException {
        
	int byteRead;
        int sum = 0;
        
        while ((byteRead = fis.read()) != -1) {
        	sum += byteRead;
        }

        return sum;
    }

    public static long sum(String path) throws IOException {

        Path filePath = Paths.get(path);
        if (Files.isRegularFile(filePath)) {
       	    FileInputStream fis = new FileInputStream(filePath.toString());
            return sum(fis);
        } else {
            throw new RuntimeException("Non-regular file: " + path);
        }
    }

    public static void main(String[] args) throws Exception {

        var numThreads = 4;
        Long[] res = new Long[numThreads];
        int count = 25;
        Thread[] threads = new Thread[numThreads];
        

        for(int i = 0; i < numThreads; i++) {
            int[] size = new int[]{i * count, i * count + count};
            Task t = new Sum(). new Task(res, i, size);
            threads[i] = t;
            threads[i].start();
        }

        for(Thread t : threads) {
            t.join();
        }

        System.out.println(res);
    
        Long ccount = 0L;
        for(Long value : res){
            ccount += value;
        }

        System.out.println(ccount);

    }


    class Task extends Thread {
        private Long[] res;
        private int thread_num;
        private int[] size;

        public Task(Long[] res, int thread_num, int[] size) {
            this.res = res;
            this.thread_num = thread_num;
            this.size = size;
        }
        
        @Override
        public void run(){
            try {
                res[this.thread_num] += sum_thread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int sum_thread() throws IOException{
            int _sum = 0;

            for(int i = size[0]; i < size[1]; i++){
                _sum += sum("../../dataset/" + "file." + (i + 1));
            }


            return _sum;
        }
    }


}

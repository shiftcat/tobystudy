package pe.sample.ch08;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest
{

    static AtomicInteger idx = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/emitter";

        StopWatch main = new StopWatch();
        main.start();

        for(int i = 0; i<100; i++ ) {
            es.execute(() -> {
                int cnt = idx.addAndGet(1);
                log.debug("Thread {}", cnt);

                StopWatch sw = new StopWatch();
                sw.start();
                rt.getForObject(url, String.class);
                sw.stop();
                log.debug("Elapsed {} {}", cnt, sw.getTotalTimeSeconds());
            });
        }

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);
        main.stop();
        log.debug("Total {}", main.getTotalTimeSeconds());
    }
}

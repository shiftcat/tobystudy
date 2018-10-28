package pe.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest
{

    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8082/rest?idx=";

        // 스레드 동기화
        CyclicBarrier barrier = new CyclicBarrier(101);

        for(int i = 0; i<100; i++ ) {
            es.submit(() -> {
                int idx = counter.addAndGet(1);

                barrier.await(); // 위 생성자 인자값인 100개의 스레드가 생성될때까지 스레드 대기.

                log.debug("Thread {}", idx);

                StopWatch sw = new StopWatch();
                sw.start();
                String res = rt.getForObject(url+idx, String.class, idx);
                sw.stop();
                log.debug("Elapsed {} {} / {}", idx, sw.getTotalTimeSeconds(), res);
                return null;
            });
        }

        barrier.await(); // 101개 스레드. 정해진 숫자 만큼 스레드가 차면 해당 모든 스레드를 동시에 실행.

        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);
        main.stop();
        log.debug("Total {}", main.getTotalTimeSeconds());
    }
}

package pe.sample.ch08;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class FutureEx1
{

    static String havyProc() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        log.debug("Hello");
        return "Hello";
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        FutureTask<String> ft = new FutureTask<String>(() -> {
            return havyProc();
        });

        es.execute(ft);

        log.debug("is down : {}", ft.isDone());
        log.debug(ft.get());
        log.debug("is down : {}", ft.isDone());
        log.debug("Exit");
        es.shutdown();
    }

}

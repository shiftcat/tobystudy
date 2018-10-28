package pe.sample.ch08;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;


@Slf4j
public class FutureEx
{

    static String havyProc() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        log.debug("Hello");
        return "Hello";
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        /*
        submit 메서드에는 Callable 과 Runnable 을 인자로 하는 두 가지 메서드가 있다.
        람다식 ()->{} 으로 하면 둘 중 어느 것이 호출될까?
        람다식 ()->{} 을 Callable 으로 강제 형변환 하여 Callable을 인자로 하는 submit이 호출 되도록 하였다.
         */
        Future<String> f = es.submit((Callable<String>) () -> {
            return havyProc();
        });

        log.debug("is down : {}", f.isDone());
        log.debug(f.get());
        log.debug("is down : {}", f.isDone());
        log.debug("Exit");
    }
}

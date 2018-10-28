package pe.sample.ch07;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxScEx
{

    public static void main(String[] args) throws InterruptedException {
        Flux.range(1, 10)
                .subscribeOn(Schedulers.newSingle("pub"))
                .log()
                .subscribeOn(Schedulers.newSingle("sub"))
                .subscribe(System.out::println);
        // 내부적으로 스레드풀을 생성하여 관리 하므로 메인 스레드가 죽지 않고 계속 실행 중 임.

        Flux.interval(Duration.ofMillis(200)).take(10).subscribe(s->log.debug("onNext:{}", s));
        TimeUnit.SECONDS.sleep(5);
    }
}

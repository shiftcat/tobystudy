package pe.sample.ch13;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
@EnableAsync
public class SpringWebCH13App
{


    @Service
    public static class MyService
    {
        public String work(String msg)
        {
            log.debug("{}/work",msg);
            return msg + "/work";
        }

        /*
        매 요청마다 SimpleAsyncTaskExecutor 스레드를 생성하여 처리하므로 절대 비추. 샘플코드 이므로...
         */
        @Async
        public CompletableFuture<String> asyncWork(String msg)
        {
            log.debug("{}/asyncWork",msg);
            return CompletableFuture.completedFuture(msg + "/asyncWork");
        }
    }


    @RestController
    public static class Controller
    {

        @Autowired
        MyService service;

        @GetMapping("/hello")
        public Mono<String> hello()
        {
            // 로그가 찍히는 순서 확인
            log.debug("pos1");
            Mono m = Mono.just("Hello").doOnNext(c -> log.debug("doOnNext: {}", c)).log();
            log.debug("pos2");
            return m;
        }



        @GetMapping("/hello2")
        public Mono<String> hello2()
        {
            // 로그가 찍히는 순서 확인
            log.debug("pos1");
            Mono<String> m = Mono.just("Hello").doOnNext(c -> log.debug("doOnNext: {}", c)).log();
            // 가능한 아래 메서드는 사용지 말것. (subscribe(), block())
//            m.subscribe();
            String msg = m.block();
            log.debug("pos2: {}", msg);
            return m;
        }



        @GetMapping("/rest1")
        public Mono<String> rest1()
        {
            // 로그가 찍히는 순서 확인
            log.debug("pos1");
            Mono m = Mono.just(service.work("hello")).doOnNext(c -> log.debug("doOnNext: {}", c)).log();
            log.debug("pos2");
            return m;
        }

        @GetMapping("/rest2")
        public Mono<String> rest2()
        {
            // 로그가 찍히는 순서 확인
            log.debug("pos1");
            // fromSupplier() 블록킹 메서드를 비동기적으로 실행.
            Mono m = Mono.fromSupplier(() -> service.work("hello")).doOnNext(c -> log.debug("doOnNext: {}", c)).log();
            log.debug("pos2");
            return m;
        }

        @GetMapping("/rest3")
        public Mono<String> rest3()
        {
            // 로그가 찍히는 순서 확인
            log.debug("pos1");
            Mono m = Mono.fromCompletionStage(service.asyncWork("hello")).doOnNext(c -> log.debug("doOnNext: {}", c)).log();
            log.debug("pos2");
            return m;
        }

    }


    public static void main(String[] args)
    {
        System.setProperty("server.port", "8082");
        SpringApplication.run(SpringWebCH13App.class);
    }

}

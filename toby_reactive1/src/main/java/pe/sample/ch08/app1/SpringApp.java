package pe.sample.ch08.app1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

@Slf4j
@SpringBootApplication
@EnableAsync
public class SpringApp
{

    @Component("MyService")
    public static class MyService {
        /*
        @Async 은 SimpleAsyncTaskExecutor 을 기본으로 사용한다.
        SimpleAsyncTaskExecutor 는 스레드들 호출할 때마다 새로운 스레드를 생성한다. 따라서 사용에 주의를 필요로 한다.
        @Async("MyTaskExecuter") 처럼 사용자 정의 Executor를 만들어 사용한다.
         */
        @Async
        public Future<String> hello() throws InterruptedException {
            log.info("Hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("Hello");
        }


        @Async("MyThreadExecutor")
        public ListenableFuture<String> listenHello() throws InterruptedException {
            log.info("Hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("Hello");
        }
    }

    @Bean("MyThreadExecutor")
    ThreadPoolTaskExecutor tp()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 기본 풀 사이즈 - 첫 스레드 요청이 들어오는 시점에 생성 -> 런타임중에 변경 가능
        executor.setMaxPoolSize(100); // 대기 큐 까지도 차면 풀 사이즈를 늘려 줌.. 디비 커넥션풀과 다름에 주의
        executor.setQueueCapacity(200); // 기본 풀 사이즈에 도달하면 대기 큐에 대기...
        executor.setThreadNamePrefix("my-thread");
        // executor.setTaskDecorator(); 활용방법 찾아 보자.
        executor.initialize();
        return executor;
    }


    public static void main(String[] args)
    {
        try(ConfigurableApplicationContext c = SpringApplication.run(SpringApp.class, args)) {

        }
    }


    @Autowired
    @Qualifier("MyService")
    MyService myService;

    // 모든 빈들이 준비 완료 시점에 호출
    @Bean
    ApplicationRunner run() {
        return args -> {
            log.debug("====> run");
            Future<String> f = myService.hello();
            log.debug("exit {}", f.isDone());
            log.debug("result : {}", f.get());


            ListenableFuture<String> lf = myService.listenHello();
            lf.addCallback(s -> log.debug("listen {}", s), e-> log.error(e.getMessage()));
            log.debug("exit");
        };
    }
}

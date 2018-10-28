package pe.sample.ch12;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
@EnableAsync
public class SpringWebCH12App
{

//    @Bean
//    TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory()
//    {
//        return new TomcatReactiveWebServerFactory();
//    }

//    @Bean
//    NettyReactiveWebServerFactory nettyReactiveWebServerFactory()
//    {
//        return new NettyReactiveWebServerFactory();
//    }


    @Service
    public static class MyService
    {
        // database 에 데이터를 저장한다는 가정.
        private void saveDb(String val)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("process data {}", val);
        }

        @Async
        public CompletableFuture<String> work(String req)
        {
            saveDb(req);
            return CompletableFuture.completedFuture(req + " /asyncwork");
        }
    }


    @RestController
    public static class AsyncController
    {

        @Autowired
        MyService service;

        static final String URL1 = "http://localhost:8081/service1?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";

        WebClient client = WebClient.create();

        @GetMapping("/hello")
        public Mono<String> hello()
        {
            return Mono.just("Hello");
        }


        @GetMapping("/rest")
        public Mono<String> rest(int idx)
        {
            Mono<String> res = client.get().uri(URL1, idx).exchange()
                    .flatMap(c -> c.bodyToMono(String.class))
                    .doOnNext(c -> log.debug("doOnNext1 {}", c))
                    .flatMap(res1 -> client.get().uri(URL2, res1).exchange())
                    .flatMap((c -> c.bodyToMono(String.class)))
                    .doOnNext(c -> log.debug("doOnNext2 {}", c))
                    .flatMap(res2 -> Mono.fromCompletionStage(service.work(res2)))
                    .doOnNext(c -> log.debug("doOnNext3 {}", c));
            return res;
        }


    }



    public static void main(String[] args)
    {
        System.setProperty("server.port", "8082");
        System.setProperty("reactor.ipc.netty.workerCount", "2");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
//        System.setProperty("server.tomcat.max-threads", "1");
        SpringApplication.run(SpringWebCH12App.class, args);
    }
}

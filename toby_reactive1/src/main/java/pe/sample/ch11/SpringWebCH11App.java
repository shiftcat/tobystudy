package pe.sample.ch11;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
public class SpringWebCH11App
{

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

        public String work(String req)
        {
            saveDb(req);
            return req + " /asyncwork";
        }
    }


    @RestController
    public static class AsyncController
    {

        <T>CompletableFuture<T> toCF(ListenableFuture<T> lf)
        {
            CompletableFuture<T> cf = new CompletableFuture<>();
            lf.addCallback(s -> {cf.complete(s);}, e-> {cf.completeExceptionally(e);});
            return cf;
        }

        // 정해진 숫자 만큼 스레드를 만들어 처리 하지만 성능은 동일 함.
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        @Autowired
        MyService service;

        static final String URL1 = "http://localhost:8081/service1?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";


        @GetMapping("/rest")
        public DeferredResult<String> rest(Integer idx)
        {
            DeferredResult<String> dr = new DeferredResult<String>();

            log.debug("/rest {}", idx);

            toCF(rt.getForEntity(URL1, String.class, "hello" +idx))
                    .thenCompose(s -> toCF(rt.getForEntity(URL2, String.class, s.getBody())))
                    // 블록킹 메서드를 비동기적으로 실행
                    .thenApplyAsync(s2 -> service.work(s2.getBody()))
                    .thenAccept(s3 -> dr.setResult(s3))
                    .exceptionally(e -> {
                        dr.setErrorResult(e.getMessage());
                        return (Void) null;
                    });
            return dr;
        }
    }



    public static void main(String[] args)
    {
        System.setProperty("server.port", "8082");
        System.setProperty("server.tomcat.max-threads", "1");
        SpringApplication.run(SpringWebCH11App.class, args);
    }
}

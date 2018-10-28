package pe.sample.ch09;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@SpringBootApplication
public class SpringWebCH9App
{

//    @RestController
    public static class Controller
    {
        RestTemplate rt = new RestTemplate();

        @GetMapping("/rest")
        public String rest(Integer idx)
        {
            log.debug("/rest {}", idx);
            String res = rt.getForObject("http://localhost:8081/service?req={req}", String.class, "hello" +idx);
            log.debug("/rest service res => {}", res);
            return "rest " + idx;
        }
    }

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
        public ListenableFuture<String> work(String req)
        {
            saveDb(req);
            return new AsyncResult<>(req + " /asyncwork");
        }
    }


    @RestController
    public static class AsyncController
    {
        // 백그라운드에 스레드를 별도로 반들어 처리 함.
        // AsyncRestTemplate rt = new AsyncRestTemplate();

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
            ListenableFuture<ResponseEntity<String>> res1 = rt.getForEntity(URL1, String.class, "hello" +idx);
            res1.addCallback(
                    s -> {
                        String data = s.getBody();
                        log.debug("/rest service1 => {}", data);
                        ListenableFuture<ResponseEntity<String>> res2 = rt.getForEntity(URL2, String.class, data + idx);
                        res2.addCallback(
                                s2 -> {
                            String data2 = s2.getBody();
                            log.debug("/rest service2 => {}", data2);
                            ListenableFuture<String> res3 = service.work(data2);
                            res3.addCallback(
                                    s3 -> {
                                log.debug("/myservice work => {}", s3);
                                dr.setResult(s3 + " /work");
                            }, e3 -> {
                                dr.setErrorResult(e3);
                            });
                        }, e2 -> {
                            dr.setErrorResult(e2);
                        });
                    }
                    ,
                    e -> {
                        dr.setErrorResult(e);
                    }
            );
            return dr;
        }
    }



    public static void main(String[] args)
    {
        System.setProperty("server.port", "8082");
        System.setProperty("server.tomcat.max-threads", "1");
        SpringApplication.run(SpringWebCH9App.class, args);
    }
}

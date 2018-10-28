package pe.sample.ch08.app2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

@Slf4j
//@Configuration
//@EnableWebMvc
@SpringBootApplication
@EnableAsync
public class SpringWebApp
{
    @RestController
    public static class MyController
    {


        @GetMapping("/hello")
        public String hello() throws InterruptedException
        {
            log.debug("async");
            Thread.sleep(2000);
            return "Hello";
        }


        @GetMapping("/callable")
        public Callable callable() throws InterruptedException
        {
            log.debug("callable");
            return () -> {
                log.debug("async");
                Thread.sleep(2000);
                return "Hello";
            };
        }


        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/dr")
        public DeferredResult<String> defrred() throws InterruptedException
        {
            log.debug("deferred");
            DeferredResult<String> dr = new DeferredResult<String>(600000l);
            results.add(dr);
            return dr;
        }


        @GetMapping("/dr/count")
        public String drcount()
        {
            return String.valueOf(results.size());
        }


        @GetMapping("dr/event")
        public String drevent(String msg)
        {
            for(DeferredResult<String> dr: results) {
                dr.setResult("Hello " + msg);
                results.remove(dr);
            }
            return "OK";
        }


        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() throws Exception
        {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for(int i=1; i<=50; i++) {
                        emitter.send("<p>Stream "+ i + "</p>");
                        Thread.sleep(100);
                    }
                }
                catch (Exception ex) {}
            });

            return emitter;
        }

    }



    public static void main(String[] args)
    {
        SpringApplication.run(SpringWebApp.class, args);
    }

}

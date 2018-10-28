package pe.sample.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
public class RemoteServer
{
    @RestController
    public static class Controller
    {
        @GetMapping("/service1")
        public String service1(String req) throws InterruptedException
        {
            log.debug("/service1 {}", req);
            Thread.sleep(740);
//            if(1==1) throw new RuntimeException();
            return req + "/service1";
        }


        @GetMapping("/service2")
        public String service2(String req) throws InterruptedException
        {
            log.debug("/service2 {}", req);
            Thread.sleep(650);
            return req + "/service2";
        }

    }



    public static void main(String[] args)
    {
        System.setProperty("server.port", "8081");
        System.setProperty("server.tomcat.max-threads", "1000");
        SpringApplication.run(RemoteServer.class, args);
    }

}

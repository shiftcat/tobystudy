package pe.sample.ch10;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
public class SpringWebCH10App
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

        @Async
        public ListenableFuture<String> work(String req)
        {
            saveDb(req);
            return new AsyncResult<>(req + " /asyncwork");
        }
    }



    public static class AcceptCompletion<S> extends Completion<S,Void>
    {
        private Consumer<S> con;

        public AcceptCompletion(Consumer<S> con)
        {
            this.con = con;
        }


        public void run(S value)
        {
            con.accept(value);
        }
    }

    public static class ErrorCompletion<T> extends Completion<T, T>
    {
        private Consumer<Throwable> econ;

        public ErrorCompletion(Consumer<Throwable> econ)
        {
            this.econ = econ;
        }


        public void run(T value)
        {
            if(next != null) {
                next.run(value);
            }
        }

        @Override
        void error(Throwable t)
        {
            econ.accept(t);
        }
    }

    public static class ApplyCompletion<S,T> extends Completion<S,T>
    {
        private Function<S, ListenableFuture<T>> fn;


        public ApplyCompletion(Function<S, ListenableFuture<T>> fn)
        {
            this.fn = fn;
        }


        public void run(S value)
        {
            ListenableFuture<T> lf = fn.apply(value);
            lf.addCallback(s->complete(s), e->error(e));
        }
    }



    public static class Completion<S,T>
    {
        Completion next;


        private Completion()
        {
        }


        public static <S, T> Completion<S, T> from(ListenableFuture<T> lf)
        {
            Completion<S, T> c = new Completion<>();
            lf.addCallback(
                    s -> {
                        c.complete(s);
                    },
                    e -> {
                        c.error(e);
                    });
            return c;
        }

        void complete(T s)
        {
            if(next != null) {
                next.run(s);
            }
        }

        void error(Throwable t)
        {
            if(next != null) {
                next.error(t);
            }
        }


        public <V> Completion<T,V> andApply(Function<T, ListenableFuture<V>> fn)
        {
            Completion<T,V> c = new ApplyCompletion<>(fn);
            this.next = c;
            return c;
        }

        public void andAccept(Consumer<T> con)
        {
            Completion<T, Void> c = new AcceptCompletion<>(con);
            this.next = c;
        }


        public Completion<T,T> andError(Consumer<Throwable> econ)
        {
            Completion<T, T> c = new ErrorCompletion(econ);
            this.next = c;
            return c;
        }


        public void run(S value){}
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

            Completion
                    .from(rt.getForEntity(URL1, String.class, "hello" +idx))
                    .andApply(s->rt.getForEntity(URL2, String.class, s.getBody()))
                    .andApply(s->service.work(s.getBody()))
                    .andError(e->dr.setErrorResult(e.toString()))
                    .andAccept(s->dr.setResult(s));
            return dr;
        }
    }



    public static void main(String[] args)
    {
        System.setProperty("server.port", "8082");
        System.setProperty("server.tomcat.max-threads", "1");
        SpringApplication.run(SpringWebCH10App.class, args);
    }
}

package pe.sample.ch07;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class IntervalEx
{

    public static void main(String[] args)
    {

        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                int no = 0;
                ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                @Override
                public void request(long n) {
                    exec.scheduleAtFixedRate(() -> {
                        sub.onNext(no++);
                    }, 0, 300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    exec.shutdown();
                }
            });
        };


        Publisher<Integer> takePub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                int count = 0;
                Subscription sc;
                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                    this.sc = s;
                }

                @Override
                public void onNext(Integer integer) {
                    sub.onNext(integer);
                    if(++count > 9) {
                        sc.cancel();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    sub.onError(t);
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                }
            });
        };


        takePub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.debug("onNext: {}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.error("onError {}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });

    }


}

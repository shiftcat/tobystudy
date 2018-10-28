package pe.sample.ch05;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.*;

// JDK 9 이상 : JDK8 이라면 reactivestreams에도 동일한 API 존재 함.
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class PubSub1
{


    /*
    publisher <- observable
    subscriber <- observer
     */
    public static void main(String[] args) throws InterruptedException {
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService es = Executors.newSingleThreadExecutor();

        // 발행자
        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = iter.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        System.out.println(Thread.currentThread() + " OnRequest");
//                        Future<?> future = es.submit(() -> {
                            es.execute(() -> {
                            int idx = 0;
                            try {
                                while (idx++ < n) {
                                    if(it.hasNext()) {
                                        subscriber.onNext(it.next());
                                    }
                                    else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            }
                            catch (RuntimeException ex) {
                                subscriber.onError(ex);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        // 구독자
        Subscriber<Integer> s = new Subscriber<Integer>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                System.out.println(Thread.currentThread() + " OnSubscribe");
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread() + " onNext " + item);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(Thread.currentThread() + " OnError " + throwable);
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread() + " onComplete ");
            }
        };

        p.subscribe(s);
        es.awaitTermination(10, TimeUnit.SECONDS);
        es.shutdown();
    }
}

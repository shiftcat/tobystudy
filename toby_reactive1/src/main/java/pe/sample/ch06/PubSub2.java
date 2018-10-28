package pe.sample.ch06;


import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/*
reactive streams - operators

Publisher -> data1 --> op1 --> data2 --> op2 --> data3 -> Subscriber

1. map (d1 -> f -> d2)

*/


@Slf4j
public class PubSub2
{
//    private static final Logger log = LoggerFactory.getLogger(PubSub2.class);

    public static void main(String[] args)
    {
        System.out.print("Application start");

        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
        Publisher<Integer> mPub = mapPub(pub, s -> s * 10);
        Publisher<Integer> mPub2 = mapPub(mPub, s -> -s);
        mPub2.subscribe(logSub());

        Publisher<Integer> sumPub = sumPub(pub);
        sumPub.subscribe(logSub());

        /*
        1,2,3,4,5
        0 -> (0,1) -> 0 + 1 = 1
        1 -> (1,2) -> 1 + 2 = 3
        3 -> (3,3) -> 3 + 3 = 6
        6 -> (6,4) -> 6 + 4 = 10
        .
        .
        .
         */
        Publisher<Integer> reducePub = reducePub(pub, 0, (BiFunction<Integer, Integer, Integer>)(a, b) -> a+b);
        reducePub.subscribe(logSub());

        System.out.print("Application end");
    }


    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bf)
    {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub<Integer, Integer>(sub) {
                    int result = init;

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        result = bf.apply(result, integer);
                    }
                });
            }
        };
    }



    private static Publisher<Integer> sumPub(Publisher<Integer> pub)
    {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {

                pub.subscribe(new DelegateSub<Integer, Integer>(sub) {
                    int sum = 0;

                    @Override
                    public void onComplete() {
                        sub.onNext(sum);
                        sub.onComplete();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        sum += integer;
                    }
                });
            }
        };
    }



    private static Publisher<Integer> iterPub(List<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(i->sub.onNext(i));
                            sub.onComplete();
                        }
                        catch (RuntimeException ex) {
                            sub.onError(ex);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }



    private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f)
    {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T, R>(sub){
                    @Override
                    public void onNext(T item) {
                        sub.onNext(f.apply(item));
                    }
                });
            }
        };
    }


    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onsubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T integer) {
                log.debug("onNext: {}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.error("onError: {}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };
    }


}

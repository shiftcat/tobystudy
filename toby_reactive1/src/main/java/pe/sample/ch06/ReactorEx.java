package pe.sample.ch06;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

public class ReactorEx
{

    public static void main(String[] args)
    {
        Flux.<Integer>create(e -> {
            e.next(1);
            e.next(2);
            e.next(3);
            e.complete();
        })
                .log().map(s->s+2).log()
                .subscribe(System.out::println);


        Flux.create(new Consumer<FluxSink<Integer>>() {
            @Override
            public void accept(FluxSink<Integer> fluxSink) {
                fluxSink.next(1);
                fluxSink.next(2);
                fluxSink.next(3);
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                System.out.print(o);
            }
        });
    }
}

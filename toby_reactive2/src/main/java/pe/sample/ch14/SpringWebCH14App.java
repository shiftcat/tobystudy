package pe.sample.ch14;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class SpringWebCH14App
{

    @Data @AllArgsConstructor
    public static class Event
    {
        long id;
        String value;
    }



    @RestController
    public static class Controller
    {
        @GetMapping("/event/{id}")
        public Mono<Event> event(@PathVariable long id)
        {
            return Mono.just(new Event(id, "event" + id));
        }


        @GetMapping("/event/list")
        public Mono<List<Event>> eventList()
        {
            List<Event> events = Arrays.asList(new Event(1, "event1"), new Event(2, "event2"));
            return Mono.just(events);
        }

        /*
        response //
        [{"id":1,"value":"event1"},{"id":2,"value":"event2"}]
         */
        @GetMapping("/events")
        public Flux<Event> events()
        {
            List<Event> events = Arrays.asList(new Event(1, "event1"), new Event(2, "event2"));
            return Flux.fromIterable(events);
//            return Flux.just(new Event(1, "event1"), new Event(2, "event2"));
        }


        /*
        response //
        data:{"id":1,"value":"event1"}
        data:{"id":2,"value":"event2"}
         */
        @GetMapping(value = "/eventstream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<Event> eventstream()
        {
            List<Event> events = Arrays.asList(new Event(1, "event1"), new Event(2, "event2"));
            return Flux.fromIterable(events);
        }

        // server sent event
        @GetMapping(value = "/eventstream2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<Event> eventstream2()
        {
            Stream<Event> es = Stream.generate(() -> new Event(System.currentTimeMillis(), "value"));
            return Flux.fromStream(es)
                    .delayElements(Duration.ofSeconds(1))
                    .take(5);
        }


        @GetMapping(value = "/eventstream3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<Event> eventstream3()
        {
            return Flux
//                    .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
                    .<Event, Long>generate(() -> 1l, (id, sink) -> {
                        sink.next(new Event(id, "event3 " + id));
                        return id+1;
                    })
                    .delayElements(Duration.ofSeconds(1))
                    .take(5);
        }


        @GetMapping(value = "/eventstream4", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
        public Flux<Event> eventstream4()
        {
            Flux<Event> es = Flux
                    .<Event, Long>generate(() -> 1l, (id, sink) -> {
                        sink.next(new Event(id, "event4 " + id));
                        return id+1;
                    });

            Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

            return Flux.zip(es, interval).map(tu -> tu.getT1()).take(5);
        }


        @GetMapping(value = "/eventstream5", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
        public Flux<Event> eventstream5()
        {
            Flux<String> es = Flux
                    .generate(sink->sink.next("event5 "));

            Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

            return Flux.zip(es, interval).map(tu -> new Event(tu.getT2(), tu.getT1())).take(5);
        }
    }


    public static void main(String[] args)
    {
        System.setProperty("server.port", "8082");
        SpringApplication.run(SpringWebCH14App.class);
    }

}

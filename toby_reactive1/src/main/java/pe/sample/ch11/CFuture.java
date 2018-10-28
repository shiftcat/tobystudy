package pe.sample.ch11;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;


@Slf4j
public class CFuture
{
    /*
    Future
    ListenableFuture
    CompletableFuture
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException
    {

        // ex1
//        CompletableFuture<Integer> f = CompletableFuture.completedFuture(1);
//        f.complete(2);
//        f.completeExceptionally(new RuntimeException());
//        System.out.println(f.get());


        // ex2
//        CompletableFuture.runAsync(() -> {log.debug("runAsync");})
//                .thenRun(() -> {log.debug("thenRun");})
//                .thenRun(() -> {log.debug("thenRun");})
//                .thenRun(() -> {log.debug("thenRun");});
//        log.debug("Exit");

        ExecutorService es = Executors.newFixedThreadPool(10);
        // ex3
        CompletableFuture
                .supplyAsync(() -> {
                    log.debug("supplyAsync");
                    return 1;
                })
                .thenCompose(s -> {
                    log.debug("thenApply {}", s);
//                    if(1==1) throw new RuntimeException();
                    return CompletableFuture.completedFuture(s + 1);
                })
                .thenApply(s2 -> {
                    log.debug("thenApply {}", s2);
                    return s2 * 2;
                })
                .thenApplyAsync(s2 -> {
                    log.debug("thenApplyAsync {}", s2);
                    return s2 * 3;
                }, es)
                .exceptionally(e -> {
                    return -10;
                })
                .thenAccept(s3 -> {
                    log.debug("thenAccept {}", s3);
                });
        log.debug("Exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}

package pe.sample.ch05;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ob3
{

    /*
    Observable : source -> event -> observer

     */

    static class IntObservable extends Observable implements Runnable {

        // publisher
        @Override
        public void run() {
            for(int i=1; i<=10; i++) {
                setChanged();
                notifyObservers(i);
            }
        }
    }

    /*
     1. 더 이상 데이터가 없음. 완료 상태가 없음.
     2. 에러... 예외처리
     */

    public static void main(String[] args)
    {
       // subscriber
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        IntObservable ibo = new IntObservable();
        ibo.addObserver(ob);
//        ibo.run();

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(ibo);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();
    }
}

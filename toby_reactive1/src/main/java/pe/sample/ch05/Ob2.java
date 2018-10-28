package pe.sample.ch05;

import java.util.Iterator;

public class Ob2
{

    public static void run(String[] args)
    {
        // iterator() implement
        Iterable<Integer> iter = () ->
            new Iterator<Integer>() {
                int i = 0;
                final static int MAX = 10;
                @Override
                public boolean hasNext() {
                    return i < MAX;
                }

                @Override
                public Integer next() {
                    return ++i;
                }
            };

        for(Integer i : iter) {
            System.out.println(i);
        }

        for(Iterator<Integer> it = iter.iterator(); it.hasNext();) {
            System.out.println(it.next());
        }

    }

}

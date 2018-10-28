package pe.sample.ch05;

import java.util.Arrays;

public class Ob1
{

    public static void run(String[] args)
    {
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);
        for(Integer i : iter) { // for-each
            System.out.println(i);
        }
    }

}

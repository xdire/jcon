package xdire.con.tests;

import xdire.con.ConcurrentLinkedSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentStructureTester {

    public static void main(String[] args) {

        ConcurrentStructureTester.testLinkedSet();

    }

    public static void testLinkedSet() {

        ConcurrentLinkedSet<String> set = new ConcurrentLinkedSet<String>();

        ExecutorService es = Executors.newFixedThreadPool(3);

        try {

            es.execute(() -> {

                set.remove("a");
                set.remove("zzz");
                set.remove("nnn");

                set.add("one");
                set.add("two");
                set.add("aaa");
                set.add("bbb");
                set.add("ccc");

                set.remove("a");
                set.remove("zzz");
                set.remove("nnn");

                set.add("test1");
                set.add("test2");
                set.add("xxx");
                set.add("yyy");

                set.remove("bbb");
                set.remove("test1");
                set.remove("two");

                System.out.println("Worker 1 finished: " + set.toString());

            });

            es.execute(() -> {

                set.add("ones");
                set.add("twos");
                set.add("aaas");
                set.add("bbbs");
                set.add("cccs");

                set.remove("a");
                set.remove("zzz");
                set.remove("nnn");

                set.add("test1");
                set.add("test2");
                set.add("xxx");
                set.add("yyy");

                set.remove("cccs");
                set.remove("test1");
                set.remove("twos");
                set.remove("test2");

                System.out.println("Worker 2 finished: " + set.toString());

            });

            es.execute(() -> {

                set.add("on");
                set.add("tw");
                set.add("aa");
                set.add("bb");
                set.add("cc");

                set.remove("zzz");
                set.remove("nnn");

                set.add("test1");
                set.add("test2");
                set.add("xxx");
                set.add("yyy");

                set.remove("cc");
                set.remove("test1");
                set.remove("tw");
                set.remove("test2");

                System.out.println("Worker 3 finished: " + set.toString());

            });

            es.awaitTermination(3000, TimeUnit.MILLISECONDS);

        } catch (Exception e) {

            e.printStackTrace();

        }

        es.shutdown();

    }

}

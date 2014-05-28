package com.fererlab.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * acm
 */
public class Collector {

    private ForkJoinPool dbForkJoinPool = new ForkJoinPool();
    private ForkJoinPool wsForkJoinPool = new ForkJoinPool();
    private ForkJoinPool httpForkJoinPool = new ForkJoinPool();
    private ForkJoinPool forkJoinPool = new ForkJoinPool();
    public long COLLECT_TIMEOUT_MILLIS = 10 * 60 * 1000;

    public List<Object> collect(final long timeoutMillis, Exec... execs) {
        final List<Object> objects = new ArrayList<Object>();
        final List<Callable<Object>> callableList = new ArrayList<Callable<Object>>();
        final List<Callable<Object>> dbCallableList = new ArrayList<Callable<Object>>();
        final List<Callable<Object>> wsCallableList = new ArrayList<Callable<Object>>();
        final List<Callable<Object>> httpCallableList = new ArrayList<Callable<Object>>();
        for (final Exec exec : execs) {
            if (exec instanceof DBExec) {
                dbCallableList.add(
                        new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                return exec.run();
                            }
                        });
            } else if (exec instanceof WSExec) {
                wsCallableList.add(
                        new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                return exec.run();
                            }
                        });
            } else if (exec instanceof HttpExec) {
                httpCallableList.add(
                        new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                return exec.run();
                            }
                        });
            } else {
                callableList.add(
                        new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                return exec.run();
                            }
                        });
            }
        }
        List<Callable<Object>> allCallables = new ArrayList<Callable<Object>>();

        Callable<Object> dbCallable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return dbForkJoinPool.invokeAll(dbCallableList);
            }
        };
        allCallables.add(dbCallable);

        Callable<Object> wsCallable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return wsForkJoinPool.invokeAll(wsCallableList);
            }
        };
        allCallables.add(wsCallable);

        Callable<Object> httpCallable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return httpForkJoinPool.invokeAll(httpCallableList);
            }
        };
        allCallables.add(httpCallable);

        Callable<Object> callable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return forkJoinPool.invokeAll(callableList);
            }
        };
        allCallables.add(callable);

        ForkJoinPool pool = new ForkJoinPool();
        try {
            List<Future<Object>> futures = pool.invokeAll(allCallables, timeoutMillis, TimeUnit.MILLISECONDS);
            for (Future<Object> future : futures) {
                try {
                    ArrayList fjtList = (ArrayList) future.get();
                    for (Object fjt : fjtList) {
                        ForkJoinTask objectForkJoinTask = (ForkJoinTask) fjt;
                        objects.add(objectForkJoinTask.get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return objects;
    }

}

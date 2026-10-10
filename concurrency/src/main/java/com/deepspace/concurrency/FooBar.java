package com.deepspace.concurrency;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

class FooBar {
    
    private static final Object dummy = new Object();
    private int n;
    private CyclicBarrier barrier;
    private Semaphore semaphore;
    private volatile boolean allowPrintFirst = true;
    
    public FooBar(int n) {
        this.n = n;
        this.barrier = new CyclicBarrier(2);
        this.semaphore = new Semaphore(1);
    }
    
    public void foo(Runnable printFoo) throws InterruptedException {
        Executors.newCachedThreadPool();
        for (int i = 0; i < n; i++) {
            synchronized (dummy) {
                if (allowPrintFirst) {
                    // printFoo.run() outputs "foo". Do not change or remove this line.
                    printFoo.run();
                    allowPrintFirst = false;
                }
            }
        }
    }
    
    public synchronized void bar(Runnable printBar) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            if (!allowPrintFirst) {
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();
                allowPrintFirst = true;
            }
        }
    }
}

package pl.piotrowski.pwir.projekt.zadanie1.zad_model;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Model extends java.util.Observable {

    private final BlockingQueue<BigInteger> queue;
    private BigInteger result;
    private boolean isFinished;
    private final int NUMBERS_QUANTITY = 1500;
    private ThreadA threadA;
    private ThreadB threadB;
    private boolean isWaiting = true;
    private long delay;
    private boolean isGeneratingLongs;
    private ReentrantLock lock;

    public Model() {
        this.result = new BigInteger("0");
        this.isFinished = false;
        threadA = new ThreadA();
        threadB = new ThreadB();
        delay = 150;
        queue = new LinkedBlockingQueue<>();
        lock = new ReentrantLock();
    }

    public void start() {
        setFinished(false);
        threadA = new ThreadA();
        threadB = new ThreadB();
        threadA.start();
        threadB.start();
    }

    public void reset() {
        result = new BigInteger("0");
        queue.clear();
        setFinished(false);
        setChanged();
        notifyObservers();
    }

    public void stop() {

        if (!threadA.isFinished() || !threadB.isFinished()) {
            threadA.interrupt();
            threadB.interrupt();
            synchronized (this) {
                try {
                    setWaiting(true);
                    this.wait();
                } catch (InterruptedException e) {
                    return;
                } finally {
                    setWaiting(false);
                }
            }
        }

        result = new BigInteger("0");
        queue.clear();
        setChanged();
        notifyObservers();

    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    private boolean isGeneratingLongs() {
        return isGeneratingLongs;
    }

    public void setGeneratingLongs(boolean generatingLongs) {
        isGeneratingLongs = generatingLongs;
    }

    private ReentrantLock getLock() {
        return lock;
    }

    public class ThreadA extends Thread {


        AtomicInteger count = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean isFinished = true;
        final Object syncObject = new Object();

        @Override
        public void run() {
            setFinished(false);
            try {
                sleep(50);
            } catch (InterruptedException e) {
                setFinished(true);
                Model.this.setFinished(true);
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
                return;
            }

            for (int i = 0; i < 2; i++) {
                executor.execute(new Thread(() -> {
                    Random randomGenerator=new Random();
                    for (int j = 1; j <= NUMBERS_QUANTITY / 2 && !isInterrupted(); j++) {

                        BigInteger bigNumber;

                        if (isGeneratingLongs()) {
                            bigNumber = new BigInteger(Long.toString(randomGenerator.nextLong()));
                        } else {
                            bigNumber = new BigInteger(Integer.toString(randomGenerator.nextInt()));
                        }

                        getLock().lock();
                        getQueue().add(bigNumber);
                        setChanged();
                        notifyObservers(bigNumber);
                        getLock().unlock();

                        try {
                            sleep(delay, 1);
                        } catch (InterruptedException e) {
                            break;
                        }

                    }

                    if (count.incrementAndGet() == 2) {
                        setFinished(true);
                        synchronized (threadA.syncObject) {
                            threadA.syncObject.notify();
                        }
                    }
                }));
            }

            try {
                synchronized (syncObject) {
                    syncObject.wait();
                }
            } catch (InterruptedException e) {
                Model.this.setFinished(true);
                executor.shutdownNow();
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
                setFinished(true);
                return;
            }

            setFinished(true);
            executor.shutdown();

        }

        boolean isFinished() {
            return isFinished;
        }

        void setFinished(boolean finished) {
            isFinished = finished;
        }
    }

    public class ThreadB extends Thread {

        boolean isFinished = true;

        @Override
        public void run() {
            setFinished(false);
            try {
                sleep(50);
            } catch (InterruptedException e) {
                setFinished(true);
                Model.this.setFinished(true);
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
                return;
            }

            for (int j = 1; j <= NUMBERS_QUANTITY && !isInterrupted(); j++) {
                try {
                    setResult(getResult().add(getQueue().take()));
                    setChanged();
                    notifyObservers(getResult().toString());
                } catch (InterruptedException e) {
                    setFinished(true);
                    Model.this.setFinished(true);
                    synchronized (Model.this) {
                        if (Model.this.isWaiting()) {
                            Model.this.notify();
                        }
                    }
                    return;
                }
            }

            Model.this.setFinished(true);
            setChanged();
            notifyObservers();
            this.setFinished(true);
            System.out.println("done2");
        }

        boolean isFinished() {
            return isFinished;
        }

        void setFinished(boolean finished) {
            isFinished = finished;
        }
    }

    private boolean isWaiting() {
        return isWaiting;
    }

    private void setWaiting(boolean waiting) {
        isWaiting = waiting;
    }

    private void setResult(BigInteger result) {
        this.result = result;
    }

    private BigInteger getResult() {
        return result;
    }

    private BlockingQueue<BigInteger> getQueue() {
        return queue;
    }

    public boolean isFinished() {
        return isFinished;
    }

    private void setFinished(boolean finished) {
        isFinished = finished;
    }
}



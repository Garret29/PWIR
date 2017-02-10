package pl.piotrowski.pwir.projekt.zadanie1.zad_model;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Model extends java.util.Observable {

    private final BlockingQueue<BigInteger> queue;
    private BigInteger result;
    private boolean isFinished;
    private final int NUMBERS_QUANTITY = 10000;
    private ThreadA threadA;
    private ThreadB threadB;
    private boolean isWaiting = false;
    private long delay;
    private boolean isGeneratingLongs;

    public Model() {
        this.result = new BigInteger("0");
        this.isFinished = false;
        threadA = new ThreadA();
        threadB = new ThreadB();
        delay = 150;
        queue = new LinkedBlockingQueue<>();
    }

    public void start() {
        threadA = new ThreadA();
        threadB = new ThreadB();
        threadA.start();
        threadB.start();
    }

    public void stop() {
        System.out.println("interrupting A");
        threadA.interrupt();
        System.out.println("interrupting B");
        threadB.interrupt();

    }

    public void reset() {
        if (!threadA.isFinished() || !threadB.isFinished()) {
            System.out.println("stop!");
            stop();
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
        setFinished(false);
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


    public class ThreadA extends Thread {

        Random randomGenerator;
        AtomicInteger count = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean isFinished = true;
        final Object syncObject = new Object();

        ThreadA() {
            this.randomGenerator = new Random();
        }

        @Override
        public void run() {
            setFinished(false);

            try {
                sleep(50);
            } catch (InterruptedException e) {
                setFinished(true);
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
                System.out.println("interrupted A");
                Model.this.setFinished(true);
                return;
            }

            for (int i = 0; i < 2; i++) {
                executor.execute(new Thread(() -> {
                    for (int j = 1; j <= NUMBERS_QUANTITY / 2 && !isInterrupted(); j++) {

                        BigInteger bigNumber;

                        if (isGeneratingLongs()) {
                            bigNumber = new BigInteger(Long.toString(randomGenerator.nextLong()));
                        } else {
                            bigNumber = new BigInteger(Integer.toString(randomGenerator.nextInt()));
                        }

                        getQueue().add(bigNumber);
                        setChanged();
                        notifyObservers(bigNumber);

                        try {
                            sleep(delay, 1);
                        } catch (InterruptedException e) {
                            System.out.println("interrupted A_minor");
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
                System.out.println("interrupted A");
                executor.shutdownNow();
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
                setFinished(true);
                Model.this.setFinished(true);
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
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
                System.out.println("interrupted B");
                Model.this.setFinished(true);
                return;
            }

            for (int j = 1; j <= NUMBERS_QUANTITY && !isInterrupted(); j++) {
                try {
                    setResult(getResult().add(getQueue().take()));
                    setChanged();
                    notifyObservers(getResult().toString());
                } catch (InterruptedException e) {
                    setFinished(true);
                    synchronized (Model.this) {
                        if (Model.this.isWaiting()) {
                            Model.this.notify();
                        }
                    }
                    System.out.println("interrupted B");
                    Model.this.setFinished(true);
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



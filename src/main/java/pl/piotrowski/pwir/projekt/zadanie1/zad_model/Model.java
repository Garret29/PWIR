package pl.piotrowski.pwir.projekt.zadanie1.zad_model;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Model extends java.util.Observable {


    private final BlockingQueue<BigInteger> queue = new LinkedBlockingQueue<>();
    private BigInteger result;
    private boolean isFinished;
    private final int NUMBERS_QUANTITY = 10000;
    private ThreadA threadA;
    private ThreadB threadB;
    private boolean isWaiting = false;


    public Model() {
        this.result = new BigInteger("0");
        this.isFinished = false;
        threadA = new ThreadA();
        threadB = new ThreadB();
    }

    public void start() {
        threadA = new ThreadA();
        threadB = new ThreadB();
        threadA.start();
        threadB.start();
    }

    public void reset() {
        //System.out.println("reset");
        if (!threadA.isFinished() && !threadB.isFinished()) {
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
        //System.out.println("reset2");
        result = new BigInteger("0");
        queue.clear();
        setFinished(false);
    }

    public class ThreadA extends Thread {

        Random randomGenerator;
        AtomicInteger count = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean isFinished = true;

        ThreadA() {
            this.randomGenerator = new Random();
        }

        @Override
        public void run() {
            setFinished(false);

            try {
                sleep(250);
            } catch (InterruptedException e) {
                setFinished(true);
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                executor.execute(new Thread(() -> {
                    for (int j = 1; j <= NUMBERS_QUANTITY / 2 && !isInterrupted(); j++) {
                        BigInteger bigNumber = new BigInteger(Long.toString(randomGenerator.nextLong()));
                        getQueue().add(bigNumber);
                        setChanged();
                        notifyObservers(bigNumber);

                        //Not necessary, made to make GUI update smoothly
                        try {
                            sleep(0, 1);
                        } catch (InterruptedException e) {
                            break;
                        }

                    }

                    if (count.incrementAndGet() == 2) {
                        setFinished(true);
                    }
                }));
            }
            if (this.isInterrupted()) {
                executor.shutdownNow();
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
            } else {
                executor.shutdown();
            }

            //System.out.println("done1");
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
                sleep(500);
            } catch (InterruptedException e) {
                setFinished(true);
                synchronized (Model.this) {
                    if (Model.this.isWaiting()) {
                        Model.this.notify();
                    }
                }
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
                    return;
                }
            }

            Model.this.setFinished(true);
            setChanged();
            notifyObservers();
            this.setFinished(true);
            //System.out.println("done2");
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



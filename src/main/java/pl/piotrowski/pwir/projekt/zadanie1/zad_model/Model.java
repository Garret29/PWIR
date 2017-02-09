package pl.piotrowski.pwir.projekt.zadanie1.zad_model;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Model extends java.util.Observable {


    private final BlockingQueue<BigInteger> queue = new LinkedBlockingQueue<>();
    private BigInteger result;
    private boolean isFinished;
    private final int NUMBERS_QUANTITY = 10000;


    public Model() {
        this.result = new BigInteger("0");
        this.isFinished = false;
    }

    public void start() {
        ThreadA threadA = new ThreadA();
        ThreadB threadB = new ThreadB();
        threadA.start();
        threadB.start();
    }

    public void reset() {
        result = new BigInteger("0");
        queue.clear();
        setFinished(false);
    }

    public class ThreadA extends Thread {

        private Random randomGenerator;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        ThreadA() {
            this.randomGenerator = new Random();
        }

        @Override
        public void run() {
            for (int i = 0; i < 2; i++) {
                executor.execute(new Thread(() -> {
                    for (int j = 1; j <= NUMBERS_QUANTITY / 2; j++) {
                        BigInteger bigNumber = new BigInteger(Integer.toString(randomGenerator.nextInt()));
                        getQueue().add(bigNumber);
                        setChanged();
                        notifyObservers(bigNumber);

                        //Not necessary, made to make GUI update smoothly
                        try {
                            sleep(0, 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }));
            }
            executor.shutdown();
            System.out.println("done1");
        }
    }

    public class ThreadB extends Thread {

        @Override
        public void run() {
            for (int j = 1; j <= NUMBERS_QUANTITY; j++) {
                try {
                    setResult(getResult().add(getQueue().take()));
                    setChanged();
                    notifyObservers(getResult().toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            setFinished(true);
            setChanged();
            notifyObservers();
            System.out.println("done2");
        }
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



/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units
 *
 * Assignment: 1.1.0,
 * Multi-threading.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class ThreadTest {
  /** Implementation using a {@link Thread}. */
  private static class T1 extends Thread {
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          System.out.println("Tråd T1: Tråd 1");
          sleep(1000);
        } catch (InterruptedException e) {
          return; // Thread has been interrupted
        }
      }
    }
  }

  /** Implementation using a {@link Runnable}. */
  private static class T2 implements Runnable {
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          System.out.println("Tråd T2: Tråd 2");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          return; // Thread has been interrupted
        }
      }
    }
  }

  public static void main(String[] args) {
    try {
      var t1 = new T1();
      var t2 = new Thread(new T2());

      t1.start();
      Thread.sleep(5000);
      t2.start();
      Thread.sleep(5000);
      t1.interrupt();
      Thread.sleep(5000);
      t2.interrupt();
    } catch (InterruptedException e) {
      System.exit(1);
      System.err.println(e);
    }
  }
}

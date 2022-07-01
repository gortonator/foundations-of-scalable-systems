package dining.philosophers;

// 
// Thread to represent behavior of a single philosopher
//
public class Philosopher implements Runnable {
 
  private final Object leftChopStick;
  private final Object rightChopStick;
  private static final int SLEEPYTIME = 1000;
 
  Philosopher(Object leftChopStick, Object rightChopStick) {
    this.leftChopStick = leftChopStick;
    this.rightChopStick = rightChopStick;
  }
  private void LogEvent(String event) throws InterruptedException {
    System.out.println(Thread.currentThread().getName() + " " + event);
    Thread.sleep(SLEEPYTIME);
  }
 
  @Override
  public void run() {
    try {
      while (true) {
        LogEvent(": Thinking deeply"); 
        synchronized (leftChopStick) {
          LogEvent( ": Picked up left chop stick");
          synchronized (rightChopStick) {
            LogEvent(": Picked up right chopstick – eating");
            LogEvent(": Put down right chopstick");
          }
          LogEvent(": Put down left chopstick. Ate too much");
        }
      } // end while
    } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
  }
 }
}

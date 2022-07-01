
package dining.philosophers;

//
// Deadlocked implementation of Dining Philosophers
//

public class DeadlockDiningPhilosophers {
    
    private final static int NUMCHOPSTICKS = 5 ;
    private final static int NUMPHILOSOPHERS = 5; 
    
    public static void main(String[] args) throws Exception {

      final Philosopher[] ph = new Philosopher[NUMPHILOSOPHERS];
      Object[] chopSticks = new Object[NUMCHOPSTICKS];

      for (int i = 0; i < NUMCHOPSTICKS; i++) {
        chopSticks[i] = new Object();
      }

      for (int i = 0; i < NUMPHILOSOPHERS; i++) {
          Object leftChopStick = chopSticks[i];
          Object rightChopStick = chopSticks[(i + 1) % chopSticks.length];
          
          ph[i] = new Philosopher(leftChopStick, rightChopStick);
   
          Thread th = new Thread(ph[i], "Philosopher " + i);
          th.start();
      }
   }
}     

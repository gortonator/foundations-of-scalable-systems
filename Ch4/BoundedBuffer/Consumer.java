
package producerconsumerex;

/**
 *
 * @author igorton
 */
import java.util.Random;

public class Consumer implements Runnable {
    private Buffer buffer;

    public Consumer(Buffer buf) {
        this.buffer = buf;
    }

    public void run() {
        Random random = new Random();
        for (String message = buffer.retrieve();
             ! message.equals("DONE");
             message = buffer.retrieve()) {
            System.out.format("MESSAGE RECEIVED: %s%n", message);
            try {
                Thread.sleep(random.nextInt(5000));
            } catch (InterruptedException e) {}
        }
    }
}

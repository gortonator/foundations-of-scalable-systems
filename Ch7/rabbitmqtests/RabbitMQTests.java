/*
** RabbitMQ examples to illustrate two techniques for channel pooling.
** 1) using Apache GenericObjectPool
** 2) Using a simple channel pool using a BlockingQueue
**
** The examples supplement Chapter 7 of the Foundations of Scalable Systems, O'Reilly Media 2022
*/
package rabbitmqtests;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.pool2.*;
import rmqpool.RMQChannelFactory;
import rmqpool.RMQChannelPool;
import org.apache.commons.pool2.impl.*;

/**
 *
 * @author Ian Gorton, Northeastern University
 * 
 */
public class RabbitMQTests {
     // Number of threads to deploy in each test
     private static final int NUM_THREADS = 200;
     // Number of messages to publish in each thread
     private static final int NUM_ITERATIONS = 100;
     // Number of channels to add to pools
     private static final int NUM_CHANS = 30;
     // For Apache pool example, this allows the pool size to grow to ~= the same number of concurrent threads 
     // that utilize the pool. Pass to config.setMaxWait(..) method to allow this behaviour
     private static final int ON_DEMAND = -1;
     // RMQ broker machine
     private static final String SERVER = "localhost";
     // test queue name    
     private static final String QUEUE_NAME = "test";
     // the durtaion in seconds a client waits for a channel to be available in the pool
     // Tune value to meet request load and pass to config.setMaxWait(...) method
     private static final int WAIT_TIME_SECS = 1;
     
     
     
     /**
     * This method runs a multi-threaded test using a channel pool based on the
     * Apache GenericObjectPool class
     * 
     * @param conn A connection to a RabbitMQ broker
     * @throws java.lang.InterruptedException
     */
    public void ApachePoolTest(Connection conn) throws InterruptedException {
        
        final GenericObjectPool<Channel> pool;
        
        // we use this object to tailor the behavior of the GenericObjectPool
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        // The code as is allows the cahhnel pool to grow to meet demand. 
        // Change to config.setMaxTotal(NUM_CHANS) to limit the pool size
        config.setMaxTotal(ON_DEMAND);
        // clients will block when pool is exhausted, for a maximum duration of WAIT_TIME_SECS
        config.setBlockWhenExhausted(true);
        // tune WAIT_TIME_SECS to meet your workload/demand
        config.setMaxWait(Duration.ofSeconds(WAIT_TIME_SECS));
        
        // The channel facory generates new channels on demand, as needed by the GenericObjectPool
        RMQChannelFactory chanFactory = new RMQChannelFactory (conn);
        
        //create the pool 
        pool = new GenericObjectPool<>(chanFactory, config);       
        
        // latch is used for the main thread to block until all test treads complete
        final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        
        // create N threads, each of which uses the channel pool to publish messages
        // TO DO refactor the loop into a method used by both tests?
        for (int i=0; i < NUM_THREADS; i++) {

            Runnable testThread = () -> {     
                for (int j = 0; j < NUM_ITERATIONS; j++) {
                    try {
                         Channel channel;
                         // get a channel from the pool
                         channel = pool.borrowObject();    

                         // publish a message   
                         channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                         byte[] payLoad = "test message apache pool".getBytes();
                         channel.basicPublish("", QUEUE_NAME, null, payLoad);

                         // return the channel to the pool
                         pool.returnObject(channel);
                       
                    } catch (IOException ex) {
                        Logger.getLogger(RabbitMQTests.class.getName()).log(Level.INFO, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(RabbitMQTests.class.getName()).log(Level.INFO, null, ex);
                    }
                }
                // thread has finished, signal parent thread of completion
                latch.countDown();

            };
            new Thread(testThread).start();
        }
        // block until all threads complete 
        latch.await();
        // close the channels and shutdown the pool
        pool.close();
        System.out.println("INFO: Apache pool test finished");
    }
        
     /**
     * This method runs a multithreaded test using a channel pool based on s 
     * simple BlockQueue based implementation
     * 
     * @param conn A valid connection to a RabbitMQ broker
     * @throws java.lang.InterruptedException
     */
    public  void QueuePoolTest(Connection conn) throws InterruptedException {
        
        // The channel facory generates new channels on demand, as needed by the channel pool
        RMQChannelFactory chanFactory = new RMQChannelFactory (conn);
        // create the fixed size channel pool 
        RMQChannelPool pool = new RMQChannelPool(NUM_CHANS, chanFactory);
        
        // latch is used for the main thread to block until all test treads complete
        final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        
        // create N threads, each of which uses the channel pool to publish messages
        // TO DO refactor loop into a method for use by both tests
        for (int i=0; i < NUM_THREADS; i++) {

            Runnable testThread = () -> {     
                for (int j = 0; j < NUM_ITERATIONS; j++) {
                    try {
                         Channel channel;
                         // get a channel from the pool
                         channel = pool.borrowObject();    

                         // publish message
                         channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                         byte[] payLoad = "queue test message".getBytes();
                         channel.basicPublish("", QUEUE_NAME, null, payLoad);

                         // return channel to the pool
                         pool.returnObject(channel);
                       
                    } catch (IOException ex) {
                        Logger.getLogger(RabbitMQTests.class.getName()).log(Level.INFO, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(RabbitMQTests.class.getName()).log(Level.INFO, null, ex);
                    }
                }
                // I've finished - inform parent thread
                latch.countDown();

            };
            new Thread(testThread).start();
        }
        // Block until all threads complete
        latch.await();
        System.out.println("INFO: Queue based pool test finished");
    
    }
    
    private void showTestConfig() {
         System.out.println("INFO: RabbitMQ Channel Pool Examples");
         System.out.println("INFO: Test Configuration");
         System.out.println("INFO: ==================");
         System.out.println(" ");
         System.out.println("INFO: Number of Threads per Test: " + NUM_THREADS);
         System.out.println("INFO: Number of messges to publish per thread: " + NUM_ITERATIONS);
         System.out.println("INFO: Channel Pool Size: " + NUM_CHANS);
         System.out.println("INFO: Queue name: " + QUEUE_NAME);
         System.out.println("INFO: RMQ Broker: " + SERVER);
         System.out.println(" ");
         System.out.println("INFO: ==============================");
         System.out.println(" ");
    }
    
     /**
     * Run this method to execute two multi threaded tests using (1) an Apache GenericObjectPool based implementation
     * and (2)  a channel pool based on s simple BlockQueue based implementation
     *
     */
    public static void main(String[] args) throws IOException, TimeoutException {
         // create object to run tests
        RabbitMQTests test = new RabbitMQTests();     
        test.showTestConfig();

        // connect to local for testing
        // TO DO refactor to use constants or config file
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(SERVER);
        factory.setUsername("guest");
        factory.setPassword("guest");

        final Connection RMQconn = factory.newConnection();
        System.out.println("INFO: RabbitMQ connection established");

        
        // run ApachePoolTest and calculate the duration it takes
        long start = System.nanoTime();
        try { 
            test.ApachePoolTest(RMQconn);        
        } catch (InterruptedException ex) {
            Logger.getLogger(RabbitMQTests.class.getName()).log(Level.SEVERE, null, ex);
        } 
        long duration = System.nanoTime() - start;
        long convert = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);

        System.out.println("INFO: Test Duration =  " + convert + " milliseconds");
        System.out.println("INFO: Apache Pool Test Complete - hit any key to continue");
        
        // read keybord input to continue
        Scanner scan = new Scanner(System.in);
        String text = scan.nextLine();  
        
        // run Blocking Queue based pool test and calculate the duration it takes
        long QueueTestStart = System.nanoTime();
        try { 
            test.QueuePoolTest(RMQconn);        
        } catch (InterruptedException ex) {
            Logger.getLogger(RabbitMQTests.class.getName()).log(Level.SEVERE, null, ex);
        }
        duration = System.nanoTime() - QueueTestStart;
        convert = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);

        System.out.println("INFO: Test Duration =  " + convert + " milliseconds");
        System.out.println("INFO: All tests complete");
        
        // clsoe RMQ connection and terminate
        RMQconn.close();

    }
    
}

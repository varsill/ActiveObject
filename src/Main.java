
import ActiveObject.Proxy;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static int howManyProducers=1000;
    public static int howManyConsumers=1000;
    public static int bufferSize=100;
    public static int timeout=5000;
    public static void main(String[] args) throws InterruptedException {
        if(args.length==4) {
            howManyProducers = Integer.parseInt(args[0]);
            howManyConsumers = Integer.parseInt(args[1]);
            bufferSize = Integer.parseInt(args[2]);
            timeout = Integer.parseInt(args[3]);
        }
        Proxy proxy = new Proxy(bufferSize);

        List<Thread> list = new ArrayList<Thread>();
        Runnable runnable;
        for(int i=0; i<howManyProducers; i++)
        {
            runnable = new Producer(proxy, bufferSize/2);
            Thread t = new Thread(runnable);
            list.add(t);
            t.setPriority(1);
            t.start();
        }

        for(int i=0; i<howManyConsumers; i++)
        {
            runnable = new Consumer(proxy, bufferSize/2);
            Thread t = new Thread(runnable);
            list.add(t);
            t.setPriority(1);
            t.start();
        }
        Thread.sleep(timeout);
        for(Thread t: list)
        {
            try{
                t.interrupt();

            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }


        for(Thread t: list)
        {
            try{
                t.join();

            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }


        try{
            proxy.schedulerThread.interrupt();
            proxy.schedulerThread.join();


        }
        catch (Exception e)
        {
            System.out.println(e);
        }


    }

}

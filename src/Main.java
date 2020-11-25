import ActiveObject.Proxy;

import java.util.ArrayList;
import java.util.List;


public class Main {

    public static final int howManyProducers = 1;
    public static final int howManyConsumers = 1000;
    public static final int bufferSize = 100;
    public static void main(String[] args)
    {

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


        for(Thread t: list)
        {
            try{
                t.join();
            }
            catch (Exception e)
            {

            }
        }


    }

}

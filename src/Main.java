import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;


class Consumer implements Runnable
{

    private static int MAX_SIZE_TO_TAKE = 5;
    private Proxy proxy;
    Random rand = new Random();
    public Consumer(Proxy p)
    {
        proxy = p;
    }
    @Override
    public void run() {

        try {

            while(true){
                int howManyToConsume = rand.nextInt(MAX_SIZE_TO_TAKE-1)+1;
                ArrayFuture future = proxy.consume(howManyToConsume);
                int times = 1;
                while(!future.isReady())
                {
                    Thread.sleep((int) (Math.random() * 10));
                    //System.out.println("CONSUMER: "+Thread.currentThread().getId()+" is waiting for: "+times+" time. He wants to consume: "+howManyToConsume);
                    times++;
                }
                int[] result = future.getResult();

                //Thread.sleep((int) (Math.random() * 10));
                //System.out.println("CONSUMER: "+Thread.currentThread().getId()+" had consumed: "+howManyToConsume+" . He waited: "+times);


            }

        }catch(Exception e)
        {

        }
    }
}

class Producer implements  Runnable
{
    private static int MAX_SIZE_TO_INSERT = 5;
    private Proxy proxy;
    Random rand = new Random();
    public Producer(Proxy p)
    {
        proxy = p;
    }
    @Override
    public void run() {

        try{

            while(true) {

                int howManyToProduce = rand.nextInt(MAX_SIZE_TO_INSERT-1)+1;

                int[] whatToProduce = new int[howManyToProduce];
                proxy.produce(howManyToProduce, whatToProduce);
                //Thread.sleep((int) (Math.random() * 10));

            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}

public class Main {
    private static final ConcurrentSkipListSet<Produce> producingRequests = new ConcurrentSkipListSet<>();
    public static final int howManyProducers = 1000;
    public static final int howManyConsumers = 250;
    public static final int bufferSize = 10;
    public static void main(String[] args)
    {
        /*
        producingRequests.add(new Produce(null, 1, null, 1));
        producingRequests.add(new Produce(null, 1, null, 1));
        producingRequests.add(new Produce(null, 1, null, 1));
        producingRequests.add(new Produce(null, 1, null, 1));
        producingRequests.add(new Produce(null, 1, null, 1));
        System.out.println(producingRequests.size());

        */

        Proxy proxy = new Proxy(bufferSize);
        List<Thread> list = new ArrayList<Thread>();
        Runnable runnable;
        for(int i=0; i<howManyProducers; i++)
        {
            runnable = new Producer(proxy);
            Thread t = new Thread(runnable);
            list.add(t);
            t.start();
        }

        for(int i=0; i<howManyConsumers; i++)
        {
            runnable = new Consumer(proxy);
            Thread t = new Thread(runnable);
            list.add(t);
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

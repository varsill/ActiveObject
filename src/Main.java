import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;


class Consumer implements Runnable
{

    private static int MAX_SIZE_TO_TAKE = 50;
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
                Future<int[]> future = proxy.consume(howManyToConsume);
                int times = 1;

                while(!future.isReady())
                {
                    Thread.sleep((int) (Math.random() * 10));
                    System.out.println("CONSUMER: "+Thread.currentThread().getId()+" is waiting for: "+times+" time. He wants to consume: "+howManyToConsume);
                    times++;
                }
                int[] result = future.getResult();

                //Thread.sleep((int) (Math.random() * 1000));
                //System.out.println("CONSUMER: "+Thread.currentThread().getId()+" had consumed: "+howManyToConsume+" . He waited: "+times);


            }

        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}

class Producer implements  Runnable
{
    private static int MAX_SIZE_TO_INSERT = 50;
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
                Future<Void> future = proxy.produce(howManyToProduce, whatToProduce);

                int times = 1;

                while(!future.isReady())
                {
                    Thread.sleep((int) (Math.random() * 10));
                    //System.out.println("Producer: "+Thread.currentThread().getId()+" is waiting for: "+times+" time. He wants to consume: "+howManyToConsume);
                    times++;
                }


            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}

public class Main {
    private static final ConcurrentSkipListSet<Produce> producingRequests = new ConcurrentSkipListSet<>();
    public static final int howManyProducers = 10000;
    public static final int howManyConsumers = 10000;
    public static final int bufferSize = 100;
    public static void main(String[] args)
    {

        Proxy proxy = new Proxy(bufferSize);
        List<Thread> list = new ArrayList<Thread>();
        Runnable runnable;
        for(int i=0; i<howManyProducers; i++)
        {
            runnable = new Producer(proxy);
            Thread t = new Thread(runnable);
            list.add(t);
            t.setPriority(1);
            t.start();
        }

        for(int i=0; i<howManyConsumers; i++)
        {
            runnable = new Consumer(proxy);
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

import ActiveObject.Future;
import ActiveObject.Proxy;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

class Consumer implements Runnable
{
    private AtomicBoolean running = new AtomicBoolean(true);
    private final int MAX_SIZE_TO_TAKE;
    private Proxy proxy;
    private Random rand = new Random();
    public int howManyMethodRequestDispatched = 0;
    public Consumer(Proxy p, int maxSizeToTake)
    {
        proxy = p;
        MAX_SIZE_TO_TAKE = maxSizeToTake;
    }
    @Override
    public void run() {

        try {

            while(running.get()){
                int howManyToConsume = rand.nextInt(MAX_SIZE_TO_TAKE-1)+1;
                Future<int[]> future = proxy.consume(howManyToConsume);
                /*
                int times = 1;
                while(!future.isReady())
                {
                    Thread.sleep((int) (Math.random() * 10));
                    //System.out.println("CONSUMER: "+Thread.currentThread().getId()+" is waiting for: "+times+" time. He wants to consume: "+howManyToConsume);
                    times++;
                }
                int[] result = future.getResult();
                System.out.println("CONSUMER: "+Thread.currentThread().getId()+" had consumed: "+howManyToConsume+" . He waited: "+times);
                */
                future.waitForReady();
                int[] result = future.getResult();
                System.out.println("CONSUMER: "+Thread.currentThread().getId()+" had consumed: "+howManyToConsume);
                howManyMethodRequestDispatched++;
            }
            System.out.println("CONSUMER: "+Thread.currentThread().getId()+":"+howManyMethodRequestDispatched);
        }catch(Exception e)
        {
            running.set(false);
            System.out.println("CONSUMER: "+Thread.currentThread().getId()+":"+howManyMethodRequestDispatched);
            //System.out.println(e);
        }
    }
}

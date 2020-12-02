import ActiveObject.Future;
import ActiveObject.Proxy;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

class Consumer implements Runnable
{
    private AtomicBoolean running = new AtomicBoolean(true);
    private final int MAX_SIZE_TO_TAKE;
    private final int COUNT_LIMIT;
    private Proxy proxy;
    private Random rand = new Random();
    public int howManyMethodRequestDispatched = 0;
    public Consumer(Proxy p, int maxSizeToTake, int countLimit)
    {
        proxy = p;
        MAX_SIZE_TO_TAKE = maxSizeToTake;
        COUNT_LIMIT = countLimit;
    }

    private int timeConsumingTask()
    {
        int i =0;
        int sum = 0;
        while(i<1000)
        {
            int r = rand.nextInt(10);
            sum+=r;
            i++;
        }
        return sum;

    }

    @Override
    public void run() {

        try {

            while(running.get()){
                int howManyToConsume = rand.nextInt(MAX_SIZE_TO_TAKE-1)+1;
                Future<int[]> future = proxy.consume(howManyToConsume);
                int i=0;
               while(!future.isReady())
                {
                    if(i<COUNT_LIMIT)
                    {
                        i++;
                        timeConsumingTask();
                    }
                    else
                    {
                        break;
                    }

                }
               while(i<COUNT_LIMIT)
               {
                   i++;
                   timeConsumingTask();
               }

                future.waitForReady();
                howManyMethodRequestDispatched++;
                if(Thread.interrupted())
                {
                    running.set(false);
                    System.out.println("C,"+howManyMethodRequestDispatched);
                }
            }

        }catch(Exception e)
        {

            running.set(false);
            System.out.println("C,"+howManyMethodRequestDispatched);
            //System.out.println(e);
        }
    }
}

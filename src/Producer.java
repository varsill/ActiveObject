import ActiveObject.Future;
import ActiveObject.Proxy;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

class Producer implements  Runnable
{
    private AtomicBoolean running = new AtomicBoolean(true);
    private static final int MAX_VALUE = 10000;
    private final int COUNT_LIMIT;
    private final int MAX_SIZE_TO_INSERT;
    private Proxy proxy;
    public int howManyMethodRequestDispatched = 0;
    private Random rand = new Random();
    public Producer(Proxy p, int maxSizeToInsert, int countLimit)
    {
        MAX_SIZE_TO_INSERT = maxSizeToInsert;
        COUNT_LIMIT = countLimit;
        proxy = p;
    }

    private int timeConsumingTask()
    {
        int i =0;
        int sum = 0;
        while(i<1000)
        {
            i++;
            int r = rand.nextInt(10);
            sum+=r;
        }
        return sum;
    }
    @Override
    public void run() {

        try{

            while(running.get()) {

                int howManyToProduce = rand.nextInt(MAX_SIZE_TO_INSERT-1)+1;

                int[] whatToProduce = new int[howManyToProduce];
                for(int i=0; i<howManyToProduce; i++)
                {
                    whatToProduce[i]=rand.nextInt(MAX_VALUE);
                }
                Future<Void> future = proxy.produce(howManyToProduce, whatToProduce);

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
                //System.out.println("PRODUCER: "+Thread.currentThread().getId()+" had produced: "+howManyToProduce+" . He waited: "+times);


                future.waitForReady();
                //System.out.println("PRODUCER: "+Thread.currentThread().getId()+" had produced: "+howManyToProduce);
                howManyMethodRequestDispatched++;
                if(Thread.interrupted())
                {
                    running.set(false);
                    System.out.println("P,"+howManyMethodRequestDispatched);
                }
            }

            //System.out.println("PRODUCER: "+Thread.currentThread().getId()+":"+howManyMethodRequestDispatched);
        }catch(Exception e)
        {
            running.set(false);
            System.out.println("P,"+howManyMethodRequestDispatched);

            //System.out.println(e);

        }
    }
}

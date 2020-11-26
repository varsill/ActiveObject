import ActiveObject.Future;
import ActiveObject.Proxy;

import java.util.Random;

class Producer implements  Runnable
{
    private static final int MAX_VALUE = 100;
    private final int MAX_SIZE_TO_INSERT;
    private Proxy proxy;
    Random rand = new Random();
    public Producer(Proxy p, int maxSizeToInsert)
    {
        MAX_SIZE_TO_INSERT = maxSizeToInsert;
        proxy = p;
    }
    @Override
    public void run() {

        try{

            while(true) {

                int howManyToProduce = rand.nextInt(MAX_SIZE_TO_INSERT-1)+1;

                int[] whatToProduce = new int[howManyToProduce];
                for(int i=0; i<howManyToProduce; i++)
                {
                    whatToProduce[i]=rand.nextInt(MAX_VALUE);
                }
                Future<Void> future = proxy.produce(howManyToProduce, whatToProduce);

                /*
                int times = 1;
                while(!future.isReady())
                {
                    Thread.sleep((int) (Math.random() * 10));
                    System.out.println("Producer: "+Thread.currentThread().getId()+" is waiting for: "+times+" time. He wants to consume: "+howManyToProduce);
                    times++;
                }
                //System.out.println("PRODUCER: "+Thread.currentThread().getId()+" had produced: "+howManyToProduce+" . He waited: "+times);
                */

                future.waitForReady();
                System.out.println("PRODUCER: "+Thread.currentThread().getId()+" had produced: "+howManyToProduce);

            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}

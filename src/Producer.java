import ActiveObject.Future;
import ActiveObject.Proxy;

import java.util.Random;

class Producer implements  Runnable
{
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

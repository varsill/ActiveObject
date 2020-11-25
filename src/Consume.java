public class Consume implements MethodRequest{
    private Servant servant;
    private int howManyToConsume;
    private ArrayFuture<int[]> arrayFuture;
    public int priority = 1;
    private long consumerId;
    public Consume(Servant servant, int howManyToConsume, ArrayFuture<int[]> arrayFuture, long consumerId)
    {
        this.consumerId = consumerId;
        this.servant = servant;
        this.howManyToConsume = howManyToConsume;
        this.arrayFuture = arrayFuture;
    }

    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public boolean guard() {
        if(this.servant.canTakeNElements(this.howManyToConsume))return true;
        else
        {
            //System.out.println("Guard rejection because of he want to consume: "+howManyToConsume+" and in buffer there are: "+this.servant.buffer.size());
            return false;
        }
    }

    @Override
    public void call() throws Exception {

        int[] result = this.servant.consume(this.howManyToConsume);
        System.out.println("CONSUMER: "+ consumerId+" had consumed: "+this.howManyToConsume+". In buffer: "+this.servant.buffer.size());
        this.arrayFuture.bind(result);
    }

    @Override
    public int compareTo(Object o) {
        if(this.priority<((Consume)o).getPriority())
        {
            return -1;
        }
        else if(this.priority==((Consume)o).getPriority())
        {
           if(this.consumerId<((Consume)o).consumerId)
           {
               return -1;
           }
           else
           {
               return 1;
           }
        }
        else
        {
            return 1;
        }
    }
}

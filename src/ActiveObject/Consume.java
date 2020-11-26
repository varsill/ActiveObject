package ActiveObject;

public class Consume implements MethodRequest{
    private Servant servant;
    private int howManyToConsume;
    private Future<int[]> future;
    public int priority = 1;
    private long creationTime;
    public Consume(Servant servant, int howManyToConsume, Future<int[]> future, long creationTime)
    {
        this.creationTime = creationTime;
        this.servant = servant;
        this.howManyToConsume = howManyToConsume;
        this.future = future;
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
            return false;
        }
    }

    @Override
    public void call() throws Exception {

        int[] result = this.servant.consume(this.howManyToConsume);
        this.future.bind(result);
    }

    @Override
    public int compareTo(Object o) {
        if(this.priority<((Consume)o).getPriority())
        {
            return -1;
        }
        else if(this.priority==((Consume)o).getPriority())
        {
           if(this.creationTime<((Consume)o).creationTime)
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

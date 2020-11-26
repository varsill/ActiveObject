package ActiveObject;

public class Produce implements MethodRequest {
    private Servant servant;
    private int howManyToProduce;
    private int[] whatToProduce;
    public int priority = 1;
    private long creationTime;
    private Future<Void> future;
    public Produce(Servant servant, int howManyToProduce, int[] whatToProduce, Future<Void> future, long creationTime)
    {
        this.creationTime = creationTime;
        this.servant = servant;
        this.howManyToProduce = howManyToProduce;
        this.whatToProduce = whatToProduce;
        this.future = future;
    }

    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public boolean guard() {
        if(this.servant.canPutNElements(this.howManyToProduce))return true;
        else
        {
            return false;
        }
    }

    @Override
    public void call() throws Exception {
        this.servant.produce(this.howManyToProduce, this.whatToProduce);
        this.future.bind(null);
    }

    @Override
    public int compareTo(Object o) {
        if(this.priority<((Produce)o).getPriority())
        {
            return -1;
        }
        else if(this.priority==((Produce)o).getPriority())
        {
            if(this.creationTime<((Produce)o).creationTime)
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

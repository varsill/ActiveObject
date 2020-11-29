package ActiveObject;

public class Consume extends MethodRequestWithPriority {
    private Servant servant;
    private int howManyToConsume;
    private Future<int[]> future;

    public Consume(Servant servant, int howManyToConsume, Future<int[]> future, long creationTime)
    {

        this.creationTime = creationTime;
        this.servant = servant;
        this.howManyToConsume = howManyToConsume;
        this.future = future;
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
}

package ActiveObject;

public class Produce extends MethodRequestWithPriority {
    private Servant servant;
    private int howManyToProduce;
    private int[] whatToProduce;

    private Future<Void> future;
    public Produce(Servant servant, int howManyToProduce, int[] whatToProduce, Future<Void> future, long creationTime)
    {
        this.creationTime = creationTime;
        this.servant = servant;
        this.howManyToProduce = howManyToProduce;
        this.whatToProduce = whatToProduce;
        this.future = future;
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


}

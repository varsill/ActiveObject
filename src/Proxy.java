public class Proxy {
    public static final int MAX_SIZE = 100;

    protected Scheduler scheduler;
    protected Servant servant;

    public Proxy(int size)
    {
        this.servant = new Servant(size);
        this.scheduler = new Scheduler(servant);
        this.scheduler.startExecutingThread();

    }

    public Proxy()
    {
        this(MAX_SIZE);
    }

    ArrayFuture<Void> produce(int howManyToProduce, int[] whatToProduce)
    {
        ArrayFuture<Void> result = new ArrayFuture<Void>();

        Produce methodRequest = new Produce(this.servant, howManyToProduce, whatToProduce, result, Thread.currentThread().getId());
        this.scheduler.enqueueProducingRequest(methodRequest);
        return result;
    }

    ArrayFuture<int[]> consume(int howManyToConsume)
    {
        ArrayFuture<int[]> result = new ArrayFuture<int[]>();

        Consume methodRequest = new Consume(this.servant, howManyToConsume, result, Thread.currentThread().getId());
        this.scheduler.enqueueConsumingRequest(methodRequest);
        return result;
    }

}

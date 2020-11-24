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

    void produce(int howManyToProduce, int[] whatToProduce)
    {
        Produce methodRequest = new Produce(this.servant, howManyToProduce, whatToProduce, Thread.currentThread().getId());
        this.scheduler.enqueueProducingRequest(methodRequest);
    }

    ArrayFuture consume(int howManyToConsume)
    {
        ArrayFuture result = new ArrayFuture();

        Consume methodRequest = new Consume(this.servant, howManyToConsume, result, Thread.currentThread().getId());
        this.scheduler.enqueueConsumingRequest(methodRequest);
        return result;
    }

}

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

    Future<Void> produce(int howManyToProduce, int[] whatToProduce) throws InterruptedException {
        Future<Void> result = new Future<Void>();

        Produce methodRequest = new Produce(this.servant, howManyToProduce, whatToProduce, result, Thread.currentThread().getId());
        this.scheduler.enqueueProducingRequest(methodRequest);
        return result;
    }

    Future<int[]> consume(int howManyToConsume) throws InterruptedException {
        Future<int[]> result = new Future<int[]>();

        Consume methodRequest = new Consume(this.servant, howManyToConsume, result, Thread.currentThread().getId());
        this.scheduler.enqueueConsumingRequest(methodRequest);
        return result;
    }

}

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {

    private final Servant servant;
    private final ConcurrentSkipListSet<Produce> producingRequests;
    private final ConcurrentSkipListSet<Consume>  consumingRequests;
    private ReentrantLock lock = new ReentrantLock();
    private Condition waitForSchedulerFinishingJobOnProducingRequests = lock.newCondition();
    private boolean isSchedulerModifyingProducingRequests = false;

    private Condition waitForSchedulerFinishingJobOnConsumingRequests = lock.newCondition();
    private boolean isSchedulerModifyingConsumingRequests = false;

   public Scheduler(Servant servant)
   {
       this.servant = servant;
       producingRequests = new ConcurrentSkipListSet<Produce>();
       consumingRequests = new ConcurrentSkipListSet<Consume>();

   }

   public void enqueueConsumingRequest(Consume methodRequest) throws InterruptedException {
       lock.lock();
       while(isSchedulerModifyingConsumingRequests)
       {
           this.waitForSchedulerFinishingJobOnConsumingRequests.await();
       }
       consumingRequests.add( methodRequest);
       lock.unlock();

   }

    public void enqueueProducingRequest(Produce methodRequest) throws InterruptedException {
        lock.lock();
        while(isSchedulerModifyingProducingRequests)
        {
            this.waitForSchedulerFinishingJobOnProducingRequests.await();
        }
        producingRequests.add(methodRequest);
        lock.unlock();
       // System.out.println("ProducingRequest: "+producingRequests.size());
    }

    public Consume dequeConsumingRequest() throws InterruptedException {
        lock.lock();
        while(isSchedulerModifyingConsumingRequests)
        {
            this.waitForSchedulerFinishingJobOnConsumingRequests.await();
        }
        Consume result = consumingRequests.pollFirst();
        lock.unlock();
        return result;
    }

    public Produce dequeProducingRequest() throws InterruptedException {
        lock.lock();
        while(isSchedulerModifyingProducingRequests)
        {
            this.waitForSchedulerFinishingJobOnConsumingRequests.await();
        }
        Produce result = producingRequests.pollFirst();
        lock.unlock();
        return  result;
   }



    private void enqueueConsumingRequestAsScheduler(Consume methodRequest)
    {

        isSchedulerModifyingConsumingRequests=true;
        lock.lock();
        consumingRequests.add( methodRequest);
        waitForSchedulerFinishingJobOnConsumingRequests.signalAll();
        isSchedulerModifyingConsumingRequests=false;
        lock.unlock();
        //System.out.println("ConsumingRequest: "+consumingRequests.size());
    }

    private void enqueueProducingRequestAsScheduler(Produce methodRequest)
    {

        isSchedulerModifyingProducingRequests=true;
        lock.lock();
        producingRequests.add(methodRequest);

        waitForSchedulerFinishingJobOnProducingRequests.signalAll();
        isSchedulerModifyingProducingRequests=false;
        lock.unlock();

        // System.out.println("ProducingRequest: "+producingRequests.size());
    }

    private Consume dequeConsumingRequestAsScheduler()
    {

        isSchedulerModifyingConsumingRequests=true;
        lock.lock();
        Consume result = consumingRequests.pollFirst();

        waitForSchedulerFinishingJobOnConsumingRequests.signalAll();
        isSchedulerModifyingConsumingRequests=false;
        lock.unlock();
        return result;
    }


    private Produce dequeProducingRequestAsScheduler()
    {

        isSchedulerModifyingProducingRequests=true;
        lock.lock();
        Produce result = producingRequests.pollFirst();

        waitForSchedulerFinishingJobOnProducingRequests.signalAll();
        isSchedulerModifyingProducingRequests=false;
        lock.unlock();
        return result;
    }






    private final void dispatch() throws Exception {
        while(true) {
            //System.out.println("I am dispatching: "+servant.howManyTakenPlaces());
            //System.out.println("ConsumingRequest: "+consumingRequests.size()+" ProducingRequest: "+producingRequests.size()+" FREE: "+this.servant.howManyFreePlaces()+"/"+this.servant.bufferSize);

            if(servant.howManyTakenPlaces()>servant.bufferSize/2)
            {
                //System.out.println("There are more taken places. FREE: " +servant.howManyFreePlaces());
                MethodRequest request = dequeConsumingRequestAsScheduler();
                if(request!=null&&request.guard())
                {
                    //System.out.println("I have chosen default consumer.");
                    request.call();
                }
                else {

                    request = dequeProducingRequestAsScheduler();
                    if(request==null)continue;
                    if(!request.guard()) {
                        //System.out.println("Requeing");
                        ((Produce) request).priority *= 2;
                        enqueueProducingRequestAsScheduler((Produce)request);

                    }
                    else
                    {
                        request.call();
                    }


                }
            }
            else
            {
                //System.out.println("There are more free places. FREE: "+servant.howManyFreePlaces());
                MethodRequest request = dequeProducingRequestAsScheduler();
                if(request!=null&&request.guard())
                {
                    //System.out.println("I have chosen default producer.");
                    request.call();
                }
                else {
                    //System.out.println("There were no producers, but there are: "+consumingRequests.size()+" consumers.");
                    request = dequeConsumingRequestAsScheduler();
                    if(request==null)continue;
                    if (!request.guard()) {
                        System.out.println("Requeing");
                        ((Consume) request).priority *= 2;
                        enqueueConsumingRequestAsScheduler((Consume)request);

                    }
                    else
                    {
                        request.call();
                    }


                }
            }

        }
    }

    public void startExecutingThread() {
        Thread thread = new Thread(() -> {
            try {
                this.dispatch();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        thread.setPriority(10);
        thread.start();
    }


}

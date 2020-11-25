import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {

    private final Servant servant;
    private final Deque<Produce> producingRequests;
    private final Deque<Consume> consumingRequests;

    private ReentrantLock lockForProducingRequests = new ReentrantLock(true);
    private Condition waitForSchedulerFinishingJobOnProducingRequests = lockForProducingRequests.newCondition();
    private boolean isSchedulerModifyingProducingRequests = false;
    private boolean isAnotherThreadModyfingProducingRequests = false;

    private ReentrantLock stateLock = new ReentrantLock(true);
    private Condition conditionForScheduler = stateLock.newCondition();

    private ReentrantLock lockForConsumingRequests = new ReentrantLock(true);
    private Condition waitForSchedulerFinishingJobOnConsumingRequests = lockForConsumingRequests.newCondition();
    private boolean isSchedulerModifyingConsumingRequests = false;
    private boolean isAnotherThreadModyfingConsumingRequests = false;

   public Scheduler(Servant servant)
   {
       this.servant = servant;
       producingRequests = new LinkedList<>();
       consumingRequests = new LinkedList<Consume>();

   }

   public void enqueueConsumingRequest(Consume methodRequest) throws InterruptedException {

       lockForConsumingRequests.lock();

       while(isSchedulerModifyingConsumingRequests)
       {
           this.waitForSchedulerFinishingJobOnConsumingRequests.await();
       }


       stateLock.lock();
       isAnotherThreadModyfingConsumingRequests = true;
       stateLock.unlock();

       consumingRequests.addLast( methodRequest);

       stateLock.lock();
       isAnotherThreadModyfingConsumingRequests = false;
       conditionForScheduler.signal();
       stateLock.unlock();

       lockForConsumingRequests.unlock();

   }

    public void enqueueProducingRequest(Produce methodRequest) throws InterruptedException {

        lockForProducingRequests.lock();

        while(isSchedulerModifyingProducingRequests)
        {

            this.waitForSchedulerFinishingJobOnProducingRequests.await();
        }


        stateLock.lock();
        isAnotherThreadModyfingProducingRequests = true;
        stateLock.unlock();

        producingRequests.addLast(methodRequest);

        stateLock.lock();
        isAnotherThreadModyfingProducingRequests = false;
        conditionForScheduler.signal();
        stateLock.unlock();

        lockForProducingRequests.unlock();


       // System.out.println("ProducingRequest: "+producingRequests.size());
    }


    private void enqueueConsumingRequestAsScheduler(Consume methodRequest) throws InterruptedException {
        if(methodRequest==null)System.out.println("TSO");
        isSchedulerModifyingConsumingRequests=true;
        stateLock.lock();
        while(isAnotherThreadModyfingConsumingRequests)
        {
            conditionForScheduler.await();
        }
        stateLock.unlock();

        lockForConsumingRequests.lock();
        consumingRequests.addFirst(methodRequest);


        isSchedulerModifyingConsumingRequests=false;

        waitForSchedulerFinishingJobOnConsumingRequests.signalAll();
        lockForConsumingRequests.unlock();
    }

    private void enqueueProducingRequestAsScheduler(Produce methodRequest) throws InterruptedException {
        if(methodRequest==null)System.out.println("TSO");
        isSchedulerModifyingProducingRequests=true;
        stateLock.lock();
        while(isAnotherThreadModyfingProducingRequests)
        {
            conditionForScheduler.await();
        }
        stateLock.unlock();

        lockForProducingRequests.lock();
        producingRequests.addFirst(methodRequest);


        isSchedulerModifyingProducingRequests=false;

        waitForSchedulerFinishingJobOnProducingRequests.signalAll();
        lockForProducingRequests.unlock();


        // System.out.println("ProducingRequest: "+producingRequests.size());
    }

    private Consume dequeConsumingRequestAsScheduler() throws InterruptedException {

        isSchedulerModifyingConsumingRequests=true;
        stateLock.lock();
        while(isAnotherThreadModyfingConsumingRequests)
        {
            conditionForScheduler.await();
        }
        stateLock.unlock();

        lockForConsumingRequests.lock();
        Consume result = consumingRequests.pollFirst();


        isSchedulerModifyingConsumingRequests=false;

        waitForSchedulerFinishingJobOnConsumingRequests.signalAll();
        lockForConsumingRequests.unlock();
        return result;
    }


    private Produce dequeProducingRequestAsScheduler() throws InterruptedException {

        isSchedulerModifyingProducingRequests=true;
        stateLock.lock();
        while(isAnotherThreadModyfingProducingRequests)
        {
            conditionForScheduler.await();
        }
        stateLock.unlock();

        lockForProducingRequests.lock();
        Produce result = producingRequests.pollFirst();

        isSchedulerModifyingProducingRequests=false;

        waitForSchedulerFinishingJobOnProducingRequests.signalAll();
        lockForProducingRequests.unlock();
        return result;
    }






    private final void dispatch() throws Exception {
        while(true) {
            //System.out.println("I am dispatching: "+servant.howManyTakenPlaces());
            //System.out.println("ConsumingRequest: "+consumingRequests.size()+" ProducingRequest: "+producingRequests.size()+" FREE: "+this.servant.howManyFreePlaces()+"/"+this.servant.bufferSize);
            //if(producingRequests.contains(null))System.out.println("producing zepsuty");
            //if(consumingRequests.contains(null))System.out.println("consuming zepsuty");
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
                        //System.out.println("Requeing");
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

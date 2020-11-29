package ActiveObject;

import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {

    private final Servant servant;
    private final PriorityQueue<Produce> producingRequests;
    private final PriorityQueue<Consume> consumingRequests;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ReentrantLock lock= new ReentrantLock(true);
    private Condition condition = lock.newCondition();


   public Scheduler(Servant servant)
   {
       this.servant = servant;
       producingRequests = new PriorityQueue<>();
       consumingRequests = new PriorityQueue<Consume>();
   }

   public void enqueueConsumingRequest(Consume methodRequest) {


       lock.lock();
       try
    {
        consumingRequests.add(methodRequest);
        condition.signal();
    }
    catch (Exception e)
    {
        System.out.println(e);
    }
    finally {
        lock.unlock();
    }

   }

    public void enqueueProducingRequest(Produce methodRequest){
       lock.lock();
       try
        {
            producingRequests.add(methodRequest);
            condition.signal();
        }
        catch (Exception e)
        {
            System.out.println();
        }
        finally {
            lock.unlock();
        }

    }


    private void enqueueConsumingRequestAsScheduler(Consume methodRequest){
           consumingRequests.add(methodRequest);
    }

    private void enqueueProducingRequestAsScheduler(Produce methodRequest){
       producingRequests.add(methodRequest);

    }

    private Consume dequeConsumingRequestAsScheduler() {
        Consume result = null;
        result = consumingRequests.poll();

        return result;
    }


    private Produce dequeProducingRequestAsScheduler() {
       Produce result = null;
       result = producingRequests.poll();
        return result;
    }






    private final void dispatch(){


        try {
            while (running.get()) {
                //System.out.println("I am dispatching: "+servant.howManyTakenPlaces());
                //System.out.println("ConsumingRequest: "+consumingRequests.size()+" ProducingRequest: "+producingRequests.size()+" FREE: "+this.servant.howManyFreePlaces()+"/"+this.servant.bufferSize);
                lock.lock();
                boolean wasCalled = false;
                if (servant.howManyTakenPlaces() >= servant.bufferSize / 2) {
                    MethodRequest request = dequeConsumingRequestAsScheduler();
                    if (request != null && request.guard()) {

                        request.call();
                        wasCalled = true;
                    } else {

                        request = dequeProducingRequestAsScheduler();
                        if (request != null && !request.guard()) {

                            ((Produce) request).priority *= 10;
                            enqueueProducingRequestAsScheduler((Produce) request);

                        } else {
                            if (request != null) {
                                request.call();
                                wasCalled = true;
                            }
                        }


                    }
                } else {

                    MethodRequest request = dequeProducingRequestAsScheduler();
                    if (request != null && request.guard()) {
                        request.call();
                        wasCalled = true;
                    } else {

                        request = dequeConsumingRequestAsScheduler();

                        if (request != null && !request.guard()) {
                            ((Consume) request).priority *= 10;
                            enqueueConsumingRequestAsScheduler((Consume) request);

                        } else {
                            if (request != null) {
                                request.call();
                                wasCalled = true;
                            }

                        }


                    }
                }
                if (!wasCalled) {
                    //System.out.println("l");
                    condition.await();
                }
                lock.unlock();

            }
        }catch (Exception e)
        {
            //System.out.println("XDDD123");
            running.set(false);
        }

    }

    public Thread startExecutingThread() {
        Thread thread = new Thread(() -> {

                this.dispatch();


        });
        thread.setPriority(10);
        thread.start();
        return thread;
    }


}

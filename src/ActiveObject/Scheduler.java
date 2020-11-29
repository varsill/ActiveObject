package ActiveObject;

import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {

    private final Servant servant;
    private final PriorityQueue<Produce> producingRequests;
    private final PriorityQueue<Consume> consumingRequests;

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
       try
       {
           //lock.lock();
           consumingRequests.add(methodRequest);
       }
       catch (Exception e)
       {
           System.out.println(e);
       }
       finally {
           //lock.unlock();
       }


    }

    private void enqueueProducingRequestAsScheduler(Produce methodRequest){
        try
        {
            //lock.lock();
            producingRequests.add(methodRequest);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            //lock.unlock();
        }


    }

    private Consume dequeConsumingRequestAsScheduler() {
        Consume result = null;
        try
        {
            //lock.lock();
            result = consumingRequests.poll();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            //lock.unlock();
        }
        return result;
    }


    private Produce dequeProducingRequestAsScheduler() throws InterruptedException {
       Produce result = null;
       try
        {
            //lock.lock();
            result = producingRequests.poll();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            //lock.unlock();
        }

        return result;
    }






    private final void dispatch() throws Exception {
        boolean wasCalled = false;
        while(true) {
            //System.out.println("I am dispatching: "+servant.howManyTakenPlaces());
            //System.out.println("ConsumingRequest: "+consumingRequests.size()+" ProducingRequest: "+producingRequests.size()+" FREE: "+this.servant.howManyFreePlaces()+"/"+this.servant.bufferSize);
            lock.lock();
            if(servant.howManyTakenPlaces()>=servant.bufferSize/2)
            {
                MethodRequest request = dequeConsumingRequestAsScheduler();
                if(request!=null&&request.guard())
                {

                    request.call();
                    wasCalled = true;
                }
                else {

                    request = dequeProducingRequestAsScheduler();
                    if(request!=null&&!request.guard()) {

                        ((Produce) request).priority *= 10;
                        enqueueProducingRequestAsScheduler((Produce)request);

                    }
                    else
                    {
                        if(request!=null)
                        {
                            request.call();
                            wasCalled = true;
                        }
                    }


                }
            }
            else
            {

                MethodRequest request = dequeProducingRequestAsScheduler();
                if(request!=null&&request.guard())
                {
                    request.call();
                    wasCalled = true;
                }
                else {

                    request = dequeConsumingRequestAsScheduler();

                    if (request!=null&&!request.guard()) {
                        ((Consume) request).priority *= 10;
                        enqueueConsumingRequestAsScheduler((Consume)request);

                    }
                    else
                    {
                        if(request!=null)
                        {
                            request.call();
                            wasCalled = true;
                        }

                    }


                }
            }
            if(!wasCalled)
            {
                System.out.println("l");
                condition.await();
            }
            lock.unlock();

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

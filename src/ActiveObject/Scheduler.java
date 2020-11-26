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

    private ReentrantLock lockForProducingRequests = new ReentrantLock(true);
    private ReentrantLock lockForConsumingRequests = new ReentrantLock(true);

   public Scheduler(Servant servant)
   {
       this.servant = servant;
       producingRequests = new PriorityQueue<>();
       consumingRequests = new PriorityQueue<Consume>();

   }

   public void enqueueConsumingRequest(Consume methodRequest) {
    try
    {
        lockForConsumingRequests.lock();
        consumingRequests.add( methodRequest);
    }
    catch (Exception e)
    {
        System.out.println(e);
    }
    finally {
        lockForConsumingRequests.unlock();
    }

   }

    public void enqueueProducingRequest(Produce methodRequest){
        try
        {
            lockForProducingRequests.lock();
            producingRequests.add(methodRequest);
        }
        catch (Exception e)
        {
            System.out.println();
        }
        finally {
            lockForProducingRequests.unlock();
        }

    }


    private void enqueueConsumingRequestAsScheduler(Consume methodRequest){
       try
       {
           lockForConsumingRequests.lock();
           consumingRequests.add(methodRequest);
       }
       catch (Exception e)
       {
           System.out.println(e);
       }
       finally {
           lockForConsumingRequests.unlock();
       }


    }

    private void enqueueProducingRequestAsScheduler(Produce methodRequest){
        try
        {
            lockForProducingRequests.lock();
            producingRequests.add(methodRequest);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            lockForProducingRequests.unlock();
        }


    }

    private Consume dequeConsumingRequestAsScheduler() {
        Consume result = null;
        try
        {
            lockForConsumingRequests.lock();
            result = consumingRequests.poll();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            lockForConsumingRequests.unlock();
        }
        return result;
    }


    private Produce dequeProducingRequestAsScheduler() throws InterruptedException {
       Produce result = null;
       try
        {
            lockForProducingRequests.lock();
            result = producingRequests.poll();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            lockForProducingRequests.unlock();
        }

        return result;
    }






    private final void dispatch() throws Exception {
        while(true) {
            //System.out.println("I am dispatching: "+servant.howManyTakenPlaces());
            //System.out.println("ConsumingRequest: "+consumingRequests.size()+" ProducingRequest: "+producingRequests.size()+" FREE: "+this.servant.howManyFreePlaces()+"/"+this.servant.bufferSize);
            //if(producingRequests.contains(null))System.out.println("producing zepsuty");
            //if(consumingRequests.contains(null))System.out.println("consuming zepsuty");
            if(servant.howManyTakenPlaces()>=servant.bufferSize/2)
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
                        ((Produce) request).priority *= 10;
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
                        ((Consume) request).priority *= 10;
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

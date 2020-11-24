import java.util.concurrent.ConcurrentSkipListSet;

public class Scheduler {

    private final Servant servant;
    private final ConcurrentSkipListSet<Produce> producingRequests;
    private final ConcurrentSkipListSet<Consume>  consumingRequests;
   public Scheduler(Servant servant)
   {
       this.servant = servant;
       producingRequests = new ConcurrentSkipListSet<Produce>();
       consumingRequests = new ConcurrentSkipListSet<Consume>();

   }

   public synchronized void enqueueConsumingRequest(Consume methodRequest)
   {

       consumingRequests.add( methodRequest);
       //System.out.println("ConsumingRequest: "+consumingRequests.size());


   }

    public synchronized void enqueueProducingRequest(Produce methodRequest)
    {
        producingRequests.add(methodRequest);
       // System.out.println("ProducingRequest: "+producingRequests.size());
    }

    public synchronized Consume dequeConsumingRequest()
    {
        return consumingRequests.pollFirst();
    }

    public synchronized Produce dequeProducingRequest()
    {
        return producingRequests.pollFirst();
    }

    private final void dispatch() throws Exception {
        while(true) {
            //System.out.println("I am dispatching: "+servant.howManyTakenPlaces());
            //System.out.println("ConsumingRequest: "+consumingRequests.size()+" ProducingRequest: "+producingRequests.size()+" FREE: "+this.servant.howManyFreePlaces()+"/"+this.servant.bufferSize);

            if(servant.howManyTakenPlaces()>servant.bufferSize/2)
            {
                //System.out.println("There are more taken places. FREE: " +servant.howManyFreePlaces());
                MethodRequest request = dequeConsumingRequest();
                if(request!=null&&request.guard())
                {
                    //System.out.println("I have chosen default consumer.");
                    request.call();
                }
                else {
                    request = dequeProducingRequest();
                    if(request==null)continue;
                    if(!request.guard()) {
                        System.out.println("Requeing");
                        ((Produce) request).priority *= 2;
                        enqueueProducingRequest((Produce)request);

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
                MethodRequest request = dequeProducingRequest();
                if(request!=null&&request.guard())
                {
                    //System.out.println("I have chosen default producer.");
                    request.call();
                }
                else {
                    //System.out.println("There were no producers, but there are: "+consumingRequests.size()+" consumers.");
                    request = dequeConsumingRequest();
                    if(request==null)continue;
                    if (!request.guard()) {
                        System.out.println("Requeing");
                        ((Consume) request).priority *= 2;
                        enqueueConsumingRequest((Consume)request);

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
        thread.start();
    }


}

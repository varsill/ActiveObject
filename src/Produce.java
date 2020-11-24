public class Produce implements MethodRequest {
    private Servant servant;
    private int howManyToProduce;
    private int[] whatToProduce;
    public int priority = 1;
    private long producerId;
    public Produce(Servant servant, int howManyToProduce, int[] whatToProduce, long producerId)
    {
        this.producerId = producerId;
        this.servant = servant;
        this.howManyToProduce = howManyToProduce;
        this.whatToProduce = whatToProduce;
    }

    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public boolean guard() {
        if(this.servant.canPutNElements(this.howManyToProduce))return true;
        else
        {
           // System.out.println("Guard rejection because of: "+this.servant.buffer.size());
            return false;
        }
    }

    @Override
    public void call() throws Exception {
        this.servant.produce(this.howManyToProduce, this.whatToProduce);
        System.out.println("PRODUCER: "+ producerId+" had produced: "+this.howManyToProduce+". In buffer: "+this.servant.buffer.size());

    }

    @Override
    public int compareTo(Object o) {
        if(this.priority<((Produce)o).getPriority())
        {
            return -1;
        }
        else if(this.priority==((Produce)o).getPriority())
        {
            if(this.producerId<((Produce)o).producerId)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return 1;
        }
    }
}

package ActiveObject;

import java.util.LinkedList;

public class Servant {
    public final int bufferSize;
    public final LinkedList<Integer> buffer;

    public Servant(int bufferSize)
    {
        this.bufferSize=bufferSize;
        this.buffer = new LinkedList<Integer>();
    }
    public void produce(int howManyToProduce, int[] whatToProduce) throws Exception
    {
        if(howManyToProduce>this.howManyFreePlaces())
        {
            throw new Exception("There are not enough free places.");
        }

        for(int i=0; i<howManyToProduce; i++)
        {
            buffer.add(whatToProduce[i]);
        }

    }

    public int[] consume(int howManyToConsume) throws Exception
    {
        if(howManyToConsume>this.howManyTakenPlaces())
        {
            throw new Exception("There are not enough taken places.");
        }

        int[] result = new int[howManyToConsume];
        for(int i=0; i<howManyToConsume; i++)
        {
            result[i]=buffer.pop();
        }
        return result;

    }

    public boolean canPutNElements(int n)
    {
        if(n>this.howManyFreePlaces())return false;
        return true;
    }

    public boolean canTakeNElements(int n)
    {
        if(n>this.howManyTakenPlaces())return false;
        return true;
    }


    public int howManyTakenPlaces()
    {
        return this.buffer.size();
    }

    public int howManyFreePlaces()
    {
        return this.bufferSize-this.buffer.size();
    }
}

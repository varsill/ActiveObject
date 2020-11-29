package ActiveObject;

public abstract class MethodRequestWithPriority implements MethodRequest{
    public int priority = 1;
    public long creationTime;
    @Override
    public int compareTo(Object o) {
        if(this.priority>((MethodRequestWithPriority)o).priority)
        {
            return -1;
        }
        else if(this.priority==((MethodRequestWithPriority)o).priority)
        {
            if(this.creationTime<((MethodRequestWithPriority)o).creationTime)
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

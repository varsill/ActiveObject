package ActiveObject;

public  class Future<T>{

    private boolean _isReady;
    private T result;
    public Future()
    {
        this._isReady = false;
    }

    public synchronized void bind(T result) {
        this.result = result;
        this._isReady=true;
        notify();
    }

    public synchronized boolean  isReady()
    {
        return _isReady;
    }
    public synchronized void waitForReady() throws InterruptedException {
        while(!_isReady)
        {
            wait();
        }

    }
    public T getResult() throws Exception
    {
        if(!_isReady)
        {
            throw new Exception("Attempt to get result from ArrayFuture when result is not ready.");
        }
        else
        {
            return this.result;
        }

    }
}

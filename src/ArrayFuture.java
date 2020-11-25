public  class  ArrayFuture<T>{

    private boolean _isReady;
    private T result;
    public ArrayFuture()
    {
        this._isReady = false;
    }

    public void bind(T result) {
        this.result = result;
        this._isReady=true;
    }

    public boolean isReady()
    {
        return _isReady;
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

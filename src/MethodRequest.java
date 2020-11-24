public interface MethodRequest extends Comparable
{
    boolean guard();
    void call() throws Exception;

}

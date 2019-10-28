package ttl.advjava.mytry;

/**
 * @author whynot
 */
public interface MyFunction<T, R>{
    public R apply(T t) throws Exception;
}

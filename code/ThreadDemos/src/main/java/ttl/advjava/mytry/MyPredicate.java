package ttl.advjava.mytry;

/**
 * @author whynot
 */
public interface MyPredicate<T> {
    public boolean test(T t) throws Exception;
}

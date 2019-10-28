package ttl.advjava.mytry;


import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author whynot
 */
public class TryWrap<T> {
    private T right;
    private Exception left;
    private boolean empty = false;

    /**
     * Catch either result or Exception in a TryWrap
     *
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> TryWrap<T> of(MySupplier<T> supplier) {
        try {
            T t = supplier.get();
            return ofRight(t);
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    /**
     * Wrap any Exceptions in a TryWrap
     *
     * @param runnable
     * @param <T>
     * @return
     */
    public static <T> TryWrap<T> of(MyRunnable runnable) {
        try {
            runnable.run();
            return ofEmpty();
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    public static <T> TryWrap<T> ofEmpty() {
        TryWrap<T> me = new TryWrap<>(null, null);
        me.empty = true;
        return me;
    }

    public static <T> TryWrap<T> ofRight(T x) {
        TryWrap<T> me = new TryWrap<>(null, x);
        return me;
    }

    public static <T> TryWrap<T> ofLeft(Exception y) {
        TryWrap<T> me = new TryWrap<>(y, null);
        return me;
    }

    public T right() {
        if (right == null) {
            checkForEmpty();
            throw new NoSuchElementException("Null or Empty TryWrap");
        }
        return right;
    }

    public Exception left() {
        return left;
    }

    public boolean isEmpty() {
        return empty == true;
    }

    public boolean isRight() {
        return right != null;
    }

    public boolean isLeft() {
        return left != null;
    }

    public <R> TryWrap<R> map(MyFunction<? super T, ? extends R> function) {
        if (right == null) {
            if (isEmpty()) {
                return ofEmpty();
            }
            return ofLeft(left);
        }
        try {
            R r = function.apply(right);
            return ofRight(r);
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    public <R> TryWrap<R> flatMap(MyFunction<? super T, TryWrap<R>> function) {
        if (right == null) {
            if (isEmpty()) {
                return ofEmpty();
            }
            return ofLeft(left);
        }
        try {
            TryWrap<R> r = function.apply(right);
            return r;
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    public TryWrap<T> filter(MyPredicate<? super T> pred) {
        if (right == null) {
            if (isEmpty()) {
                return ofEmpty();
            }
            return ofLeft(left);
        }
        boolean allOk = false;
        try {
            boolean r = pred.test(right);
            if (r) {
                return ofRight(right);
            } else {
                return ofEmpty();
            }
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    private void checkForEmpty() {
        if (isEmpty() && left == null) {
            left = new NoSuchElementException("Empty TryWrap");
        }
    }

    public void emptyThrow() throws Exception {
        if(isLeft()) {
            throw left;
        }
    }

    public T orElseThrow() throws Exception {
        if (isRight()) {
            return right;
        }
        checkForEmpty();
        throw left;
    }

    public T orElseThrowUnchecked() {
        if (isRight()) {
            return right;
        }
        checkForEmpty();
        throw new RuntimeException(left);
    }


    public T orElseThrow(Exception e) throws Exception {
        if (isRight()) {
            return right;
        }
        throw e;
    }

    public void orElseConsume(Consumer<Exception> consumer) {
        if (isLeft()) {
            consumer.accept(left);
        }
    }

    public void orElsePrintStackTrace() {
        if (isLeft()) {
            left.printStackTrace();
        }
    }


    public TryWrap<T> failWith(Exception e) {
        right = null;
        left = e;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TryWrap<?> tryWrap = (TryWrap<?>) o;
        return empty == tryWrap.empty &&
                Objects.equals(right, tryWrap.right) &&
                Objects.equals(left, tryWrap.left);
    }

    @Override
    public int hashCode() {
        return Objects.hash(right, left, empty);
    }

    private TryWrap(Exception left, T right) {
        this.right = right;
        this.left = left;
    }

}

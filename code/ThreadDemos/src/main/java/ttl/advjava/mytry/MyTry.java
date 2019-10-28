package ttl.advjava.mytry;

import java.util.Objects;

/**
 * @author whynot
 */
public abstract class MyTry<T> {

    public static <T> MyTry<T> of(MySupplier<T> supplier) {
        try {
            T t = supplier.get();
            return new Success(t);
        }catch(Exception e) {
           return new Failure(e);
        }
    }

    public abstract T get() throws Exception;
    public abstract boolean isSuccess() ;

    public abstract <R> MyTry<R> map(MyFunction<? super T, ? extends R> function);
    public abstract <R> MyTry<R> flatMap(MyFunction<? super T, MyTry<R>> function);

    public abstract boolean equals(Object o);

    public static <U> Success<U> asSuccess(U u) {
        return new Success<U>(u);
    }
    public static Failure<Exception> asFailure(Exception e) {
        return new Failure(e);
    }

    public static class Failure<T> extends MyTry<T> {
        private Exception exception;

        private Failure(Exception throwable) {
            this.exception = throwable;
        }

        @Override
        public T get() throws Exception {
            throw exception;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public <R> MyTry<R> map(MyFunction<? super T, ? extends R> function) {
            return new Failure(exception);
        }

        @Override
        public <R> MyTry<R> flatMap(MyFunction<? super T, MyTry<R>> function) {
            return new Failure(exception);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?> success = (Success<?>) o;
            return Objects.equals(exception, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exception);
        }
    }

    public static class Success<T> extends MyTry<T> {
        private T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public T get() throws Exception {
            return value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public <R> MyTry<R> map(MyFunction<? super T, ? extends R> function) {
            try {
                R r = function.apply(value);
                return new Success(r);
            }catch(Exception e) {
                return new Failure(e);
            }
        }

        @Override
        public <R> MyTry<R> flatMap(MyFunction<? super T, MyTry<R>> function) {
            try {
                MyTry<R> newVal = function.apply(value);
                return newVal;
            }catch(Exception e) {
                return new Failure(e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?> success = (Success<?>) o;
            return Objects.equals(value, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}

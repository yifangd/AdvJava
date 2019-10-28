package ttl.examples.book;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MatcherSpliterator implements Spliterator<String> {
    private Matcher matcher;

    public static Stream<String> stream(Matcher matcher) {
        Stream<String> stream = StreamSupport.stream(new MatcherSpliterator(matcher), false);
        return stream;
    }

    public MatcherSpliterator(Matcher matcher) {
        this.matcher = matcher;
    }
    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        if(matcher.find()) {
           action.accept(matcher.group());
           return true;
        }
        return false;
    }

    @Override
    public Spliterator<String> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}

package ttl.advjava.queues;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StateObject {
    //Can use to cause timeouts for stuck Connections.
    //But it uses a *LOT* of memory, linearly with the number of
    //connections
//	public static ScheduledExecutorService sExec = Executors.newSingleThreadScheduledExecutor();
    public int readDelay = 300; //seconds
    public TimeUnit timeUnit = TimeUnit.SECONDS;

    public SelectionKey key;
    public boolean error = false;
    public Instant startTime;
    public Instant endTime;
    public String clientId;
    //public ByteBuffer buffer;
    public int localPort;
    public int remotePort;
    public SocketChannel socketChannel;
    public String fileName;
    public long filePosition;
    public SeekableByteChannel fileChannel;
    //public FakeFileInputStream fileInputStream;
    public long totBytesWritten;
    public long totBytesReceived;

    public boolean isValid = true;

    private ScheduledFuture<?> timer;
    public CompletableFuture<Void> completer = new CompletableFuture<>();

    public StateObject() {
        this(null);
    }

    public StateObject(String clientId) {
        this.clientId = clientId;
    }

    public void errorComplete() {
        error = true;
        complete();
    }

    public void invalidate() {
        isValid = false;
    }

    public boolean isValid() {
        return isValid;
    }

    public void complete() {
        invalidate();
//		stopTimer();
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
                socketChannel = null;
            }
            if (fileChannel != null) {
                fileChannel.close();
                fileChannel = null;
            }
        } catch (IOException e) {
            System.err.println("SO.complete: Error closing SocketChannel " + socketChannel);
            e.printStackTrace();
        }
        endTime = Instant.now();
        completer.complete(null);

    }

    public void stopTimer() {
//		if(timer != null) {
//			timer.cancel(true);
//		}
    }

    public void resetTimer() {
//		stopTimer();
//		setTimer();
    }

    public void setTimer() {
//	    setTimer(readDelay, timeUnit);
    }

    public void setTimer(int delay, TimeUnit unit) {
//		this.readDelay = delay;
//		this.timeUnit = unit;
//		timer = sExec.schedule(() -> {
//			System.err.println("Got Time out for " + clientId +
//					", closing socket " + socketChannel);
//			complete();
//		}, delay, unit);
    }

    @Override
    public String toString() {
        return "StateObject{" +
                "clientId='" + clientId + '\'' +
                ", socketChannel=" + socketChannel +
                ", fileName='" + fileName + '\'' +
                ", totBytesWritten=" + totBytesWritten +
                ", totBytesReceived=" + totBytesReceived +
                '}';
    }

}

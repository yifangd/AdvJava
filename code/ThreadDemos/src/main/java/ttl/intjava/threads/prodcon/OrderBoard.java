package ttl.intjava.threads.prodcon;

public interface OrderBoard {
    void postOrder(Order toBeProcessed);

    Order cookOrder();
}

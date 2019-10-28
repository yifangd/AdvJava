package ttl.intjava.threads.prodcon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author developintelligence llc
 * @version 1.0
 */
public class LockOrderBoard implements OrderBoard {

	List<Order> orders;

	Lock fullLock = new ReentrantLock();
	Condition full = fullLock.newCondition();
	Condition empty = fullLock.newCondition();

	public LockOrderBoard() {
		orders = new ArrayList<Order>();
	}

	public void postOrder(Order toBeProcessed) {
		fullLock.lock();
		try {

			while (orders.size() == 5) {
				full.await();
			}
			orders.add(toBeProcessed);
			empty.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			fullLock.unlock();
		}

	}

	public Order cookOrder() {
		Order returnValue = null;
		fullLock.lock();
		try {
			while (orders.size() == 0) {
				empty.await();
			}
			returnValue = orders.remove(0);
			full.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			fullLock.unlock();
		}
		return returnValue;
	}

}



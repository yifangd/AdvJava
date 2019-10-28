package ttl.jcstress;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Mode;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.Signal;
import org.openjdk.jcstress.annotations.State;

/**
 * Test visibility problems.  We use jcstess in Termination Mode.
 * In this mode, we have code that loops or sleeps or blocks in
 * some way, and we want to test our mechanisms for catching it's
 * attention and telling it to stop doing whatever it is doing.
 *
 * In our case, the while loop in the @Actor is spinning on a
 * boolean variable.  The @Signal method set's the boolean variable
 * to true.  We are expecting that this will make the @Actor pop
 * out of the loop.  Our hopes are going to be cruelly dashed.
 *
 * To resurrect our hopes again, we need to declare 'ready' to be volatile.
 *
 * If the @Actor responds to the signal, an outcome of TERMINATED is recorded.
 * If the @Actor does NOT respond in , the thread is killed and some point and
 * an outcome of STALE is recorded.
 */
@JCStressTest(Mode.Termination)
@Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "All cool")
@Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@State
public class BadVisibility {

	private boolean ready;
	private int var;

	@Actor
	public void actor1() {
		while (!ready) {
		}
	}

	@Signal
	public void actor2() {
		var = 10;
		ready = true;
	}
}

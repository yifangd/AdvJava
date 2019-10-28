package ttl.jcstress;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

/**
 * See instruction reordering in action.  We run the jcstress test in
 * the default Continuous Mode
 *
 * We are using two @Actors and an II_Result (int, int)
 *
 * The 'id's in the @Outcome specifies a particular pair of results.
 * We are hoping to not see any 0, 0 results.  We are going to be
 * disappointed.
 *
 * Make X and Y volatile to fix the issue.
 */
@JCStressTest
@Outcome(id = "0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@Outcome(id = "0, 1", expect = Expect.ACCEPTABLE, desc = "All cool")
@Outcome(id = "1, 0", expect = Expect.ACCEPTABLE, desc = "All cool")
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE, desc = "All cool")
@State
public class ReorderingTest {

	int X, Y;

	@Actor
	public void actor1(II_Result r) {
		X = 1;
		r.r1 = Y;
	}

	@Actor
	public void actor2(II_Result r) {
		Y = 1;
		r.r2 = X;
	}
}
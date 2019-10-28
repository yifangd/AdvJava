package ttl.jcstress;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * A variation of ReorderingTest.
 * Here we are using an IIII_Result.  The values will be X, Y, A, B
 * And we are using regular expressions for id's.  They seem to work.
 *
 * Any outcomes where either X and Y are 0, or A and B are 0 mean
 * some reordering has happened
 */

@JCStressTest
@Outcome(id = "0, 0, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@Outcome(id = "0, 0, .*", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@Outcome(id = ".*, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@Outcome(id = ".*", expect = Expect.ACCEPTABLE, desc = "All cool")
/*
@Outcome(id = "1, 0, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@Outcome(id = "0, 1, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordered")
@Outcome(id = "0, 1, 0, 1", expect = Expect.ACCEPTABLE, desc = "All cool")
@Outcome(id = "1, 0, 0, 1", expect = Expect.ACCEPTABLE, desc = "All cool")
@Outcome(id = "0, 1, 1, 0", expect = Expect.ACCEPTABLE, desc = "All cool")
@Outcome(id = "1, 1, 1, 1", expect = Expect.ACCEPTABLE, desc = "All cool")
*/
@State
public class ReorderingTestRedux {

	volatile int X, Y;
	int A; 
	int B;

	@Actor
	public void actor1(IIII_Result r) {
		A = 1;
		//This store to volatile will prevent previous stores from
		//floating below (StoreStore)
		X = 1; 

		//Read from the volatile to get a fresh value for Y 
		//*and* for B  
		r.r4 = B;
		r.r2 = Y;
	}
	
	@Actor
	public void actor2(IIII_Result r) {
		B = 1;

		Y = 1;
		//volatile Store will prevent previous store (B = 1) from
		//floating below it

		r.r3 = A;
		r.r1 = X;
	}
}
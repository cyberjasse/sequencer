//to run this test in command line,
//java -cp .:../lib/junit-4.11.jar:../lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore sequencer.test.AlignmentTest
package sequencer.test;
import sequencer.Sequence;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.assertEquals;

public class AlignmentTest{

	@Test
	public void semiGlobalMatriceTest(){//The exemple in slide page 63. See https://moodle.umons.ac.be/mod/resource/view.php?id=2559
		Sequence s1 = new Sequence("cagcacttggattctcgg");
		Sequence s2 = new Sequence("cagcgtgg");
		int[][] a = s1.semiGlobalAlignment(s2);
		assertEquals(3,a[10][8]);
	}
}

//to 
//to run this test in command line,
//java -cp .:../lib/junit-4.11.jar:../lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore sequencer.test.AlignmentTest
package sequencer.test;
import sequencer.Sequence;
import sequencer.AlignmentPath;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class AlignmentTest{

	static private int[][] a;
	static private Sequence s1;
	static private Sequence s2;
	static private byte U=AlignmentPath.UP, L=AlignmentPath.LEFT, D=AlignmentPath.LEFT_UP;//

	private byte[] compact(AlignmentPath ap){
		byte[] newtab = new byte[ap.pathlength];
		for(int i=0 ; i<ap.pathlength ; i++){
			newtab[i] = ap.path[i];
		}
		return newtab;
	}

	@BeforeClass
	public static void beforeClass(){
		s1 = new Sequence("cagcacttggattctcgg");
		s2 = new Sequence("cagcgtgg");
		a = s1.semiGlobalAlignment(s2);//The exemple in slide page 63. See https://moodle.umons.ac.be/mod/resource/view.php?id=2559
	}

	@Test
	public void semiGlobalMatrixTest(){
		assertEquals(3,a[10][8]);
	}

	@Test
	public void pathfgTest(){
		AlignmentPath ap = s1.getAlignmentScore(s2)[0];
		byte[] expected = {D,D};
		assertArrayEquals(expected, compact(ap));
	}

	@Test
	public void deltafgTest(){
		AlignmentPath ap = s1.getAlignmentScore(s2)[0];
		assertEquals(16, ap.delta);
	}

	@Test
	public void pathgfTest(){
		AlignmentPath ap = s1.getAlignmentScore(s2)[1];
		byte[] expected = {D,D,D,D,D,U,D};//The exemple in slide page 63
		assertArrayEquals(expected, compact(ap));
	}

	@Test
	public void deltagfTest(){
		AlignmentPath ap = s1.getAlignmentScore(s2)[1];
		assertEquals(-4, ap.delta);
	}
}

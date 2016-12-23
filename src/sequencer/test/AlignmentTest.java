//to 
//to run this test in command line,
//java -cp .:../lib/junit-4.11.jar:../lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore sequencer.test.AlignmentTest
package sequencer.test;
import sequencer.Sequence;
import sequencer.Sequencer;
import sequencer.Alignment;
import sequencer.AlignmentPath;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class AlignmentTest{

	static private short[][] a;
	static private Sequence s1;
	static private Sequence s2;
	static private byte U=AlignmentPath.UP, L=AlignmentPath.LEFT, D=AlignmentPath.LEFT_UP;//

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
		int start;
		//search the position of the score in matrix
		AlignmentPath ap = s1.getAlignmentPath(s2);
		byte[] expected = {D,D};
		ap.compact();
		assertArrayEquals(expected, ap.path);
	}

	@Test
	public void deltafgTest(){
		AlignmentPath ap = s1.getAlignmentPath(s2);
		assertEquals(16, ap.delta);
	}

	@Test
	public void pathgfTest(){
		AlignmentPath ap = s1.getRevertedAlignmentPath(s2);
		byte[] expected = {D,D,D,D,D,U,D};//The exemple in slide page 63
		ap.compact();
		assertArrayEquals(expected, ap.path);
	}

	@Test
	public void deltagfTest(){
		AlignmentPath ap = s1.getRevertedAlignmentPath(s2);
		assertEquals(-4, ap.delta);
	}

	@Test
	public void alalTest(){
		Sequence s = new Sequence("GTCCC");
		Sequence t = new Sequence("GTACCA");
		/*
		G-TCCC
		G-TACCA
		*/
		Alignment a = new Alignment("G-TACCA", 0);
		assertEquals(a, Sequencer.getAlignment("G-TCCC", s, t, 0));
		s = new Sequence("ATCGTAAT");
		t = new Sequence("TAATGG");
		/*
		ATCGT-AAT
		    T-AATGG
		*/
		a = new Alignment("T-AATGG", 4);
		assertEquals(a, Sequencer.getAlignment("ATCGT-AAT", s, t, 0));
		s = new Sequence("ATCT");
		t = new Sequence("TATCTAAGG");
		/*
		 AT--CT
		TAT--CTAAGG
		*/
		a = new Alignment("TAT--CTAAGG", -1);
		assertEquals(a, Sequencer.getAlignment("AT--CT", s, t, 0));
		s = new Sequence("ATCGTAAT");
		t = new Sequence("TAATGG");
		/*
		A--TCGT-AAT
		 --   T-AATGG
		*/
		a = new Alignment("T-AATGG", 7);
		assertEquals(a, Sequencer.getAlignment("A--TCGT-AAT", s, t, 0));
	}
	
	@Test
	public void forConsensus1Test(){
		Sequence s1 = new Sequence("atcccggg");
		Sequence s2 = new Sequence("ccgggatat");
		/*
		ATCCCGGG
		   CCGGGATAT
		consensus should be "atcccgggatat"*/
		Alignment al = Sequencer.getAlignment(s1.toString(), s1, s2, 0);
		assertEquals("ccgggatat",al.aligned);
		assertEquals(3, getDelta(s1,s2));
	}

	@Test
	public void forConsensus2Test(){
		Sequence s2 = new Sequence("ccgggatat");
		Sequence s3 = new Sequence("atatcg");
		Alignment al = Sequencer.getAlignment(s2.toString(), s2, s3, 0);
		assertEquals("atatcg",al.aligned);
		assertEquals(5, getDelta(s2,s3));
	}

	private int getDelta(Sequence s1, Sequence s2){
		return Sequencer.getAlignment(s1.toString(), s1, s2, 0).delta;
	}
}

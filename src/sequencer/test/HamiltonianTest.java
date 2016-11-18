package sequencer.test;
import sequencer.Sequence;
import static sequencer.Sequencer.*;
import sequencer.AlignmentPath;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class HamiltonianTest{

	/**
	 * @param expected A list of identifier that we must find if we take the from and to of all expected edges, in order.
	 */
	private void assertEdges(int[] expected, List<Edge> got){
		if( expected.length/2 != got.size()){
			fail("Number of edges found in expected not the same that got.size()");
		}
		else{
			String message="";
			boolean fail = false;
			for(int i=0 ; i<got.size() ; i++){
				if( expected[i*2]!=got.get(i).from || expected[i*2+1]!=got.get(i).to){
					fail = true;
					message += "got[i] = ("+got.get(i).from+" -> "+got.get(i).to+") expected ("+expected[i*2]+" -> "+expected[i*2+1]+")\n";
				}
				i += 2;
			}
			assertFalse(message, fail);
		}
	}

	@Test
	public void simpleHamiltonianTest(){
		ArrayList<Sequence> l = new ArrayList<Sequence>();
		l.add( new Sequence("aaattt"));
		l.add( new Sequence("tttccc"));
		l.add( new Sequence("cccggg"));
		List<Edge> result = hamiltonian(allEdges(l,1), l.size());
		assertEdges( new int[]{0,1,1,2} , result);
	}

	@Test
	public void complementaryHamiltonianTest(){
		ArrayList<Sequence> l = new ArrayList<Sequence>();
		l.add( new Sequence("tttt"));
		l.add( new Sequence("gaaaa"));
		l.add( new Sequence("ca"));
		List<Edge> result = hamiltonian(allEdges(l,1), l.size());
		assertEdges( new int[]{0,4,4,2} , result);
	}
}

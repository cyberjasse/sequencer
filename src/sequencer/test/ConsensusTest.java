package sequencer.test;
import sequencer.Sequence;
import sequencer.Sequencer;
import sequencer.Alignment;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ConsensusTest{

	/**
	 * @param end The index of the last node
	 * Build the path automatically from 0 to end in order
	 */
	private List<Sequencer.Edge> buildPath(int end){
		List<Sequencer.Edge> path = new ArrayList<>(end);
		short w=0;
		for(int i=0 ; i<end ; i++){
			path.add(new Sequencer.Edge(i,i+1,w));
		}
		return path;
	}

	@Test
	public void verySimpleConsensusTest(){
		List<Sequence> frags = new ArrayList<>(2);
		frags.add( new Sequence("atcccggg"));
		frags.add( new Sequence("ccgggatat"));
		/*
		ATCCCGGG
		   CCGGGATAT
		consensus should be "atcccgggatat"*/
		Sequence consensus = Sequencer.getConsensus(frags,buildPath(1));
		assertEquals("atcccgggatat",consensus.toString());
	}

	@Test
	public void simpleConsensusTest(){
		List<Sequence> frags = new ArrayList<>(3);
		frags.add( new Sequence("atcccggg"));
		frags.add( new Sequence("ccgggatat"));
		frags.add( new Sequence("atatcg"));
		/*
		ATCCCGGG
		   CCGGGATAT
		        ATATCG
		consensus should be "atcccgggatatcg"*/
		Sequence consensus = Sequencer.getConsensus(frags,buildPath(2));
		assertEquals("atcccgggatatcg",consensus.toString());
	}

	@Test
	public void simpleInclusionTest(){
		List<Sequence> frags = new ArrayList<>(2);
		frags.add( new Sequence("atcccggg"));
		frags.add( new Sequence("atcccgggatat"));
		Sequence consensus = Sequencer.getConsensus(frags,buildPath(1));
		assertEquals("atcccgggatat",consensus.toString());
	}

	@Test
	public void simpleRevertedTest(){
		List<Sequence> frags = new ArrayList<>(2);
		frags.add( new Sequence("atatcg"));
		frags.add( new Sequence("atcgcg").getComplementary());
		List<Sequencer.Edge> path = new ArrayList<>(1);
		short w=0;
		path.add(new Sequencer.Edge(0,1+2,w));
		Sequence consensus = Sequencer.getConsensus(frags,path);
		assertEquals("atatcgcg",consensus.toString());
	}

	@Test
	public void inclusionTest(){
		List<Sequence> frags = new ArrayList<>(5);
		frags.add( new Sequence("atcccgatcg"));
		frags.add( new Sequence("atcgat"));
		frags.add( new Sequence("cgatcgatgg"));
		frags.add( new Sequence("gatggccccat"));
		frags.add( new Sequence("ccccatgg"));
		Sequence consensus = Sequencer.getConsensus(frags,buildPath(4));
		assertEquals("atcccgatcgatggccccatgg",consensus.toString());
	}
}

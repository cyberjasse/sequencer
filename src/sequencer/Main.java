package sequencer;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
	private static String collection = null;

	private static void usage(int exit) {
		System.err.println("java -jar sequencer.jar i --out o --out-ic p");
		System.exit(exit);
	}

	public static void savePath(List<Sequencer.Edge> edges, String filename)
			throws FileNotFoundException, IOException {
		PrintWriter output = new PrintWriter(filename);
		boolean first = true;
		for (Sequencer.Edge e: edges) {
			if (first) {
				output.println(e.from);
				first = false;
			}
			output.println(e.to);
			//discard its weight
		}
		output.close();
	}

	public static List<Sequencer.Edge> loadPath(String name)
			throws FileNotFoundException, IOException {
		List<Sequencer.Edge> edges = new ArrayList<>();
		BufferedReader input = new BufferedReader(new FileReader(name));
		String line = input.readLine();
		if (line == null)
			throw new RuntimeException("Empty edge dump!");
		int from = Integer.parseInt(line);
		do {
			line = input.readLine();
			if (line != null) {
				int to = Integer.parseInt(line);
				edges.add(new Sequencer.Edge(from, to, (short)42));
				from = to;
			}
		} while (line != null);
		return edges;
	}

	public static void main(String args[]){
		String input=null, in_ham=null;
		String out=null, out_ic=null, out_ham=null;
		if (args.length == 1) {
			if (args[0].equals("--help") || args[0].equals("-h"))
				usage(0);
			else {
				input = args[0];
				String stripped = input.substring(0, input.lastIndexOf('.'));
				System.out.println(stripped);
				out = stripped+".out.fasta";
				in_ham = out_ham = stripped+".ham";
				out_ic = stripped+".ic.fasta";
			}
		}
		else {
			for (int i=0; i<args.length; i++) {
				if (args[i].startsWith("--") && i<args.length-1) {
					if (args[i].equals("--out") && out==null)
						out = args[++i];
					else if (args[i].equals("--out-ic") && out_ic==null)
						out_ic = args[++i];
					else if (args[i].equals("--out-ham") && out_ham==null)
						out_ham = args[++i];
					else if (args[i].equals("--in-ham") && in_ham==null)
						in_ham = args[++i];
					else
						usage(1);
				}
				else if (input == null)
					input = args[i];
				else
					usage(1);
			}
			if (input==null || out==null || out_ic==null)
				usage(1);
		}
		try {
			System.out.print("Loading fragments...");
			MyFasta fin = new MyFasta();
			List<Sequence> fragments = fin.load(input);
			System.out.println(" loaded "+fragments.size()+" fragments.");
			List<Sequencer.Edge> path = null;
			if (in_ham != null) {
				System.out.println("Loading the Hamiltonian path...");
				try {
					path = loadPath(in_ham);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (path!=null && path.size()!=fragments.size()-1) {
					System.err.println("The path length doesn't match the collection size.");
					System.exit(2);
				}
			}
			if (path == null) {
				int n = 4 * fragments.size() * fragments.size() - fragments.size();
				System.out.println("Computing "+n+" edges...");
				List<Sequencer.Edge> edges = Sequencer.allEdges(fragments, Runtime.getRuntime().availableProcessors());
				System.out.println("Computing the Hamiltonian path...");
				path = Sequencer.hamiltonian(edges, fragments.size());
				if (out_ham != null) {
					System.out.println("Saving the Hamiltonian path...");
					savePath(path, out_ham);
				}
			}
			System.out.println("Computing the consensus...");
			Sequence consensus = Sequencer.getConsensus(fragments, path);
			fin.save(consensus, out);
			fin.save(consensus.getComplementary(), out_ic);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class MyFasta extends Fasta {
		protected String collection;

		protected void initHeader() {
			collection = null;
		}

		protected void handleHeader(String h) {
			if (collection == null) {
				int p = h.lastIndexOf(' ');
				collection = h.substring(p+1);
			}
		}

		public void save(Complementary seq, String filename)
				throws FileNotFoundException, IOException {
			String header = "Groupe-5 Collection "+collection;
			header += " Longueur "+seq.length()+" (IC)";
			save(seq, filename, header);
		}

		public void save(Sequence seq, String filename)
				throws FileNotFoundException, IOException {
			String header = "Groupe-5 Collection "+collection;
			header += " Longueur "+seq.length();
			save(seq, filename, header);
		}
	}
}

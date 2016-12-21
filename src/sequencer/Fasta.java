package sequencer;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Fasta {
	private static final int MAX_LINE = 80;

	/**
	 * Stores a save a String in a fasta file
	 * @param sequence The sequence to save
	 * @param filename The name of the file without the '.fasta' sufix.
	 * @param header The number of the collection
	 */
	public static void save(Sequence seq, String filename, String header)
			throws FileNotFoundException, IOException {
		PrintWriter output = new PrintWriter(filename);
		int len = seq.length();
		output.println(">"+header);
		int i;
		for (i=0; i<len-MAX_LINE; i+=MAX_LINE) {
			//we have yet at least 80 characters to print
			for (int j=0; j<MAX_LINE; j++)
				output.print(seq.get(i+j));
			output.print('\n');
		}
		//print the last line
		while (i < len)
			output.print(seq.get(i++));
		output.print('\n');
		//done
		output.close();
	}

	protected abstract void initHeader();
	protected abstract void handleHeader(String h);

	/**
	 * Load a fasta file. The inverted complementary of these fragments aren't
	 * included but you can get them using #Sequence.getComplementary().
	 * @param name The path of the file
	 * @return A list of loaded fragments.
	 */
	public List<Sequence> load(String name)
			throws FileNotFoundException, IOException {
		List<Sequence> fragments = new ArrayList<Sequence>();
		BufferedReader input = new BufferedReader(new FileReader(name));
		StringBuilder current_sequence = new StringBuilder();
		int c;
		initHeader();
		do {
			c = input.read();
			if (c==-1 || c == '>') {
				if (current_sequence.length() > 0) {
					fragments.add(new Sequence(current_sequence.toString()));
					current_sequence = new StringBuilder();
				}
				if (c == '>')
					handleHeader(input.readLine());
			}
			else {
				current_sequence.append((char)c);
				current_sequence.append(input.readLine());
			}
		} while (c != -1);
		return fragments;
	}
}

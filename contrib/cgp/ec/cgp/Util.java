package ec.cgp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;

/**
 * 
 * Miscelanneous utility methods used by CGP.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class Util {
	/**
	 * Return the int in the range [0,max-1] represented by the given float
	 * which is in the range [0.0,1.0).
	 * 
	 * @param val
	 *            float value to scale
	 * @param max
	 *            the maximum integer to generate
	 * @return scaled integer
	 */
	public static int scale(float val, int max) {
		if (val == 1)
			return max - 1;
		return (int) (val * max);
	}

	/**
	 * Read lines from the given file.
	 * @param aFile File to read
	 * @return All lines read from the given file.
	 */
	static public List<String> readFile(File aFile) {
		List<String> contents = new ArrayList<String>();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(aFile));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (!line.startsWith("@") && !line.startsWith("%")
						&& !"".equals(line)) {
					contents.add(line);
				}
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return contents;
	}

	public static void test() {
		MersenneTwisterFast rand = new MersenneTwisterFast();

		int max;
		float val;
		for (int i = 0; i < 100; i++) {
			max = rand.nextInt(10);
			val = rand.nextFloat();
			System.out.println("scale(" + val + "," + max + ") = "
					+ scale(val, max));
		}
		System.out.println("scale(" + 0.9999f + "," + 5 + ") = "
				+ scale(0.9999f, 5));
		System.out.println("scale(" + 1.0f + "," + 5 + ") = " + scale(1.0f, 5));

	}

	public static void computeResults() {
		float[] totals = new float[2500];
		int max = 0;
		for (int x = 0; x < 1000; x++) {
			List<String> lines = readFile(new File("reg2-converge-" + x));
			for (int i = 0; i < lines.size(); i++) {
				totals[i] += Float.valueOf(lines.get(i).split(" ")[1]);
				max = Math.max(max, i);
			}
		}

		for (int i = 0; i <= max; i++) {
			System.out.println(totals[i] / 1000);
		}
	}

	public static void main(String[] args) {
		test();
	}
}

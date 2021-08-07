package adver.sarius.albion.mpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ItemNameParser {

	private ItemNameParser() {
	}

	// https://github.com/broderickhyman/ao-bin-dumps/blob/master/formatted/items.txt
	// expects lines in format:
	// <number>:<uniquename>[:<display name>]
	public static Map<String, String> parseInputFile(String inputFilePath) {
		Map<String, String> results = new HashMap<>();
		Scanner myReader;
		try {
			myReader = new Scanner(new File(inputFilePath));
			while (myReader.hasNextLine()) {
				String line = myReader.nextLine();
				String[] splitted = line.split(":", 3);
				if (splitted.length == 2) {
					results.put(splitted[1].trim(), "");
				} else if (splitted.length == 3) {
					results.put(splitted[1].trim(), splitted[2].trim());
				} else {
					System.out.println("Found unparsable line: " + line);
				}
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found! " + e);
		}
		return results;
	}
}
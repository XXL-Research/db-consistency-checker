package de.htwk.imn.consistencychecker;

import org.apache.commons.cli.ParseException;

import de.htwk.imn.consistencychecker.utils.ConfigurationParser;
import de.htwk.imn.consistencychecker.utils.ConsistencyCheckerUtils;
import de.htwk.imn.consistencychecker.utils.SelectedOptions;

public class Main {

	public static void main(String[] args) {

		try {
			if (args.length == 0) {
				throw new ParseException("No configuration file found. Pass a configuration file as argument.");
			}

			ConfigurationParser configParser = new ConfigurationParser();
			SelectedOptions selectedOptions;
			selectedOptions = configParser.parse(args[0]);

			ConsistencyCheckerUtils utils = new ConsistencyCheckerUtils(selectedOptions);
			utils.setUpDatabase();
			utils.runConsistencyCheck();

		} catch (ParseException e) {
			System.out.print("Parse error: " + e.getMessage());
			System.exit(1);
		}

	}

}

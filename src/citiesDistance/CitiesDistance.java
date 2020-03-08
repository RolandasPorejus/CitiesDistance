package citiesDistance;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * The main class responsible for running the initial City data import into the
 * database and allowing the user to input the coordinates, which are used to
 * find the cities closest to them.
 * 
 * @author Rolandas
 *
 */
public class CitiesDistance {
	private final static String DEFAULT_CSV = "input/500_europe_cities.csv";
	private final static Config config = new Config();
	static Scanner in = new Scanner(System.in);

	public static void main(String[] args) {
		if (config.getKey().equals("")) {
			System.out.println("Key parameter in the configuration file (config/config.cfg) isn't present. Please"
					+ " provide a valid Google API key.");
			System.exit(3);
		}
		String csvFilename = InputConfigUI();
		DataImport(csvFilename);
		ConsoleUI();
	}

	/**
	 * Main console UI method, which allows the user to input the coordinates and
	 * use them to calculate distance to cities
	 */
	private static void ConsoleUI() {
		String input = "";
		Double longitude;
		Double latitude;

		DistanceCalculator calc = DistanceCalculator.getInstance();
		calc.setLimit(config.getLimit());
		calc.setSearchRadius(config.getSearchRadius());

		while (input.toLowerCase() != "q") {
			System.out.println("Please enter the coordinates of the point (q to quit):");
			System.out.print("Longitude: ");
			while (!in.hasNextDouble()) {
				input = in.nextLine();
				if (input.toLowerCase().equals("q")) {
					in.close();
					Runtime.getRuntime().halt(0);
				} else {
					System.out.println("");
				}
			}
			longitude = in.nextDouble();

			System.out.print("Latitude: ");
			while (!in.hasNextDouble()) {
				input = in.nextLine();
				if (input.toLowerCase().equals("q")) {
					in.close();
					Runtime.getRuntime().halt(0);
				} else {
					System.out.println("");
				}
			}
			latitude = in.nextDouble();

			Coordinates userCoord = new Coordinates(longitude, latitude);
			calc.setCoordinates(userCoord);
			calc.calculateDistance();
		}

		in.close();
	}

	/**
	 * Allows the user to choose the input file
	 * 
	 * @return The filename of the input CSV
	 */
	private static String InputConfigUI() {
		boolean useDefault = true;
		String input = "";

		System.out.println("Use the default city input file " + DEFAULT_CSV + "? (Y/N)");
		while (in.hasNext()) {
			input = in.nextLine();
			if (input.toLowerCase().equals("y")) {
				useDefault = true;
				break;
			} else if (input.toLowerCase().equals("n")) {
				useDefault = false;
				break;
			}
		}
		//in.nextLine();

		String csvFilename = "";
		if (useDefault) {
			csvFilename = DEFAULT_CSV;
		} else {
			System.out.println("Input the local path to the CSV file: ");
			while (in.hasNext()) {
				input = in.nextLine();
				File csvDir = new File(input);
				boolean fileExists = csvDir.exists();

				if (fileExists) {
					csvFilename = input;
					break;
				} else {
					System.out.printf("\"%s\" doesn't exist.\n", input);
					System.out.println("Input the local path to the CSV file: ");
				}
			}
		}

		return csvFilename;
	}

	/**
	 * Imports the City data using the specified CSV file
	 * 
	 * @param csvFilename Filename of the city CSV file
	 */
	private static void DataImport(String csvFilename) {
		CitiesDataImport cdi = new CitiesDataImport(config.getKey());
		try {
			cdi.ReadCSV(csvFilename);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error while importing the city data");
			System.exit(1);
		}

		try {
			cdi.PersistData();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while saving the City data to the database.");
			System.exit(2);
		}
	}

}

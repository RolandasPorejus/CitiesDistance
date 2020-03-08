package citiesDistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;

/**
 * The class responsible for reading a specified input CSV with
 * "cityName;cityCountry" format The data is used to download Google Maps APIs
 * XML data, which is subsequently parsed and added to the city list. The data
 * isn't immediately added to the database in order to minimize I/O performance
 * costs and instead is only added to the database when PersistData() is called.
 * 
 * @author Rolandas
 *
 */

public class CitiesDataImport {
	String filename;
	String path;
	private String key;

	// List of cities to be added to the database
	List<City> cityList;
	EntityManagerFactory factory;
	EntityManager entityManager;
	Source xsdSchema;

	CitiesDataImport(String key) {
		cityList = new ArrayList<City>();
		factory = Persistence.createEntityManagerFactory("CitiesDistance");
		entityManager = factory.createEntityManager();
		xsdSchema = new StreamSource(new File("input/CityResponseSchema.xsd"));
		setKey(key);
	}

	/**
	 * Reads the specified CSV. It is checked if the city's record is already
	 * present in the database before downloading the data. Also, the rows with the
	 * same City name as the Country name are skipped, since it downloads the
	 * Country's data, which has much larger bounds and overlays the bounds of every
	 * city inside (Example: Albania;Albania)
	 * 
	 * @param path
	 * @throws IOException
	 */
	public void ReadCSV(String path) throws IOException {
		String row;
		BufferedReader csvReader = new BufferedReader(new FileReader(path));
		for (int i = 0; (row = csvReader.readLine()) != null; i++) {
			if (i != 0) {
				String[] data = row.split(";");

				String city = data[0];
				String country = data[1];

				if (!city.contentEquals(country)) {
					if (!RecordExistsInDB(city, country)) {
						System.out.println("(" + i + "/?) " + "Downloading city data from Google Maps API: [" + city
								+ ";" + country + "]");
						GetGoogleCityData(city, country);
					} else {
						System.out.println("(" + i + "/?) " + "Record for [" + city + ";" + country
								+ "] already exists in the database. Skipping.");
					}
				} else {
					System.out.println("(" + i + "/?) " + "City name " + city + " = " + country
							+ ". Assuming country, not city data. Skipping.");
				}
			}
		}
		csvReader.close();
	}

	/**
	 * Download the city data XML from Google Maps API. It is necessary to have a
	 * valid API key for this method to work.
	 * 
	 * @param cityName
	 * @param countryName
	 */
	private void GetGoogleCityData(String cityName, String countryName) {
		String baseURL = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
		String countryURL = "&components=country:";
		String keyURL = "&key=";

		String fullURL = baseURL + cityName + countryURL + countryName + keyURL + key;

		StringBuffer content = new StringBuffer();
		content = DownloadCityData(fullURL);

		boolean valid = ValidateCityXML(content, cityName, countryName);
		if (valid) {
			ParseCityXML(content, cityName, countryName);
		} else {
			System.out.println("Response XML isn't valid. Trying URL with the city name only."); 	
			fullURL = baseURL + cityName + keyURL + key;
			content = DownloadCityData(fullURL);
			valid = ValidateCityXML(content, cityName, countryName);
			if (valid) {
				ParseCityXML(content, cityName, countryName);
			} else {
			System.out.println(
					"Response XML isn't valid. Skipping " + cityName + "(" + countryName + ")" + " addition to DB.");
			}
		}
	}
	
	/**
	 * Downloads the from the specified URL
	 * @param url
	 * @return The XML string of Google API response
	 */
	private StringBuffer DownloadCityData(String url) {
		StringBuffer content = new StringBuffer();
		URL osm;
		try {
			osm = new URL(url.replace(" ", "%20"));
			HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
			connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF8"));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine + "\n");
			}
			in.close();
			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error while downloading City data");
		}
		return content;
	}

	/**
	 * Validates the XML for fields required for City object creation
	 * 
	 * @param xml
	 * @param cityName
	 * @param countryName
	 * @return XML is valid if true, not valid if false
	 */
	private boolean ValidateCityXML(StringBuffer xml, String cityName, String countryName) {
		Source streamSource = new StreamSource(new StringReader(xml.toString()));

		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(xsdSchema);
			Validator validator = schema.newValidator();
			validator.validate(streamSource);
		} catch (SAXException e) {
			System.out.println(cityName + "(" + countryName + ")" + "XML response is not valid. Reason:" + e);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Parse the XMLdata to create a City object and add it to the city list
	 * 
	 * @param xml
	 * @param cityName
	 * @param countryName
	 */
	private void ParseCityXML(StringBuffer xml, String cityName, String countryName) {
		try {
			StringReader reader = new StringReader(xml.toString());
			InputSource is = new InputSource(reader);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);

			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			XPathExpression expr;

			String uuid;
			Location location;
			Bounds cityBounds;
			Coordinates coordNE, coordSW;
			double latitude, longitude;

			expr = xpath.compile("//result/place_id/text()");
			uuid = GetXMLNodeData(doc, expr).toString();

			expr = xpath.compile("//result/geometry/location/lat/text()");
			latitude = Double.parseDouble(GetXMLNodeData(doc, expr));

			expr = xpath.compile("//result/geometry/location/lng/text()");
			longitude = Double.parseDouble(GetXMLNodeData(doc, expr));

			location = new Location(latitude, longitude);

			String boundsStr;
			// City XML sometimes has Viewport instead of Bounds so it's important to
			// account for that
			expr = xpath.compile("//result/geometry/bounds");
			if (GetXMLNodeExists(doc, expr)) {
				boundsStr = "bounds";
			} else {
				boundsStr = "viewport";
			}

			expr = xpath.compile("//result/geometry/" + boundsStr + "/northeast/lat/text()");
			latitude = Double.parseDouble(GetXMLNodeData(doc, expr));

			expr = xpath.compile("//result/geometry/" + boundsStr + "/northeast/lng/text()");
			longitude = Double.parseDouble(GetXMLNodeData(doc, expr));

			coordNE = new Coordinates(latitude, longitude);

			expr = xpath.compile("//result/geometry/" + boundsStr + "/southwest/lat/text()");
			latitude = Double.parseDouble(GetXMLNodeData(doc, expr));

			expr = xpath.compile("//result/geometry/" + boundsStr + "/southwest/lng/text()");
			longitude = Double.parseDouble(GetXMLNodeData(doc, expr));

			coordSW = new Coordinates(latitude, longitude);

			cityBounds = new Bounds(coordNE, coordSW);

			City city = new City(uuid, cityName, countryName, cityBounds, location);
			cityList.add(city);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while parsing the XML data");
		}
	}

	private String GetXMLNodeData(Document doc, XPathExpression xpath) {
		Object result = null;
		try {
			result = xpath.evaluate(doc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			System.out.println("Error evaluating xpath:" + xpath.toString());
			e.printStackTrace();
		}
		Node node = (Node) result;
		if (node != null) {
			return node.getNodeValue();
		} else {
			return null;
		}
	}

	private boolean GetXMLNodeExists(Document doc, XPathExpression xpath) {
		Object result = null;
		try {
			result = xpath.evaluate(doc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			System.out.println("Error evaluating xpath:" + xpath.toString());
			e.printStackTrace();
		}
		Node node = (Node) result;
		if (node != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if a city with specified name and country already exists in the
	 * database
	 * 
	 * @param cityName
	 * @param countryName
	 * @return
	 */
	private boolean RecordExistsInDB(String cityName, String countryName) {
		String queryStr = "SELECT Count(c) FROM City c WHERE c.name = :cityName AND c.country = :countryName";
		Query query = entityManager.createQuery(queryStr);
		query.setParameter("cityName", cityName);
		query.setParameter("countryName", countryName);
		Long count = (Long) query.getSingleResult();
		return ((count.equals(0L)) ? false : true);
	}

	/**
	 * Save the city data to the database using the city list
	 */
	public void PersistData() {
		entityManager.getTransaction().begin();

		for (City city : cityList) {
			entityManager.persist(city);
		}

		entityManager.getTransaction().commit();
		cityList.clear();
		entityManager.clear();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}

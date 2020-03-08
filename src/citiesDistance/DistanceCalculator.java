package citiesDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * This class is responsible for calculating the distance between a point and
 * the bounds of a city and printing a list of the closest cities (10 by
 * default). The search radius and limit parameter in the configuration file
 * allow to modify the behavior of this class.
 * 
 * @author Rolandas
 *
 */

public class DistanceCalculator {
	private static DistanceCalculator instance;
	private Coordinates coordinates;
	private int searchRadius;
	private int limit;
	final int EARTH_RADIUS = 6371; // Earth's mean radius in KM

	EntityManagerFactory factory;
	EntityManager entityManager;

	private DistanceCalculator() {
		factory = Persistence.createEntityManagerFactory("CitiesDistance");
		entityManager = factory.createEntityManager();
	}

	public static DistanceCalculator getInstance() {
		if (instance == null)
			instance = new DistanceCalculator();

		return instance;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	public void calculateDistance(double longitude, double latitude) {
		setCoordinates(new Coordinates(longitude, latitude));
		calculateDistance();
	}

	/**
	 * This method uses the database to find and print a list of the closest cities
	 * to a coordinate or simply print that a coordinate is inside a city's bounds.
	 */
	/**
	 * 
	 */
	/**
	 * 
	 */
	public void calculateDistance() {
		double lon = coordinates.getLongitude(); // your longitude
		double lat = coordinates.getLatitude(); // your latitude

		String distanceQueryString = "SELECT loc, "
				+ "(:earthRadius * FUNC('acos', FUNC('cos', FUNC('radians', :latitude)) * "
				+ "FUNC('cos', FUNC('radians', loc.latitude)) * "
				+ "FUNC('cos', FUNC('radians', loc.longitude) - FUNC('radians', :longitude)) + "
				+ "FUNC('sin', FUNC('radians', :latitude)) * FUNC('sin', FUNC('radians', loc.latitude)) ) ) AS distance "
				+ "FROM Location loc "
				+ "WHERE ((:earthRadius * FUNC('acos', FUNC('cos', FUNC('radians', :latitude)) * "
				+ "FUNC('cos', FUNC('radians', loc.latitude)) * "
				+ "FUNC('cos', FUNC('radians', loc.longitude) - FUNC('radians', :longitude)) + "
				+ "FUNC('sin', FUNC('radians', :latitude)) * FUNC('sin', FUNC('radians', loc.latitude)) ) )<= :searchRadius) "
				+ "ORDER BY distance";

		Query query = entityManager.createQuery(distanceQueryString);
		query.setParameter("latitude", lat);
		query.setParameter("longitude", lon);
		query.setParameter("searchRadius", searchRadius);
		query.setParameter("earthRadius", EARTH_RADIUS);
		query.setMaxResults(limit);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = query.getResultList();
		List<Location> sortedLocations = new ArrayList<>(rows.size());
		for (Object[] row : rows) {
			sortedLocations.add((Location) row[0]);
		}

		City city;
		Map<City, Double> unsortedMap = new HashMap<>();

		for (Location entity : sortedLocations) {
			city = entity.getCity();
			double distance = getDistanceFromBounds(new Coordinates(lat, lon), city.getBounds());
			unsortedMap.put(city, distance);
		}

		Map<City, Double> sortedMap = unsortedMap.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		for (Map.Entry<City, Double> entry : sortedMap.entrySet()) {
			if (entry.getValue() == 0.0) {
				System.out.println("Point is inside the bounds of " + entry.getKey().getName() + "("
						+ entry.getKey().getCountry() + ")");
				break;
			} else {
				System.out.printf("%.4f", entry.getValue());
				System.out.println(" km. - " + entry.getKey().getName() + "(" + entry.getKey().getCountry() + ")");
			}
		}
	}

	/**
	 * @param point  Coordinates to be used to calculate the distance to the City
	 *               bounds
	 * @param bounds Bounds of a city
	 * @return Returns 0 if the point is inside City bounds
	 */
	double getDistanceFromBounds(Coordinates point, Bounds bounds) {
		double closestLon, closestLat;
		if (point.getX() < bounds.getSouthWest().getX()) {
			closestLon = bounds.getSouthWest().getX();
			if (point.getY() < bounds.getSouthWest().getY()) {
				closestLat = bounds.getSouthWest().getY();
			} else if (point.getY() > bounds.getNorthEast().getY()) {
				closestLat = bounds.getNorthEast().getY();
			} else {
				closestLat = point.getY();
			}
		} else if (point.getX() > bounds.getNorthEast().getX()) {
			closestLon = bounds.getNorthEast().getX();
			if (point.getY() < bounds.getSouthWest().getY()) {
				closestLat = bounds.getSouthWest().getY();
			} else if (point.getY() > bounds.getNorthEast().getY()) {
				closestLat = bounds.getNorthEast().getY();
			} else {
				closestLat = point.getY();
			}
		} else {
			closestLon = point.getX();
			if (point.getY() < bounds.getSouthWest().getY()) {
				closestLat = bounds.getSouthWest().getY();
			} else if (point.getY() > bounds.getNorthEast().getY()) {
				closestLat = bounds.getNorthEast().getY();
			} else {
				return 0.0; // return 0 if the point is inside the bounding rectangle
			}
		}

		double latRad = Math.toRadians(point.getLatitude());
		double closestLatRad = Math.toRadians(closestLat);
		double lonRad = Math.toRadians(point.getLongitude());
		double closestLonRad = Math.toRadians(closestLon);

		double diffLat = closestLatRad - latRad;
		double diffLon = closestLonRad - lonRad;

		// Haversine formula
		double a = Math.pow(Math.sin(diffLat / 2), 2)
				+ Math.cos(latRad) * Math.cos(closestLatRad) * Math.pow(Math.sin(diffLon / 2), 2);

		double c = 2 * Math.asin(Math.sqrt(a));
		double distance = EARTH_RADIUS * c;
		return distance;
	}

	public int getSearchRadius() {
		return searchRadius;
	}

	public void setSearchRadius(int searchRadius) {
		this.searchRadius = searchRadius;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}

package citiesDistance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * Represents the location of a city
 * 
 * @author Rolandas
 *
 */

@Entity
public class Location {
	private double latitude;
	private double longitude;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@OneToOne
	private City city;

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	Location(double latitude, double longitude) {
		super();
		this.setLatitude(latitude);
		this.setLongitude(longitude);
	}

	Location() {
		super();
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getX() {
		return this.longitude;
	}

	public double getY() {
		return this.latitude;
	}

	@Override
	public String toString() {
		return "Location [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}

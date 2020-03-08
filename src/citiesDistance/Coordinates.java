package citiesDistance;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Represents the entity of coordinates
 * 
 * @author Rolandas
 *
 */

@Entity
public class Coordinates {
	private double latitude;
	private double longitude;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@ManyToOne(fetch = FetchType.LAZY)
	private Bounds bounds;

	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}

	Coordinates(double latitude, double longitude) {
		super();
		this.setLatitude(latitude);
		this.setLongitude(longitude);
	}

	Coordinates() {
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
		return "Coordinates [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}

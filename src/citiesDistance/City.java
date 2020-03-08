package citiesDistance;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Represents the City entity with its location, bounds, name and country
 * 
 * @author Rolandas
 */

@Entity
@Table
public class City {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String placeId;
	private String name;
	private String country;

	@OneToOne(mappedBy = "city", cascade = CascadeType.PERSIST)
	private Bounds bounds;
	@OneToOne(mappedBy = "city", cascade = CascadeType.PERSIST)
	private Location location;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	City(String name, String country) {
		super();
		this.setName(name);
		this.setCountry(country);
	}

	City(String placeId, String name, String country) {
		super();
		this.placeId = placeId;
		this.setName(name);
		this.setCountry(country);
	}

	City(String placeId, String name, String country, Bounds bounds, Location location) {
		super();
		this.placeId = placeId;
		this.setName(name);
		this.setCountry(country);
		bounds.setCity(this);
		this.setBounds(bounds);
		location.setCity(this);
		this.setLocation(location);
	}

	City() {
		super();
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		bounds.setCity(this);
		this.bounds = bounds;
	}

	@Override
	public String toString() {
		return "City [id=" + id + ", placeId=" + placeId + ", name=" + name + ", country=" + country + ", bounds=" + bounds.toString()
				+ ", location=" + location.toString() + "]";
	}
}

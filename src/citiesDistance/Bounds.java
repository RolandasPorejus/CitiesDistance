package citiesDistance;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

/**
 * Represents the rectangular bounds of the city
 * 
 * @author Rolandas
 *
 */

@Entity
public class Bounds {
	private final int SOUTH_WEST = 0;
	private final int NORTH_EAST = 1;

	@OneToMany(mappedBy = "bounds", cascade = CascadeType.PERSIST, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<Coordinates> coordinates = new ArrayList<>();

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

	Bounds(Coordinates northEast, Coordinates southWest) {
		super();
		southWest.setBounds(this);
		coordinates.add(southWest);
		northEast.setBounds(this);
		coordinates.add(northEast);
	}

	Bounds() {
		super();
	}

	public Coordinates getSouthWest() {
		return coordinates.get(SOUTH_WEST);
	}

	public void setSouthWest(Coordinates southWest) {
		southWest.setBounds(this);
		coordinates.set(SOUTH_WEST, southWest);
	}

	public Coordinates getNorthEast() {
		return coordinates.get(NORTH_EAST);
	}

	public void setNorthEast(Coordinates northEast) {
		northEast.setBounds(this);
		coordinates.set(NORTH_EAST, northEast);
	}

	@Override
	public String toString() {
		return "Bounds [northEast=" + getNorthEast().toString() + ", southWest=" + getSouthWest().toString() + "]";
	}
}

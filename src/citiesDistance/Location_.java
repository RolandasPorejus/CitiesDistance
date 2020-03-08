package citiesDistance;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-03-05T22:01:05.492+0200")
@StaticMetamodel(Location.class)
public class Location_ {
	public static volatile SingularAttribute<Location, Double> latitude;
	public static volatile SingularAttribute<Location, Double> longitude;
	public static volatile SingularAttribute<Location, Integer> id;
	public static volatile SingularAttribute<Location, City> city;
}

package citiesDistance;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-03-08T16:46:57.925+0200")
@StaticMetamodel(City.class)
public class City_ {
	public static volatile SingularAttribute<City, Integer> id;
	public static volatile SingularAttribute<City, String> placeId;
	public static volatile SingularAttribute<City, String> name;
	public static volatile SingularAttribute<City, String> country;
	public static volatile SingularAttribute<City, Bounds> bounds;
	public static volatile SingularAttribute<City, Location> location;
}

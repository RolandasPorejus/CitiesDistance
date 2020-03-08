package citiesDistance;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-03-08T16:46:57.831+0200")
@StaticMetamodel(Bounds.class)
public class Bounds_ {
	public static volatile SingularAttribute<Bounds, Integer> SOUTH_WEST;
	public static volatile SingularAttribute<Bounds, Integer> NORTH_EAST;
	public static volatile ListAttribute<Bounds, Coordinates> coordinates;
	public static volatile SingularAttribute<Bounds, Integer> id;
	public static volatile SingularAttribute<Bounds, City> city;
}

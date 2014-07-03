package twitter4j;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
public interface Place extends TwitterResponse, Comparable<Place> {
	GeoLocation[][] getBoundingBoxCoordinates();

	String getBoundingBoxType();

	Place[] getContainedWithIn();

	String getCountry();

	String getCountryCode();

	String getFullName();

	GeoLocation[][] getGeometryCoordinates();

	String getGeometryType();

	String getId();

	String getName();

	String getPlaceType();

	String getStreetAddress();

	String getURL();
}

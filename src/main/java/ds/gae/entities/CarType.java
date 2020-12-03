package ds.gae.entities;

import java.util.Objects;

import com.google.cloud.datastore.*;

public class CarType {
    
    private Key key;
    
    private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    
    /***************
     * CONSTRUCTOR *
     ***************/

    public CarType(Key key) {
    	this.key = key;
    }

    public String getName() {
    	return key.getName();
    }
    
    public int getNbOfSeats() {
    	return new Long(datastore.get(key).getLong("nbOfSeats")).intValue();
    }

    public boolean isSmokingAllowed() {
        return datastore.get(key).getBoolean("smokingAllowed");
    }

    public double getRentalPricePerDay() {
        return datastore.get(key).getDouble("rentalPricePerDay");
    }

    public float getTrunkSpace() {
        return new Double(datastore.get(key).getDouble("trunkSpace")).floatValue();
    }
    
    public Key getKey() {
		return key;
	}
    
	/*************
     * TO STRING *
     *************/

    @Override
    public String toString() {
        return String.format(
                "Car type: %s \t[seats: %d, price: %.2f, smoking: %b, trunk: %.0fl]",
                key.getName(),
                getNbOfSeats(),
                getRentalPricePerDay(),
                isSmokingAllowed(),
                getTrunkSpace()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CarType other = (CarType) obj;
        if (!Objects.equals(this.key.getName(), other.key.getName())) {
            return false;
        }
        return true;
    }
}

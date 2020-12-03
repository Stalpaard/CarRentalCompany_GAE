package ds.gae.entities;
import ds.gae.helper.*;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import com.google.cloud.datastore.*;

public class Reservation {

	protected Key key;
	
	protected Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    
    /***************
     * CONSTRUCTOR *
     ***************/

    public Reservation(Key carKey, Quote quote, int carId) {
    	//Persisting constructor
    	Key newKey = datastore.allocateId(Key.newBuilder(carKey, "Reservation").build());
    	Entity entityTask = Entity.newBuilder(newKey)
    			.set("renter", quote.getCarRenter())
    			.set("startDate", quote.getStartDate().getTime())
    			.set("endDate", quote.getEndDate().getTime())
    			.set("crc", quote.getRentalCompany())
    			.set("carType", quote.getCarType())
    			.set("rentalPrice", quote.getRentalPrice())
    			.set("carId", carId)
    			.build();
    	
    	datastore.put(entityTask);
    }

    public Reservation(Key key) {
    	//Non-persisting constructor
        this.key = key;
    }

    /******
     * Getters *
     ******/

    public Key getKey() {
    	return this.key;
    }
    
    public int getCarId() {
        return new Long(datastore.get(key).getLong("carId")).intValue();
    }

    public Date getStartDate() throws ClassNotFoundException, IOException {
        return new Date(datastore.get(key).getLong("startDate"));
    }

    public Date getEndDate() throws ClassNotFoundException, IOException {
    	return new Date(datastore.get(key).getLong("endDate"));
    }

    public String getRenter() {
        return datastore.get(key).getString("renter");
    }

    public String getRentalCompany() {
        return datastore.get(key).getString("crc");
    }

    public double getRentalPrice() {
        return datastore.get(key).getDouble("rentalPrice");
    }

    public String getCarType() {
        return datastore.get(key).getString("carType");
    }

    /*************
     * TO STRING *
     *************/

    @Override
    public String toString() {
        try {
			return String.format(
			        "Quote for %s from %s to %s at %s\nCar type: %s\tTotal price: %.2f",
			        getRenter(),
			        getStartDate(),
			        getEndDate(),
			        getRentalCompany(),
			        getCarType(),
			        getRentalPrice()
			);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; //wrong
		}
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
        Reservation other = (Reservation) obj;
        if (!Objects.equals(key, other.key)) {
            return false;
        }
        
        return true;
    }
}

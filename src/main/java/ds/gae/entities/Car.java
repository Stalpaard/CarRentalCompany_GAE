package ds.gae.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.helper.Quote;
import ds.gae.helper.TemporaryReservation;
import ds.gae.entities.*;
import java.io.IOException;

public class Car {

	private Key key;
	private CarType type;
    
	private Set<Reservation> reservations = new HashSet<>();
	
    private static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Car(Key key)
    {
    	this.key = key;
    	Query<Key> query = Query.newKeyQueryBuilder()
    			.setKind("CarType")
    			.setFilter(PropertyFilter.hasAncestor(key))
    			.build();
    	this.type = new CarType(datastore.run(query).next());
    	
    	Query<Key> resQuery = Query.newKeyQueryBuilder()
        		.setKind("Reservation")
        		.setFilter(PropertyFilter.hasAncestor(key))
        		.build();
    	
        QueryResults<Key> results = datastore.run(resQuery);
        
        
        while(results.hasNext())
        	reservations.add(new Reservation(results.next()));
    }
    
    public Car(Key key, Key typeKey) {
    	this.key = key;
    	this.type = new CarType(typeKey);
    }

    /******
     * ID *
     ******/

    public int getId() {
        return this.key.getId().intValue();
    }

    /************
     * CAR TYPE *
     ************/

    public CarType getType() {
        return type;
    }
    
    public Key getKey() {
		return key;
	}


	/****************
     * RESERVATIONS *
     ****************/

    public Set<Reservation> getReservations() {

        return reservations;
    }

    public boolean isAvailable(Date start, Date end) {
        if (!start.before(end)) {
            throw new IllegalArgumentException("Illegal given period");
        }

        for (Reservation reservation : getReservations()) {
            try {
				if (reservation.getEndDate().before(start) || reservation.getStartDate().after(end)) {
				    continue;
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return false;
        }
        return true;
    }
    
    public Reservation addReservation(Transaction tx, Quote quote, int carId) {
    	
    	Reservation res = new Reservation(tx, key, quote, carId);
    	reservations.add(new TemporaryReservation(quote, carId));
    	
    	Entity updatedEntity = Entity.newBuilder(key)
    			//trigger concurrency for transaction
    			.set("reservations_made", ((datastore.get(key).getLong("reservations_made") + 1)%Long.MAX_VALUE))
    			.build();
    	
    	
    	tx.put(updatedEntity);
    			
    	return res;
    }
    
    public boolean cancelReservation(Reservation res) {
    	if(reservations.contains(res.key)) {
    		datastore.delete(res.key);
    		return true;
    	}
    	return false;
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
        Car other = (Car) obj;
        if (!Objects.equals(key, other.key)) {
            return false;
        }
        return true;
    }
}

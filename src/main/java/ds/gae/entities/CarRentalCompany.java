package ds.gae.entities;

import ds.gae.helper.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.ReservationException;
import ds.gae.helper.ReservationConstraints;

public class CarRentalCompany {

    private static final Logger logger = Logger.getLogger(CarRentalCompany.class.getName());

    private Key key;
    
    private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    
    private Set<Car> cars;
    private Map<String, CarType> carTypes = new HashMap<>();

    /***************
     * CONSTRUCTOR *
     ***************/

    public CarRentalCompany(Key key)
    {
    	//Query constructor
    	this.key = key;
    	
    	Query<Key> query = Query.newKeyQueryBuilder()
    			.setKind("Car")
    			.setFilter(PropertyFilter.hasAncestor(key))
    			.build();
    	
    	QueryResults<Key> results = datastore.run(query);
    	
    	cars = new HashSet<>();
    	while(results.hasNext())
    	{
    		Car car = new Car(results.next());
    		cars.add(car);
    		carTypes.put(car.getType().getName(), car.getType());
    	}
    }
    
    public String getName() {
    	return this.key.getName();
    }

    /*************
     * CAR TYPES *
     *************/

    public Collection<CarType> getAllCarTypes() {
        return carTypes.values();
    }

    public CarType getCarType(String carTypeName) {
        return carTypes.get(carTypeName);
    }

    public boolean isAvailable(String carTypeName, Date start, Date end) {
        logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[] { getName(), carTypeName });
        Set<CarType> availableTypes = getAvailableCarTypes(start, end);
        logger.log(Level.INFO, "<{0}>" + availableTypes.size() + " available cars " + " for car type {1}", new Object[] { getName(), carTypeName });
        for(CarType c : availableTypes) if(c.getName().equals(carTypeName)) return true;
        return false;
    }

    public Set<CarType> getAvailableCarTypes(Date start, Date end) {
        Set<CarType> availableCarTypes = new HashSet<>();
        for (Car car : getCars()) {
            if (car.isAvailable(start, end)) {
                availableCarTypes.add(car.getType());
            }
        }
        return availableCarTypes;
    }

    /*********
     * CARS *
     *********/
/*
    private Car getCar(int uid) {
        for (Car car : cars) {
            if (car.getId() == uid) {
                return car;
            }
        }
        throw new IllegalArgumentException("<" + getName() + "> No car with uid " + uid);
    }
*/
    public Set<Car> getCars() {
        return cars;
    }

    private List<Car> getAvailableCars(String carType, Date start, Date end) {
        List<Car> availableCars = new LinkedList<>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }

    /****************
     * RESERVATIONS *
     ****************/

    public Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
                new Object[] { getName(), client, constraints.toString() });

        CarType type = getCarType(constraints.getCarType());

        if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
            throw new ReservationException("<" + getName() + "> No cars available to satisfy the given constraints.");
        }

        double price = calculateRentalPrice(
                type.getRentalPricePerDay(),
                constraints.getStartDate(),
                constraints.getEndDate()
        );

        return new Quote(
                client,
                constraints.getStartDate(),
                constraints.getEndDate(),
                getName(),
                constraints.getCarType(),
                price
        );
    }

    // Implementation can be subject to different pricing strategies
    private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
        return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
    }

    public Reservation confirmQuote(Quote quote, Transaction tx) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[] { getName(), quote.toString() });
        List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
        if (availableCars.isEmpty()) {
            throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
                    + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
        }
        Car car = availableCars.get((int) (Math.random() * availableCars.size()));
        
        Reservation res = car.addReservation(tx, quote, car.getId());
        return res;
    }

    public void cancelReservation(Reservation res) {
    	logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[] { getName(), res.toString() });
    	boolean found = false;
    	Iterator<Car> it = this.getCars().iterator();
    	while(!found && it.hasNext())
    	{
    		Car car = it.next();
    		if(car.cancelReservation(res)) found = true;
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
        CarRentalCompany other = (CarRentalCompany) obj;
        if (!Objects.equals(key, other.key)) {
            return false;
        }
        return true;
    }
}

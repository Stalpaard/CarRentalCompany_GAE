package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.entities.*;
import ds.gae.helper.*;
import ds.gae.tasks.*;

public class CarRentalModel {
    
	private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    
    public Key modelKey = datastore.allocateId(datastore.newKeyFactory().setKind("CarRentalModel").newKey());
    
    private static CarRentalModel instance;
    
    public static CarRentalModel get() {
        if (instance == null) {
            instance = new CarRentalModel();
        }
        return instance;
    }
    
    /**
     * Get the car types available in the given car rental company.
     *
     * @param companyName the car rental company
     * @return The list of car types (i.e. name of car type), available in the given
     * car rental company.
     */
    public Set<String> getCarTypesNames(String companyName) {
    	Query<Key> query = Query.newKeyQueryBuilder()
    			.setKind("CarType")
    			.setFilter(PropertyFilter.hasAncestor(Key.newBuilder(modelKey, "CarRentalCompany", companyName).build()))
    			.build();
    	QueryResults<Key> results = datastore.run(query);
    	
    	Set<String> names = new HashSet<>();
    	
    	while(results.hasNext())
    		names.add(results.next().getName());
    	
    	return names;
    	
    }

    /**
     * Get the names of all registered car rental companies
     *
     * @return the list of car rental companies
     */
    public Collection<String> getAllRentalCompanyNames() {
        
    	Query<Key> query = Query.newKeyQueryBuilder()
    			.setKind("CarRentalCompany")
    			.setFilter(PropertyFilter.hasAncestor(modelKey))
    			.build();
    	QueryResults<Key> results = datastore.run(query);
    	
    	Set<String> crcNames = new HashSet<>();
       
    	while(results.hasNext())
    		crcNames.add(results.next().getName());
    	
        return crcNames;
    }

    /**
     * Create a quote according to the given reservation constraints (tentative
     * reservation).
     *
     * @param companyName name of the car renter company
     * @param renterName  name of the car renter
     * @param constraints reservation constraints for the quote
     * @return The newly created quote.
     * @throws ReservationException No car available that fits the given
     *                              constraints.
     */
    public Quote createQuote(String companyName, String renterName, ReservationConstraints constraints)
            throws ReservationException {
        CarRentalCompany crc = new CarRentalCompany(Key.newBuilder(modelKey, "CarRentalCompany", companyName).build());
        return crc.createQuote(constraints, renterName);
    }

    /**
     * Confirm the given quote.
     *
     * @param quote Quote to confirm
     */
    public void confirmQuote(Quote quote, String mailAddress) {
    	Queue queue = QueueFactory.getQueue("queue-quote");
		queue.add(TaskOptions.Builder.withPayload(new QuoteTask(modelKey, mailAddress, quote)));
    }

    /**
     * Confirm the given list of quotes
     *
     * @param quotes the quotes to confirm
     */
    public void confirmQuotes(List<Quote> quotes, String mailAddress) {
    	Queue queue = QueueFactory.getQueue("queue-quote");
		queue.add(TaskOptions.Builder.withPayload(new QuoteTask(modelKey, mailAddress, quotes.toArray(new Quote[quotes.size()]))));
    }

    /**
     * Get all reservations made by the given car renter.
     *
     * @param renter name of the car renter
     * @return the list of reservations of the given car renter
     */
    public List<Reservation> getReservations(String renter) {
        List<Reservation> out = new ArrayList<>();
        
        Query<Key> query = Query.newKeyQueryBuilder()
        		.setKind("CarRentalCompany")
        		.setFilter(PropertyFilter.hasAncestor(modelKey))
        		.build();
        
        QueryResults<Key> results = datastore.run(query);
        
        while(results.hasNext())
        {
        	CarRentalCompany crc = new CarRentalCompany(results.next());
        	for (Car c : crc.getCars()) 
                for (Reservation r : c.getReservations()) 
                    if (r.getRenter().equals(renter)) 
                        out.add(r);
        }
        
        return out;
    }

    /**
     * Get the car types available in the given car rental company.
     *
     * @param companyName the given car rental company
     * @return The list of car types in the given car rental company.
     */
    public Collection<CarType> getCarTypesOfCarRentalCompany(String companyName) {
    	CarRentalCompany crc = new CarRentalCompany(Key.newBuilder(modelKey, "CarRentalCompany", companyName).build());
        Collection<CarType> out = new ArrayList<>(crc.getAllCarTypes());
        return out;
    }

    /**
     * Get the list of cars of the given car type in the given car rental company.
     *
     * @param companyName name of the car rental company
     * @param carType     the given car type
     * @return A list of car IDs of cars with the given car type.
     */
    public Collection<Integer> getCarIdsByCarType(String companyName, CarType carType) {
        Collection<Integer> out = new ArrayList<>();
        for (Car c : getCarsByCarType(companyName, carType)) {
            out.add(c.getId());
        }
        return out;
    }

    /**
     * Get the amount of cars of the given car type in the given car rental company.
     *
     * @param companyName name of the car rental company
     * @param carType     the given car type
     * @return A number, representing the amount of cars of the given car type.
     */
    public int getAmountOfCarsByCarType(String companyName, CarType carType) {
        return this.getCarsByCarType(companyName, carType).size();
    }

    /**
     * Get the list of cars of the given car type in the given car rental company.
     *
     * @param companyName name of the car rental company
     * @param carType     the given car type
     * @return List of cars of the given car type
     */
    private List<Car> getCarsByCarType(String companyName, CarType carType) {
        List<Car> out = new ArrayList<>();
        
        CarRentalCompany crc = new CarRentalCompany(Key.newBuilder(modelKey, "CarRentalCompany", companyName).build());
       
        for (Car c : crc.getCars()) {
            if (c.getType().equals(carType)) {
                out.add(c);
            }
        }
        
        return out;

    }

    /**
     * Check whether the given car renter has reservations.
     *
     * @param renter the car renter
     * @return True if the number of reservations of the given car renter is higher
     * than 0. False otherwise.
     */
    public boolean hasReservations(String renter) {
        return this.getReservations(renter).size() > 0;
    }
}

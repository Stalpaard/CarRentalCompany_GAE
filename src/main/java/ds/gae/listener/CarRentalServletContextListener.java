package ds.gae.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.cloud.datastore.*;

import ds.gae.CarRentalModel;

public class CarRentalServletContextListener implements ServletContextListener {

	private static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        // This will be invoked as part of a warming request,
        // or the first user request if no warming request was invoked.

        // check if dummy data is available, and add if necessary
        if (!isDummyDataAvailable()) {
            addDummyData();
        }
    }

    private boolean isDummyDataAvailable() {
        // If the Hertz car rental company is in the datastore, we assume the dummy data
        // is available
        return CarRentalModel.get().getAllRentalCompanyNames().contains("Hertz");

    }

    private void addDummyData() {
        loadRental("Hertz", "hertz.csv");
        loadRental("Dockx", "dockx.csv");
    }

    private void loadRental(String crcName, String datafile) {
        Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO, "loading {0} from file {1}",
                new Object[] { crcName, datafile });
        try {
        	loadData(crcName, datafile);
        } catch (NumberFormatException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loadData(String crcName, String datafile) throws NumberFormatException, IOException {
        int carId = 1;

        // Create company key
        Key crcKey = Key.newBuilder(CarRentalModel.get().modelKey, "CarRentalCompany", crcName).build();
        
        Entity crcEntityTask = Entity.newBuilder(crcKey).build();
        
        // open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(
                CarRentalServletContextListener.class.getClassLoader().getResourceAsStream(datafile)));
        // while next line exists
        while (in.ready()) {
            // read line
            String line = in.readLine();
            // if comment: skip
            if (line.startsWith("#")) {
                continue;
            }
            // tokenize on ,
            StringTokenizer csvReader = new StringTokenizer(line, ",");

            // Parse CarType data
            String carTypeName = csvReader.nextToken();
            int nbOfSeats = Integer.parseInt(csvReader.nextToken());
            float trunkSpace = Float.parseFloat(csvReader.nextToken());
            double rentalPricePerDay = Double.parseDouble(csvReader.nextToken());
            boolean smokingAllowed = Boolean.parseBoolean(csvReader.nextToken());
            
            
            // create N new cars with given type, where N is the 5th field
            for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
            	int nextCarId = carId++;
                Key newCarKey = Key.newBuilder(crcKey, "Car",nextCarId)
                		.build();
                
                Entity carEntityTask = Entity.newBuilder(newCarKey)
                		.set("reservations_made", 0)
                		.build();
                
                Key newCarTypeKey = Key.newBuilder(newCarKey, "CarType", carTypeName).build();
                
                Entity carTypeEntityTask = Entity.newBuilder(newCarTypeKey)
                		.set("nbOfSeats", nbOfSeats)
                		.set("trunkSpace", trunkSpace)
                		.set("rentalPricePerDay", rentalPricePerDay)
                		.set("smokingAllowed", smokingAllowed)
                		.build();
                
            	datastore.put(carEntityTask, carTypeEntityTask);
            }
        }
        
        datastore.put(crcEntityTask);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // Please leave this method empty.
    }
}

package ds.gae.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.cloud.datastore.Key;

import ds.gae.ReservationException;
import ds.gae.entities.CarRentalCompany;
import ds.gae.helper.*;

public class QuoteTask implements DeferredTask {

	private Quote quote;
	private Key modelKey;
	
	public QuoteTask(Quote quote, Key modelKey)
	{
		this.quote = quote;
		this.modelKey = modelKey;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("New deferred task: " + quote.toString());
		CarRentalCompany crc = new CarRentalCompany(Key.newBuilder(modelKey, "CarRentalCompany", quote.getRentalCompany()).build());
        try {
			crc.confirmQuote(quote);
		} catch (ReservationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

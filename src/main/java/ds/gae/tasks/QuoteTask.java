package ds.gae.tasks;

import java.util.List;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import ds.gae.ReservationException;
import ds.gae.entities.CarRentalCompany;
import ds.gae.helper.*;

public class QuoteTask implements DeferredTask {

	private Quote[] quotes;
	private Key modelKey;
	
	public QuoteTask(Key modelKey, Quote...quotes)
	{
		this.quotes = quotes;
		this.modelKey = modelKey;
	}
	
	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		Transaction tx = DatastoreOptions.getDefaultInstance().getService().newTransaction();
		try {
			for(Quote quote : quotes)
			{
				System.out.println("New deferred task: " + quote.toString());
				CarRentalCompany crc = new CarRentalCompany(Key.newBuilder(modelKey, "CarRentalCompany", quote.getRentalCompany()).build());
				crc.confirmQuote(quote, tx);
			}
			tx.commit();
		}
		catch(ReservationException e) {
			tx.rollback();
			e.printStackTrace();
		}
		
	}

}

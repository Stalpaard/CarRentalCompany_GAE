package ds.gae.tasks;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
	private String mailAddress;
	
	public QuoteTask(Key modelKey, String mailAddress, Quote...quotes)
	{
		this.quotes = quotes;
		this.modelKey = modelKey;
		this.mailAddress = mailAddress;
	}
	
	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		Transaction tx = DatastoreOptions.getDefaultInstance().getService().newTransaction();
		boolean failed = false;
		
		try {
			Map<String, CarRentalCompany> crcMap = new HashMap<>();
			for(Quote quote : quotes)
			{
				System.out.println("New deferred task: " + quote.toString());
				if(crcMap.keySet().contains(quote.getRentalCompany()) == false) 
					crcMap.put(quote.getRentalCompany(), new CarRentalCompany(Key.newBuilder(modelKey, "CarRentalCompany", quote.getRentalCompany()).build()));
				CarRentalCompany crc = crcMap.get(quote.getRentalCompany());
				crc.confirmQuote(quote, tx);
			}
			tx.commit();
		}
		catch(ReservationException e) {
			failed = true;
			tx.rollback();
			e.printStackTrace();
		}
		Session session = Session.getDefaultInstance(new Properties());
		try {
			Message msg = new MimeMessage(session);
			
			InternetAddress adminEmail = new InternetAddress("admin@adminmail.com", "Admin");
			InternetAddress clientEmail = new InternetAddress(mailAddress);
			msg.setFrom(adminEmail);
			msg.addRecipient(Message.RecipientType.TO, clientEmail);
			
			String status = "Confirmed";
			if(failed) status = "Failed";
			if(quotes.length > 1) msg.setSubject("Reservations " + status);
			else msg.setSubject("Reservation " + status);
			
			StringBuilder sb = new StringBuilder(status + " reservations:\n");
			for(Quote q : quotes)
			{
				sb.append("\t" + q.getCarType() + " car from " + q.getStartDate().toString() + " until " + q.getEndDate().toString() 
						+ " at " + q.getRentalCompany() + " for a total of: " + q.getRentalPrice() + "\n");
			}
			msg.setText(sb.toString());
			
			System.out.println(sb.toString());
			
			Transport.send(msg);
		}
		catch(MessagingException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

}

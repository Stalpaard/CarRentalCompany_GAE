package ds.gae.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import ds.gae.helper.*;

public class QuoteTask implements DeferredTask {

	Quote quote;
	
	public QuoteTask(Quote quote)
	{
		this.quote = quote;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("New deferred task: " + quote.toString());	
	}

}

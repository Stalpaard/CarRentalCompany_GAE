package ds.gae.helper;

import java.io.IOException;
import java.util.Date;

import ds.gae.entities.Reservation;

public class TemporaryReservation extends Reservation {
	
	private Quote quote;
	private int carId;
	
	public TemporaryReservation(Quote quote, int carId) {
		super(null);
		this.quote = quote;
		this.carId = carId;
	}
	
	@Override
	public int getCarId() {
		return carId;
	}
	
	@Override
	public String getRenter() {
		return quote.getCarRenter();
	}
	
	@Override
	public String getRentalCompany() {
		return quote.getRentalCompany();
	}
	
	@Override
	public String getCarType() {
		return quote.getCarType();
	}
	
	@Override
	public double getRentalPrice() {
		return quote.getRentalPrice();
	}

	@Override
	public Date getStartDate() throws ClassNotFoundException, IOException {
		return quote.getStartDate();
	}
	
	@Override
	public Date getEndDate() throws ClassNotFoundException, IOException {
		return quote.getEndDate();
	}
}

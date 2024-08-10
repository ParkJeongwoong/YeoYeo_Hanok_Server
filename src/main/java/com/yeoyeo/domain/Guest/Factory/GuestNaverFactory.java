package com.yeoyeo.domain.Guest.Factory;

import com.yeoyeo.domain.Guest.GuestNaver;

public class GuestNaverFactory extends GuestFactory {

	public GuestNaverFactory() {
		super("NaverGuest", null, null, 2, null);
		super.guestClassName = "GuestNaver";
	}

	@Override
	public GuestNaver createGuest() {
		return new GuestNaver();
	}

	@Override
	public GuestNaver createGuest(String name,String phoneNumber,String email,int guestCount,String request) {
		return new GuestNaver(name, phoneNumber, email, guestCount, request);
	}

}

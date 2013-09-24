package com.monitortempecg.service;

public class PacketTemp extends Packet {

	private static final long serialVersionUID = 1L;

	private int valor;

	public PacketTemp(int valorTemp) {
		valor = valorTemp;
	}

	public int getValue() {
		// TODO Auto-generated method stub
		return valor;
	}

}

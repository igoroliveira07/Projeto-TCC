package com.monitortempecg.service;

public class PacketECG extends Packet {
	private static final long serialVersionUID = 1L;
	private int valor;

	public PacketECG(int valorECG) {
		valor = valorECG;
	}

	public int getValue() {
		return valor;
	}

}

package com.monitortempecg.service;

public class Parser {

	public enum Estados {
		IDLE, TIPO, ECG, ECG2, TEMP, TEMP2
	}

	private static final int INICIO_DE_PACOTE = 254;

	private static final int TIPO_ECG = 1;

	private static final int TIPO_TEMP = 2;

	public Estados estado = Estados.IDLE;

	private int valorECG;

	private int valorTemp;

	/**
	 * Decodifica os dados recebidos via serial
	 * 
	 * @param byteRecebido
	 * @return nulo (caso não tenha decodificado o pacote) ou o pacote
	 *         decodificado
	 */
	public Packet parse(int byteRecebido) {

		switch (estado) {
		case IDLE:
			if (byteRecebido == INICIO_DE_PACOTE) {
				estado = Estados.TIPO;
			}
			break;
		case TIPO:
			if (byteRecebido == TIPO_ECG) {
				estado = Estados.ECG;
			} else if (byteRecebido == TIPO_TEMP) {
				estado = Estados.TEMP;
			} else {
				estado = Estados.IDLE;
			}
			break;
		case ECG:

			valorECG = byteRecebido * 256;
			estado = Estados.ECG2;
			break;
		case ECG2:

			valorECG = valorECG + byteRecebido;
			estado = Estados.IDLE;

			return new PacketECG(valorECG); // TODO decodificar o pacote
		case TEMP:

			valorTemp = byteRecebido * 256;
			estado = Estados.TEMP2;
			break;
		case TEMP2:

			valorTemp = valorTemp + byteRecebido;
			estado = Estados.IDLE;

			return new PacketTemp(valorTemp); // TODO decodificar o pacote
		}

		return null;

	}

}

package com.ragnarokbot.model.enums;

import com.ragnarokbot.model.Coordenadas;

public class ComandoRecebido {
	
	Comando comando; 
	Coordenadas coordenadas;
	String sala;
	
	public ComandoRecebido(Comando comando, Coordenadas coordenadas, String sala) {
		super();
		this.comando = comando;
		this.coordenadas = coordenadas;
		this.sala = sala;
	}
	public Comando getComando() {
		return comando;
	}
	public void setComando(Comando comando) {
		this.comando = comando;
	}
	public Coordenadas getCoordenadas() {
		return coordenadas;
	}
	public void setCoordenadas(Coordenadas coordenadas) {
		this.coordenadas = coordenadas;
	}
	public String getSala() {
		return sala;
	}
	public void setSala(String sala) {
		this.sala = sala;
	}
	
}

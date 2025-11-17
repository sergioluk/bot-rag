package com.ragnarokbot.model.enums;

import com.ragnarokbot.model.Coordenadas;

public class ComandoRecebido {
	
	Comando comando; 
	Coordenadas coordenadas;
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
	public ComandoRecebido(Comando comando, Coordenadas coordenadas) {
		super();
		this.comando = comando;
		this.coordenadas = coordenadas;
	}
	
	
}

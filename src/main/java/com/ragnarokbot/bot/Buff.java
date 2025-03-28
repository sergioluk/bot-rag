package com.ragnarokbot.bot;

public class Buff {
	private int tecla;
	private int cd;
	private int cdCounter;
	private long lastUsedTime; // Timestamp do último uso
	
	private boolean self;
	
	public Buff(int tecla, int cd, boolean self) {
		this.tecla = tecla;
		this.cd = cd;
		this.lastUsedTime = 0;
		this.self = self;
	}
	
	public int getTecla() {
        return tecla;
    }
	
    
    public boolean isSelf() {
		return self;
	}

	public boolean isReady() {
        // Verifica se o cooldown acabou
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUsedTime) >= cd;
    }

    public void use() {
        // Marca o tempo de uso
        lastUsedTime = System.currentTimeMillis();
    }
}

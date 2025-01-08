package com.ragnarokbot.bot;

public class Skill {
	private int tecla;
	private String cor;
	private int cd;
	private int cdCounter;
	 private long lastUsedTime; // Timestamp do último uso
	
	public Skill(int tecla, String cor, int cd) {
		this.tecla = tecla;
		this.cor = cor;
		this.cd = cd;
		this.lastUsedTime = 0;
	}
	
	public int getTecla() {
        return tecla;
    }

    public String getCor() {
        return cor;
    }
    
    
    public boolean isReady() {
        // Verifica se o cooldown acabou
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUsedTime) >= (cd * 1000);
    }

    public void use() {
        // Marca o tempo de uso
        lastUsedTime = System.currentTimeMillis();
    }
    
}

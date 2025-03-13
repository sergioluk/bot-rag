package com.ragnarokbot.bot;

public class Skill {
	private int tecla;
	private String cor;
	private int cd;
	private int cdCounter;
	//notebook
	private int range;
	private long lastUsedTime; // Timestamp do último uso
	
	private Boolean main;
	private String posicao;
	private Boolean selfSkill;
	
	public Skill(int tecla, String cor, int cd, int range, String posicao, Boolean main, Boolean selfSkill) {
		this.tecla = tecla;
		this.cor = cor;
		this.cd = cd;
		this.range = range;
		this.lastUsedTime = 0;
		this.posicao = posicao;
		this.main = main;
		this.selfSkill = selfSkill;
	}
	
	
	
	public Boolean getSelfSkill() {
		return selfSkill;
	}



	public void setSelfSkill(Boolean selfSkill) {
		this.selfSkill = selfSkill;
	}



	public Boolean getMain() {
		return main;
	}



	public void setMain(Boolean main) {
		this.main = main;
	}



	public String getPosicao() {
		return posicao;
	}



	public void setPosicao(String posicao) {
		this.posicao = posicao;
	}



	public int getTecla() {
        return tecla;
    }

    public String getCor() {
        return cor;
    }
    
    public int getRange() {
    	return range;
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

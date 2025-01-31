package config;

import java.util.List;

public class Script {
	private String mapa;
	private List<Rota> rotas;
	private Finalizacao finalizacao;
	private List<Links> coordenadasAlt;
	
	public String getMapa() {
		return mapa;
	}
	public void setMapa(String mapa) {
		this.mapa = mapa;
	}
	public List<Links> getCoordenadasAlt() {
		return coordenadasAlt;
	}
	public void setCoordenadasAlt(List<Links> coordenadasAlt) {
		this.coordenadasAlt = coordenadasAlt;
	}
	public List<Rota> getRotas() {
		return rotas;
	}
	public void setRotas(List<Rota> rotas) {
		this.rotas = rotas;
	}
	public Finalizacao getFinalizacao() {
		return finalizacao;
	}
	public void setFinalizacao(Finalizacao finalizacao) {
		this.finalizacao = finalizacao;
	}
	
	public static class Links {
		private List<Integer> links;

		public List<Integer> getLinks() {
			return links;
		}

		public void setLinks(List<Integer> links) {
			this.links = links;
		}
	}
	
	public static class Rota {
	    private String descricao;
	    private List<Passo> passos;
	    private Verificacao verificacao;
	    
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		public List<Passo> getPassos() {
			return passos;
		}
		public void setPassos(List<Passo> passos) {
			this.passos = passos;
		}
		public Verificacao getVerificacao() {
			return verificacao;
		}
		public void setVerificacao(Verificacao verificacao) {
			this.verificacao = verificacao;
		}
	    
	}

	public static class Verificacao {
	    private String tipo;
	    private List<Integer> coordenadas;
	    private List<Passo> elseAcoes;
	    private List<Acao> acoes;
	    private boolean boss;
	    private boolean terminaNoBoss;
	    
		public boolean isTerminaNoBoss() {
			return terminaNoBoss;
		}
		public void setTerminaNoBoss(boolean terminaNoBoss) {
			this.terminaNoBoss = terminaNoBoss;
		}
		public boolean isBoss() {
			return boss;
		}
		public void setBoss(boolean boss) {
			this.boss = boss;
		}
	    
		public String getTipo() {
			return tipo;
		}
		public void setTipo(String tipo) {
			this.tipo = tipo;
		}
		public List<Integer> getCoordenadas() {
			return coordenadas;
		}
		public void setCoordenadas(List<Integer> coordenadas) {
			this.coordenadas = coordenadas;
		}
		public List<Passo> getElseAcoes() {
			return elseAcoes;
		}
		public void setElseAcoes(List<Passo> elseAcoes) {
			this.elseAcoes = elseAcoes;
		}
		public List<Acao> getAcoes() {
			return acoes;
		}
		public void setAcoes(List<Acao> acoes) {
			this.acoes = acoes;
		}
	   
	}
	
	public static class Acao {
		private boolean balaoUnico;
		private int opcao;
		
		public boolean isBalaoUnico() {
			return balaoUnico;
		}
		public void setBalaoUnico(boolean balaoUnico) {
			this.balaoUnico = balaoUnico;
		}
		public int getOpcao() {
			return opcao;
		}
		public void setOpcao(int opcao) {
			this.opcao = opcao;
		}
		
	}

	public static class Passo {
	    private List<Integer> coordenadas;

		public List<Integer> getCoordenadas() {
			return coordenadas;
		}

		public void setCoordenadas(List<Integer> coordenadas) {
			this.coordenadas = coordenadas;
		}
	    
	}
	public static class Finalizacao {
	    private String descricao;
	    private List<Integer> coordenadas;
	    
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		public List<Integer> getCoordenadas() {
			return coordenadas;
		}
		public void setCoordenadas(List<Integer> coordenadas) {
			this.coordenadas = coordenadas;
		}
	    
	}
	
}


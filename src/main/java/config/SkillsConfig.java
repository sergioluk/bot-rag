package config;

import java.util.List;

//notebook
public class SkillsConfig {
	private List<Classes> classes;
	private int refresh;
	private int goBase;
	private int labirintovalk;
	private int tempoDoLoop;
	private int tempoAndarForcado;
	private String atalhoPlay;
	private String atalhoStop;
	private String atalhoPause;
	private String atalhoClose;
	private String atalhoAdicionarCoords;
	private String atalhoAbrirTxtCoords;
	private String atalhoModoVelocidade;
	private String atalhoModoChicleteGoma;
	private String atalhoVelocidade;
	private String atalhoChicleteGoma;
	private List<String> atalhoEquipamento;
	private List<String> classesDisponiveis;
	private String atalhoVeneno;

	
	public String getAtalhoVeneno() {
		return atalhoVeneno;
	}

	public void setAtalhoVeneno(String atalhoVeneno) {
		this.atalhoVeneno = atalhoVeneno;
	}

	public List<String> getAtalhoEquipamento() {
		return atalhoEquipamento;
	}

	public void setAtalhoEquipamento(List<String> atalhoEquipamento) {
		this.atalhoEquipamento = atalhoEquipamento;
	}

	public String getAtalhoModoVelocidade() {
		return atalhoModoVelocidade;
	}

	public void setAtalhoModoVelocidade(String atalhoModoVelocidade) {
		this.atalhoModoVelocidade = atalhoModoVelocidade;
	}

	public String getAtalhoModoChicleteGoma() {
		return atalhoModoChicleteGoma;
	}

	public void setAtalhoModoChicleteGoma(String atalhoModoChicleteGoma) {
		this.atalhoModoChicleteGoma = atalhoModoChicleteGoma;
	}

	public String getAtalhoVelocidade() {
		return atalhoVelocidade;
	}

	public void setAtalhoVelocidade(String atalhoVelocidade) {
		this.atalhoVelocidade = atalhoVelocidade;
	}

	public String getAtalhoChicleteGoma() {
		return atalhoChicleteGoma;
	}

	public void setAtalhoChicleteGoma(String atalhoChicleteGoma) {
		this.atalhoChicleteGoma = atalhoChicleteGoma;
	}

	public String getAtalhoPlay() {
		return atalhoPlay;
	}

	public void setAtalhoPlay(String atalhoPlay) {
		this.atalhoPlay = atalhoPlay;
	}

	public String getAtalhoStop() {
		return atalhoStop;
	}

	public void setAtalhoStop(String atalhoStop) {
		this.atalhoStop = atalhoStop;
	}

	public String getAtalhoPause() {
		return atalhoPause;
	}

	public void setAtalhoPause(String atalhoPause) {
		this.atalhoPause = atalhoPause;
	}

	public String getAtalhoClose() {
		return atalhoClose;
	}

	public void setAtalhoClose(String atalhoClose) {
		this.atalhoClose = atalhoClose;
	}

	public String getAtalhoAdicionarCoords() {
		return atalhoAdicionarCoords;
	}

	public void setAtalhoAdicionarCoords(String atalhoAdicionarCoords) {
		this.atalhoAdicionarCoords = atalhoAdicionarCoords;
	}

	public String getAtalhoAbrirTxtCoords() {
		return atalhoAbrirTxtCoords;
	}

	public void setAtalhoAbrirTxtCoords(String atalhoAbrirTxtCoords) {
		this.atalhoAbrirTxtCoords = atalhoAbrirTxtCoords;
	}

	public int getTempoAndarForcado() {
		return tempoAndarForcado;
	}

	public void setTempoAndarForcado(int tempoAndarForcado) {
		this.tempoAndarForcado = tempoAndarForcado;
	}

	public int getTempoDoLoop() {
		return tempoDoLoop;
	}

	public void setTempoDoLoop(int tempoDoLoop) {
		this.tempoDoLoop = tempoDoLoop;
	}

	public List<String> getClassesDisponiveis() {
		return classesDisponiveis;
	}

	public void setClassesDisponiveis(List<String> classesDisponiveis) {
		this.classesDisponiveis = classesDisponiveis;
	}

	public int getRefresh() {
		return refresh;
	}

	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	public int getGoBase() {
		return goBase;
	}

	public void setGoBase(int goBase) {
		this.goBase = goBase;
	}

	public int getLabirintovalk() {
		return labirintovalk;
	}

	public void setLabirintovalk(int labirintovalk) {
		this.labirintovalk = labirintovalk;
	}

	public List<Classes> getClasses() {
		return classes;
	}

	public void setClasses(List<Classes> classes) {
		this.classes = classes;
	}
	
	
	public static class Classes {
		private String classe;
		private List<Skills> skills;
		private List<Buffs> buffs;
		public String getClasse() {
			return classe;
		}
		public void setClasse(String classe) {
			this.classe = classe;
		}
		public List<Skills> getSkills() {
			return skills;
		}
		public void setSkills(List<Skills> skills) {
			this.skills = skills;
		}
		public List<Buffs> getBuffs() {
			return buffs;
		}
		public void setBuffs(List<Buffs> buffs) {
			this.buffs = buffs;
		}
	}
	
	public static class Skills {
		private String atalho;
		private int cd;
		private String cor;
		private int range;
		private String posicao;
		private Boolean main;
		public String getAtalho() {
			return atalho;
		}
		public void setAtalho(String atalho) {
			this.atalho = atalho;
		}
		public int getCd() {
			return cd;
		}
		public void setCd(int cd) {
			this.cd = cd;
		}
		public String getCor() {
			return cor;
		}
		public void setCor(String cor) {
			this.cor = cor;
		}
		public int getRange() {
			return range;
		}
		public void setRange(int range) {
			this.range = range;
		}
		public String getPosicao() {
			return posicao;
		}
		public void setPosicao(String posicao) {
			this.posicao = posicao;
		}
		public Boolean isMain() {
			return main;
		}
		public void setMain(Boolean main) {
			this.main = main;
		}
	}
	
	public static class Buffs {
		private String atalho;
		private int cd;
		private boolean self;
		private String icone;
		public String getAtalho() {
			return atalho;
		}
		public void setAtalho(String atalho) {
			this.atalho = atalho;
		}
		public int getCd() {
			return cd;
		}
		public void setCd(int cd) {
			this.cd = cd;
		}
		public boolean isSelf() {
			return self;
		}
		public void setSelf(boolean self) {
			this.self = self;
		}
		public String getIcone() {
			return icone;
		}
		public void setIcone(String icone) {
			this.icone = icone;
		}
		
	}
	
}

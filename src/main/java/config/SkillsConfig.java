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
	private List<String> classesDisponiveis;

	
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
	}
	
	public static class Skills {
		private String atalho;
		private int cd;
		private String cor;
		private int range;
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
		
	}
	
}

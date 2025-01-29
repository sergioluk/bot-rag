package config;

import java.util.List;

//notebook
public class SkillsConfig {
	private List<Classes> classes;
	private int refresh;
	private int gomoroc;
	private int labirintovalk;

	public int getRefresh() {
		return refresh;
	}

	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	public int getGomoroc() {
		return gomoroc;
	}

	public void setGomoroc(int gomoroc) {
		this.gomoroc = gomoroc;
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

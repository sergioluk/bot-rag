package config;

import java.util.List;

public class ContasConfig {
	private List<String> instanciasDisponiveis;
	private List<String> classesDisponiveis;
	private List<Conta> contas;

	
    public List<String> getInstanciasDisponiveis() {
		return instanciasDisponiveis;
	}

	public void setInstanciasDisponiveis(List<String> instanciasDisponiveis) {
		this.instanciasDisponiveis = instanciasDisponiveis;
	}

	public List<String> getClassesDisponiveis() {
		return classesDisponiveis;
	}

	public void setClassesDisponiveis(List<String> classesDisponiveis) {
		this.classesDisponiveis = classesDisponiveis;
	}

	public List<Conta> getContas() {
        return contas;
    }

    public void setContas(List<Conta> contas) {
        this.contas = contas;
    }
    
    
    
    public static class Conta {
    	private String usuario;
        private String senha;
        private String pin;
        private List<Personagem> personagens;

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }

        public String getSenha() {
            return senha;
        }

        public void setSenha(String senha) {
            this.senha = senha;
        }

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }

        public List<Personagem> getPersonagens() {
            return personagens;
        }

        public void setPersonagens(List<Personagem> personagens) {
            this.personagens = personagens;
        }
    }
    
    public static class Personagem {
    	private int pagina;
        private int indexPersonagem;
        private boolean passarItens;
        private List<String> instancias;
        private String classe;
        
        public String getClasse() {
			return classe;
		}

		public void setClasse(String classe) {
			this.classe = classe;
		}

		public int getPagina() {
            return pagina;
        }

        public void setPagina(int pagina) {
            this.pagina = pagina;
        }

        public int getIndexPersonagem() {
            return indexPersonagem;
        }

        public void setIndexPersonagem(int indexPersonagem) {
            this.indexPersonagem = indexPersonagem;
        }

        public boolean isPassarItens() {
            return passarItens;
        }

        public void setPassarItens(boolean passarItens) {
            this.passarItens = passarItens;
        }

        public List<String> getInstancias() {
            return instancias;
        }

        public void setInstancias(List<String> instancias) {
            this.instancias = instancias;
        }
    }
}

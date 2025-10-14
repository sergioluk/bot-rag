package config;

import java.util.List;


public class Servidores {
	private List<Servidor> servidores;
	
	public List<Servidor> getServidores() {
		return servidores;
	}
	public void setServidores(List<Servidor> servidores) {
		this.servidores = servidores;
	}

	public static class Servidor {
		private String serverName;
		private String nomeJogo;
		private long addressX;
		private long addressY;
		private long addressHp;
		private long addressMapa;
		private long addressName;
		public String getServerName() {
			return serverName;
		}
		public void setServerName(String serverName) {
			this.serverName = serverName;
		}
		
		public String getNomeJogo() {
			return nomeJogo;
		}
		public void setNomeJogo(String nomeJogo) {
			this.nomeJogo = nomeJogo;
		}
		public long getAddressX() {
			return addressX;
		}
		public void setAddressX(long addressX) {
			this.addressX = addressX;
		}
		public long getAddressY() {
			return addressY;
		}
		public void setAddressY(long addressY) {
			this.addressY = addressY;
		}
		public long getAddressHp() {
			return addressHp;
		}
		public void setAddressHp(long addressHp) {
			this.addressHp = addressHp;
		}
		public long getAddressMapa() {
			return addressMapa;
		}
		public void setAddressMapa(long addressMapa) {
			this.addressMapa = addressMapa;
		}
		public long getAddressName() {
			return addressName;
		}
		public void setAddressName(long addressName) {
			this.addressName = addressName;
		}
		
		
	}
}

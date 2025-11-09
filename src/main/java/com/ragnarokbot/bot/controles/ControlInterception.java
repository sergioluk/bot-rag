package com.ragnarokbot.bot.controles;

import com.ragnarokbot.bot.Bot;

public class ControlInterception implements Control{

	@Override
	public void apertarTecla(int vk) {
		Bot.apertarTeclaInter(vk);
	}

	@Override
	public void moverMouse(int x, int y) {
		Bot.moverMouseInter(x, y);
	}

	@Override
	public void click() {
		Bot.clickInter();
	}

	@Override
	public void scrollMouse(int x) {
		Bot.scrollMouseInter(x);
	}

	@Override
	public void apertarSegurarTecla(int vk) {
		Bot.apertarSegurarTeclaInter(vk);
	}

	@Override
	public void soltarTecla(int vk) {
		Bot.soltarTeclaInter(vk);
	}

	@Override
	public void clicarMouseDireito() {
		Bot.clicarMouseDireitoInter();
	}

	@Override
	public void clicarSegurarMouse() {
		Bot.clicarSegurarMouseInter();
	}

	@Override
	public void soltarMouse() {
		Bot.soltarMouseInter();
	}

	@Override
	public void segurarMouseDireito() {
		Bot.segurarMouseDireitoInter();
	}

	@Override
	public void soltarMouseDireito() {
		Bot.soltarMouseDireitoInter();
	}

}

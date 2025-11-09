package com.ragnarokbot.bot.controles;

public interface Control {
	
    public void apertarTecla(int vk);
    public void moverMouse(int x, int y);
    public void click();
    public void scrollMouse(int x);
    public void apertarSegurarTecla(int vk);
    public void soltarTecla(int vk);
    public void clicarMouseDireito();
    public void clicarSegurarMouse();
    public void soltarMouse();
    public void segurarMouseDireito();
    public void soltarMouseDireito();

}

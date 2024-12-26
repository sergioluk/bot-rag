package com.ragnarokbot.model;

public class OcrResult {
	private String texto;
    private int x, y, largura, altura;

    public OcrResult(String texto, int x, int y, int largura, int altura) {
        this.texto = texto;
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public String getTexto() {
        return texto;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }
}

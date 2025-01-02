package com.ragnarokbot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;

public class Coordenadas {
	
	public int x;
	public int y;
	private static List<Coordenadas> coordenadasValidas = new ArrayList<>();
    private static final int MAX_COORDENADAS = 10; // Limite máximo de coordenadas na lista
	
	private static int tentativasInvalidas = 0;
	private static final int MAX_TENTATIVAS_INVALIDAS = 3;
	
	
	public Coordenadas(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coordenadas(String coordenada) {
		System.out.println("veio isso doocr novo: " + coordenada);
		// Usar expressão regular para capturar apenas números
	    String[] substrings = coordenada.split(" ");
	    if (substrings.length == 1) {
	    	System.out.println("Orc size = 1, coordenadas orc mudadas para ultima coordenadas");
	    	usarUltimaCoordenada();
	        return;
	    }
	    //System.out.println("Ocr: " + coordenada + "Tamanho: " + substrings.length + "/2");
	    String xString = substrings[0].replaceAll("[^0-9]", ""); // Remover tudo que não é número
	    String yString = substrings[1].replaceAll("[^0-9]", ""); // Remover tudo que não é número
	    
	    
		//System.out.println("Tamanho vetor: " + substrings.length + " | x: " + substrings[0] + " y: " + substrings[1]);
		
		// Tratar erros de conversão e atribuir valor padrão
		if (xString.isEmpty() || yString.isEmpty()) {
			System.out.println("alguma coordenada vazia, resetando para ultima coordenada");
			usarUltimaCoordenada();
            return;
		}
		if (!isSomenteNumeros(xString) && !isSomenteNumeros(yString)) {
			System.out.println("Tem letras, resetando para ultima coordenada");
			usarUltimaCoordenada();
            return;
		}
		
		try {
	        this.x = Integer.parseInt(xString);
	        this.y = Integer.parseInt(yString);
	    } catch (NumberFormatException e) {
	    	usarUltimaCoordenada();
            System.out.println("Erro de conversão, usando última coordenada.");
	    }
	    
	    
	    System.out.println("Final coordenada: " + this.x + " " + this.y);
	    // Atualiza a última coordenada
	    salvarCoordenadaValida(new Coordenadas(this.x, this.y));

	}
	
	@Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
	
	// Função que verifica se uma string contém apenas números
    public boolean isSomenteNumeros(String str) {
    	return str.matches("^[0-9]+$"); // Garante que a string tenha apenas números
    }
    
    private void fallbackUltimaCoordenda() {
    	tentativasInvalidas++;
        if (tentativasInvalidas >= MAX_TENTATIVAS_INVALIDAS) {
        	this.x = 100;
            this.y = 100;
            tentativasInvalidas = 0;
            System.out.println("Caiu no fallback de 100 100");
        }
    }
    
    // Adiciona uma coordenada válida à lista
    private void salvarCoordenadaValida(Coordenadas coordenada) {
        if (coordenadasValidas.size() >= MAX_COORDENADAS) {
            coordenadasValidas.remove(0); // Remove a coordenada mais antiga
        }
        coordenadasValidas.add(coordenada);
    }
    
    // Usa a última coordenada válida salva
    private void usarUltimaCoordenada() {
        if (coordenadasValidas.isEmpty()) {
            // Caso não haja coordenadas válidas na lista, usar valores padrão
            this.x = 100;
            this.y = 100;
            System.out.println("Nenhuma coordenada válida encontrada. Usando valores padrão (100, 100).");
        } else {
        	Coordenadas ultima = new Coordenadas(0,0);
        	if (coordenadasValidas.size() == 10) {
        		/*Random random = new Random();
        		ultima = coordenadasValidas.get(random.nextInt(coordenadasValidas.size()));*/
        		int somaX = 0, somaY = 0;
        		for (Coordenadas c : coordenadasValidas) {
        			somaX += c.x;
        			somaY += c.y;
        		}
        		int mediaX = somaX / coordenadasValidas.size();
        		int mediaY = somaY / coordenadasValidas.size();
        		ultima = new Coordenadas(mediaX, mediaY);
        		System.out.println("Cheio 10 pegando a media");
        	} else {
        		ultima = coordenadasValidas.get(coordenadasValidas.size() - 1);
        		System.out.println("CAIU NO ULTIMO DA LISTA HEHE BOY");
        	}
            this.x = ultima.x;
            this.y = ultima.y;
            System.out.println("Usando última coordenada válida: " + ultima);
        }
        //fallbackUltimaCoordenda();
    }
	
}
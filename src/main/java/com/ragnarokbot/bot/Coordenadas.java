package com.ragnarokbot.bot;

public class Coordenadas {
	
	public int x;
	public int y;
	
	public Coordenadas(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coordenadas(String coordenada) {
		// Usar expressão regular para capturar apenas números
	    String[] substrings = coordenada.split(" ");
	    String xString = substrings[0].replaceAll("[^0-9]", ""); // Remover tudo que não é número
	    String yString = substrings[1].replaceAll("[^0-9]", ""); // Remover tudo que não é número
	    
	    
		System.out.println("Tamanho vetor: " + substrings.length + " | x: " + substrings[0] + " y: " + substrings[1]);
		
	 // Tratar erros de conversão e atribuir valor padrão
	    try {
	        this.x = xString.isEmpty() ? 150 : Integer.parseInt(xString);
	        this.y = yString.isEmpty() ? 150 : Integer.parseInt(yString);
	    } catch (NumberFormatException e) {
	        // Caso o número seja inválido, atribuir 300
	        this.x = 150;
	        this.y = 150;
	        System.out.println("Caiu no catch");
	    }
	    

	}
	
	@Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
	
}
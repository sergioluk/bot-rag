package com.ragnarokbot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class AStar {
	
	public List<Coordenadas> calcularCaminhoComExpansao10(Coordenadas inicio, Coordenadas destino, GrafoMapa grafo) {
	    // Calcula o caminho completo usando o método existente
	    //List<Coordenadas> caminho = calcularCaminhoComExpansao(inicio, destino, grafo);
		List<Coordenadas> caminho = encontrarCaminho(grafo ,inicio, destino );

	    // Retorna vazio se o caminho não tiver coordenadas
	    if (caminho.isEmpty()) {
	        return Collections.emptyList();
	    }

	    // Determina o número de coordenadas por salto
	    int salto = caminho.size() < 10 ? 3 : 5; // 3 5

	    // Lista para armazenar o caminho dividido
	    List<Coordenadas> caminhoDividido = new ArrayList<>();

	    // Adiciona coordenadas com base no salto
	    for (int i = 0; i < caminho.size(); i += salto) {
	        caminhoDividido.add(caminho.get(i));
	    }

	    // Garante que a última coordenada seja incluída, se ainda não estiver
	    if (!caminhoDividido.contains(caminho.get(caminho.size() - 1))) {
	        caminhoDividido.add(caminho.get(caminho.size() - 1));
	    }

	    return caminhoDividido;
	}
	
	public List<Coordenadas> calcularCaminhoComExpansao(Coordenadas inicio, Coordenadas destino, GrafoMapa grafo) {
		
		// Verifica se há nodos no grafo
	    if (grafo.adjacencias.isEmpty()) {
	        return Collections.emptyList(); // Retorna lista vazia se o grafo estiver vazio
	    }
		
        // Encontra os nodos mais próximos do início e do destino
		Coordenadas nodoMaisProximoInicio = encontrarNodoMaisProximo(inicio, grafo.adjacencias.keySet());
		Coordenadas nodoMaisProximoDestino = encontrarNodoMaisProximo(destino, grafo.adjacencias.keySet());
		
		// Se não houver nodos próximos, retorna uma lista vazia
	    if (nodoMaisProximoInicio == null || nodoMaisProximoDestino == null) {
	        return Collections.emptyList();
	    }

        // Cria um grafo temporário para o cálculo
        GrafoMapa grafoExpandido = new GrafoMapa();

        // Copia todos os nodos e conexões do grafo original
        for (Map.Entry<Coordenadas, List<Coordenadas>> entry : grafo.adjacencias.entrySet()) {
        	Coordenadas nodo = entry.getKey();
            for (Coordenadas vizinho : entry.getValue()) {
                grafoExpandido.addConexao(nodo, vizinho);
            }
        }

        // Adiciona conexões temporárias para os nodos mais próximos
        grafoExpandido.addConexao(inicio, nodoMaisProximoInicio);
        grafoExpandido.addConexao(destino, nodoMaisProximoDestino);

        // Calcula o caminho usando A*
        return encontrarCaminho(grafoExpandido, inicio, destino);
    }

    private Coordenadas encontrarNodoMaisProximo(Coordenadas origem, Set<Coordenadas> nodos) {
        return nodos.stream()
                .min(Comparator.comparingDouble(n -> heuristica(origem, n)))
                .orElse(null); // Retorna null se não houver nodos
    }

    public List<Coordenadas> encontrarCaminho(GrafoMapa grafo, Coordenadas inicio, Coordenadas destino) {
        Map<Coordenadas, Coordenadas> caminho = new HashMap<>();
        Map<Coordenadas, Double> gScore = new HashMap<>();
        Map<Coordenadas, Double> fScore = new HashMap<>();
        PriorityQueue<Coordenadas> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> fScore.getOrDefault(n, Double.MAX_VALUE)));

        gScore.put(inicio, 0.0);
        fScore.put(inicio, heuristica(inicio, destino));
        openSet.add(inicio);

        while (!openSet.isEmpty()) {
        	Coordenadas atual = openSet.poll();

            if (atual.equals(destino)) {
                return reconstruirCaminho(caminho, atual);
            }

            for (Coordenadas vizinho : grafo.getVizinhos(atual)) {
                double tentativeGScore = gScore.get(atual) + distancia(atual, vizinho);
                if (tentativeGScore < gScore.getOrDefault(vizinho, Double.MAX_VALUE)) {
                    caminho.put(vizinho, atual);
                    gScore.put(vizinho, tentativeGScore);
                    fScore.put(vizinho, tentativeGScore + heuristica(vizinho, destino));
                    if (!openSet.contains(vizinho)) {
                        openSet.add(vizinho);
                    }
                }
            }
        }

        return Collections.emptyList(); // Sem caminho possível
    }

    private List<Coordenadas> reconstruirCaminho(Map<Coordenadas, Coordenadas> caminho, Coordenadas atual) {
        List<Coordenadas> totalCaminho = new ArrayList<>();
        while (caminho.containsKey(atual)) {
            totalCaminho.add(atual);
            atual = caminho.get(atual);
        }
        totalCaminho.add(atual); // Adiciona o nodo inicial
        Collections.reverse(totalCaminho);
        return totalCaminho;
    }

    private double heuristica(Coordenadas a, Coordenadas b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private double distancia(Coordenadas a, Coordenadas b) {
        return 1.0; // Supondo custo constante
    }
}

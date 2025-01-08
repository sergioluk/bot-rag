package com.ragnarokbot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrafoMapa {
	Map<Coordenadas, List<Coordenadas>> adjacencias = new HashMap<>();

    public void addConexao(Coordenadas a, Coordenadas b) {
        adjacencias.putIfAbsent(a, new ArrayList<>());
        adjacencias.putIfAbsent(b, new ArrayList<>());
        adjacencias.get(a).add(b);
        adjacencias.get(b).add(a);
    }

    public List<Coordenadas> getVizinhos(Coordenadas nodo) {
        return adjacencias.getOrDefault(nodo, new ArrayList<>());
    }
    
    public void printConexoes() {
        for (Map.Entry<Coordenadas, List<Coordenadas>> entry : adjacencias.entrySet()) {
            Coordenadas origem = entry.getKey();
            List<Coordenadas> destinos = entry.getValue();
            System.out.println("Origem: " + origem);
            for (Coordenadas destino : destinos) {
                System.out.println("  -> Destino: " + destino);
            }
        }
    }
}

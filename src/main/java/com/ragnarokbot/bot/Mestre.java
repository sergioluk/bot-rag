package com.ragnarokbot.bot;

import java.io.*;
import java.net.*;
import java.util.*;

import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.enums.Comando;

public class Mestre {
	public static final int PORT = 5000;
	public static List<Socket> slaves = new ArrayList<>();
    
    public static void enviarComando(Comando comando) {
        for (Socket s : slaves) {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                out.println(comando.name()); // Envia o nome do Enum
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void enviarComando(Comando comando, Coordenadas destino) {
    	int contador = 0;
        for (Socket s : slaves) {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                String msg = comando.name() + "|" + (destino.x + contador) + "|" + destino.y;
                out.println(msg); // Envia o nome do Enum
                contador++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void enviarComando(Comando comando, String sala) {
        for (Socket s : slaves) {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                String msg = comando.name() + "|" + sala;
                out.println(msg); // Envia o nome do Enum
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

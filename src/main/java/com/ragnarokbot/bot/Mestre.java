package com.ragnarokbot.bot;

import java.io.*;
import java.net.*;
import java.util.*;

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

}

package com.ragnarokbot.main;

import org.opencv.core.MatOfPoint;

import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.model.Coordenadas;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameController {
	
	private final Bot bot;
    private String estado = "andando";
    private int rota = 0;
    
    public GameController(Bot bot) {
        this.bot = bot;
    }
    
    public void run() throws Exception {
        boolean ligarBot = true;
         
        List<Coordenadas> caminho = inicializarCaminho();

        while (ligarBot) {
            Thread.sleep(2000);
            List<MatOfPoint> monstros = bot.listaMonstros();
            
            

            if (!monstros.isEmpty()) {
                estado = "atacando";
                System.out.println("Estado: " + estado);
            }

            if ("andando".equals(estado)) {
                andar(caminho);
                System.out.println("Estado: " + estado);
                System.out.println("Rota destino: " + caminho.get(rota) + "|| " + (rota + 1));
            } else if ("atacando".equals(estado)) {
                atacar(monstros);
                estado = "andando"; // Volta a andar após atacar
            }
        }
    }
    
    private List<Coordenadas> inicializarCaminho() {
        List<Coordenadas> caminho = new ArrayList<>();
        /*caminho.add(new Coordenadas(57, 36));
        caminho.add(new Coordenadas(62, 72));
        caminho.add(new Coordenadas(74, 120));
        caminho.add(new Coordenadas(94, 118));
        caminho.add(new Coordenadas(110, 83));
        caminho.add(new Coordenadas(104, 38));*/
        
        caminho.add(new Coordenadas(43, 250));
        caminho.add(new Coordenadas(58, 233));
        caminho.add(new Coordenadas(74, 217));
        caminho.add(new Coordenadas(101, 207));
        caminho.add(new Coordenadas(111, 162));
        caminho.add(new Coordenadas(105, 117));
        caminho.add(new Coordenadas(63, 68));
        caminho.add(new Coordenadas(56, 33));
        
        return caminho;
    }
    
    private void andar(List<Coordenadas> caminho) throws Exception {
        String coordenadaXY = bot.ocr(1694, 177, 53, 12);
        Coordenadas atual = new Coordenadas(coordenadaXY);

        //Verificar se chegou no destino
        int distanciaMinima = 10;
        Coordenadas destino = caminho.get(rota);
        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
            rota++; // Próximo ponto no caminho
            System.out.println("Rota aumentada!");
            if (rota >= caminho.size()) {
	            rota = 0; // Reiniciar o caminho
	            //return;
	        }
        } else {
            bot.moverPersonagem(atual, destino);
        }
    }
    
    private void atacar(List<MatOfPoint> monstros) throws Exception {
        monstros.sort(Comparator.comparingDouble(m -> 
            bot.calcularDistanciaCentro(m)
        ));

        for (MatOfPoint monstro : monstros) {
            bot.atacarMonstro(monstro);
            break; // Ataca um monstro e sai
        }
    }

}

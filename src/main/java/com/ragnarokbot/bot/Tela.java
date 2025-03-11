package com.ragnarokbot.bot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.ragnarokbot.main.GameController;
import com.ragnarokbot.telas.JanelaPrincipal;
import com.sun.jna.platform.win32.User32;

import config.ConfigManager;

public class Tela extends JFrame{
    
    private Bot bot;
    
    private static Map<Integer, Cronometro> cronometros = new HashMap<>();
    
    private static boolean velocidade = false;
    private static boolean chicleteGoma = false;
    
    public Tela( Bot bot) {
        super("Stonks");
        this.bot = bot;
        
        setUndecorated(true); // Remove bordas da janela
        setSize(bot.getWidth(), bot.getHeight());
        setLocation(bot.getxJanela(), bot.getyJanela());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Tornar a janela sempre visível acima das outras
        setAlwaysOnTop(true); // A janela ficará sempre em cima
        // Tornar a janela transparente e permitir os cliques passarem para as janelas de fundo
        setBackground(new Color(0, 0, 0, 0)); // Janela transparente
        setOpacity(0.2f); // Ajuste a opacidade conforme necessário
        setFocusableWindowState(false); // Permite que os cliques passem para as janelas de fundo


        // Configurar a área de desenho
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (Cronometro cronometro : cronometros.values()) {
                    cronometro.desenhar(g);
                }
                updateVeloGoma();
                g.setColor(Color.GREEN);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                String velo = "Velocidade: " + velocidade;
                g.drawString(velo, 561, 25);
                String goma = "Chiclete/Goma: " + chicleteGoma;
                g.drawString(goma, 561, 42);
            }
        };
        panel.setOpaque(false); // Deixa o painel transparente
        add(panel);
        setVisible(true);
    }
    
    public static void iniciarCronometro(int id, int tempo, Tela tela) {
        int x = 610;
        int y = (id == 1) ? 15 : 35;
        Cronometro cronometro = new Cronometro(x, y, tempo, id);
        cronometros.put(id, cronometro);
        
        // Iniciar o cronômetro com o tempo
        cronometro.iniciar(tempo);

        // Criar um Timer do Swing para atualizar o cronômetro
        Timer timer = new Timer(1000, e -> {
            if (cronometro.diminuirTempo()) {
                SwingUtilities.invokeLater(tela::repaint); // Atualiza a tela corretamente
            } else {
                ((Timer) e.getSource()).stop(); // Para o Timer quando o tempo acabar
            }
        });
        timer.start();
    }
    
    public void updateVeloGoma() {
    	velocidade = JanelaPrincipal.isVelocidade;
    	chicleteGoma = JanelaPrincipal.isChicleteGoma;
    	repaint();
    }
    
    
    // Classe interna para o cronômetro
    static class Cronometro {
        private int x, y;
        private int tempoSegundos;
        private boolean iniciado;
        private int tempoInicial;
        private String nome;

        public Cronometro(int x, int y, int minutos, int id) {
            this.x = x;
            this.y = y;
            this.tempoSegundos = -1;// Inicializa com um valor negativo para não exibir na tela antes de iniciar
            this.iniciado = false;
            this.nome = id == 1 ? "Velocidade" : "Goma"; 
        }

        public boolean diminuirTempo() {
            if (tempoSegundos > 0) {
                tempoSegundos--;
                return true;
            }
            return false;
        }
        
        public void iniciar(int minutos) {
        	 if (!iniciado) {
                 this.tempoSegundos = minutos * 60; // Inicia o cronômetro com o tempo correto
                 this.iniciado = true; // Marca como iniciado
                 this.tempoInicial = tempoSegundos;
             }
        }

        public void desenhar(Graphics g) {
        	if (tempoSegundos > 0) {
        		if (tempoSegundos == tempoInicial) {
            		return;
            	}
                // Limpar a área onde o cronômetro é exibido antes de desenhar o novo valor
                g.setColor(new Color(0, 0, 0, 0)); // Cor transparente (ou cor de fundo)
                g.fillRect(x - 2, y - 14, 100, 20); // Preenche a área com a cor transparente

                g.setColor(Color.GREEN);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                String tempoFormatado = String.format("%02d:%02d:%02d %s", tempoSegundos / 3600, (tempoSegundos % 3600) / 60, tempoSegundos % 60, nome);
                g.drawString(tempoFormatado, x, y);
            }
        }
    }
    
    

}

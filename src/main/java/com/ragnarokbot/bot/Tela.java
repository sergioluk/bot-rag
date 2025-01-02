package com.ragnarokbot.bot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.sun.jna.platform.win32.User32;

import config.ConfigManager;

public class Tela extends JFrame{

	private Point startPoint; // Ponto inicial do clique
    private Point endPoint;   // Ponto final ao arrastar o mouse
    private Rectangle rectangle; // Retângulo desenhado
    
    private Bot bot;
    
    public Tela( Bot bot) {
        super("RagfukinBot");
        this.bot = bot;
        
        setUndecorated(true); // Remove bordas da janela
//        setBackground(new Color(0, 0, 0, 10)); // Torna a janela completamente transparente
        //setAlwaysOnTop(true); // Mantém a janela sempre acima de outras
        setSize(bot.getWidth(), bot.getHeight());
        setLocation(bot.getxJanela(), bot.getyJanela());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
     // Definir o GlassPane
        MyGlassPane glassPane = new MyGlassPane();
        setGlassPane(glassPane);
        glassPane.setVisible(true);
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
                if (rectangle != null) {
                    g.setColor(new Color(0, 255, 0, 128));
                    g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                    
                    //g.drawRect(233, 192, 560, 403);
                    
                    
                }
            }
        };
        panel.setOpaque(false); // Deixa o painel transparente
        
        // Desenhar o retângulo se ja estiver salvo
        if (bot.configOCR != null && bot.configOCR.rectangle != null && bot.configOCR.rectangle.x != 0) {
            rectangle = new Rectangle(
                bot.configOCR.rectangle.x,
                bot.configOCR.rectangle.y,
                bot.configOCR.rectangle.width,
                bot.configOCR.rectangle.height
            );
        }
        
        // Listeners para eventos do mouse
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = null;
                rectangle = null;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (startPoint != null && endPoint != null) {
                    System.out.println("Coordenadas do Retângulo:");
                    System.out.println("X: " + rectangle.x + ", Y: " + rectangle.y);
                    System.out.println("Largura: " + rectangle.width + ", Altura: " + rectangle.height);
                    
                    //Salvando as informações no arquivo config.json
                    bot.configOCR.rectangle.x = rectangle.x;
                    bot.configOCR.rectangle.y = rectangle.y;
                    bot.configOCR.rectangle.width = rectangle.width;
                    bot.configOCR.rectangle.height = rectangle.height;
                    ConfigManager.saveConfig(bot.configOCR);
                    
                    //Carregando as informações do arquivo config.json
                    bot.configOCR = ConfigManager.loadConfig();
                    
                    //Focar no ragnarok
                    User32.INSTANCE.SetForegroundWindow(Bot.hwnd);
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                rectangle = calculateRectangle(startPoint, endPoint);
                repaint();
            }
        });

        add(panel);
        setVisible(true);
    }
    
 // Método para calcular o retângulo com base nos dois pontos
    private Rectangle calculateRectangle(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Rectangle(x, y, width, height);
    }
 // Classe do GlassPane que desenha o retângulo
    class MyGlassPane extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (rectangle != null) {
                g.setColor(new Color(0, 255, 0, 128));
                g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            }
        }
    }

}

package com.ragnarokbot.bot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.jna.platform.win32.User32;

public class Tela extends JFrame{

	private Point startPoint; // Ponto inicial do clique
    private Point endPoint;   // Ponto final ao arrastar o mouse
    private Rectangle rectangle; // Retângulo desenhado
    
    private Bot bot;
    
    public Tela( Bot bot) {
        super("RagfukinBot");
        this.bot = bot;
        
        setUndecorated(true); // Remove bordas da janela
        setBackground(new Color(0, 0, 0, 10)); // Torna a janela completamente transparente
        //setAlwaysOnTop(true); // Mantém a janela sempre acima de outras
        setSize(bot.getWidth(), bot.getHeight());
        setLocation(bot.getxJanela(), bot.getyJanela());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Configurar a área de desenho
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (rectangle != null) {
                    g.setColor(new Color(0, 255, 0, 128));
                    g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                }
            }
        };
        panel.setOpaque(false); // Deixa o painel transparente
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
                    bot.setxOcrCoordenadas(rectangle.x);
                    bot.setyOcrCoordenadas(rectangle.y);
                    bot.setWidthOcrCoordenadas(rectangle.width);
                    bot.setHeightOcrCoordenadas(rectangle.height);
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

}

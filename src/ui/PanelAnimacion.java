package ui;

import model.GestorTrafico;
import model.HiloPeaton;
import model.HiloVehiculo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelAnimacion extends JPanel {

    private final GestorTrafico gestor;
    private final List<HiloVehiculo> listaVehiculos;
    private final List<HiloPeaton> listaPeatones;

    public PanelAnimacion(GestorTrafico gestor, List<HiloVehiculo> vehiculos, List<HiloPeaton> peatones) {
        this.gestor = gestor;
        this.listaVehiculos = vehiculos;
        this.listaPeatones = peatones;
        setBackground(new Color(35, 39, 42));

        Timer timer = new Timer(30, e -> repaint());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        renderizarEscenario(g2, w, h);
        renderizarSemaforo(g2);
        renderizarVehiculos(g2);
        renderizarPeatones(g2);
    }

    private void renderizarEscenario(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(65, 70, 75));
        g2.fillRect(0, 0, 300, h);
        g2.fillRect(660, 0, w - 660, h);

        g2.setColor(new Color(45, 52, 58));
        g2.fillRect(300, 0, 360, h);

        g2.setColor(new Color(230, 230, 230));
        Stroke originalStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{20, 15}, 0));
        g2.drawLine(480, 0, 480, h);
        g2.setStroke(originalStroke);

        g2.setColor(Color.WHITE);
        g2.fillRect(300, 260, 360, 10);

        for (int y = 330; y < 450; y += 22) {
            g2.fillRect(310, y, 340, 12);
        }
    }

    private void renderizarSemaforo(Graphics2D g2) {
        // Estructura del semáforo (3 luces)
        g2.setColor(new Color(20, 20, 20));
        g2.fillRoundRect(680, 50, 60, 185, 15, 15);
        g2.setColor(new Color(90, 90, 90));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(680, 50, 60, 185, 15, 15);

        GestorTrafico.EstadoSemaforo est = gestor.getEstado();

        // 1. ROJO (Arriba)
        if (est == GestorTrafico.EstadoSemaforo.ROJO) {
            g2.setColor(new Color(231, 76, 60)); // Encendido
        } else {
            g2.setColor(new Color(60, 15, 15));  // Apagado
        }
        g2.fillOval(692, 60, 36, 36);

        // 2. AMARILLO (Centro)
        if (est == GestorTrafico.EstadoSemaforo.AMARILLO) {
            g2.setColor(new Color(241, 196, 15)); // Encendido
        } else {
            g2.setColor(new Color(65, 55, 10));  // Apagado
        }
        g2.fillOval(692, 115, 36, 36);

        // 3. VERDE (Abajo)
        if (est == GestorTrafico.EstadoSemaforo.VERDE) {
            g2.setColor(new Color(46, 204, 113)); // Encendido
        } else {
            g2.setColor(new Color(15, 60, 25));   // Apagado
        }
        g2.fillOval(692, 170, 36, 36);

        // Texto informativo del estado
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String texto = (est == GestorTrafico.EstadoSemaforo.VERDE) ? "PASO COCHES" :
                (est == GestorTrafico.EstadoSemaforo.AMARILLO) ? "PRECAUCIÓN" : "PASO PEATONES";
        g2.drawString(texto, 665, 260);
    }

    private void renderizarVehiculos(Graphics2D g2) {
        synchronized (listaVehiculos) {
            for (HiloVehiculo v : listaVehiculos) {
                if (!v.isFinalizado()) {
                    g2.setColor(new Color(52, 152, 219));
                    g2.fillRoundRect(v.getX(), v.getY(), 48, 80, 10, 10);

                    g2.setColor(new Color(26, 82, 118));
                    g2.drawRoundRect(v.getX(), v.getY(), 48, 80, 10, 10);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString("V" + v.getId(), v.getX() + 14, v.getY() + 45);
                }
            }
        }
    }

    private void renderizarPeatones(Graphics2D g2) {
        synchronized (listaPeatones) {
            for (HiloPeaton p : listaPeatones) {
                if (!p.isFinalizado()) {
                    g2.setColor(new Color(241, 196, 15));
                    g2.fillOval(p.getX(), p.getY(), 20, 20);

                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString("P" + p.getId(), p.getX() + 2, p.getY() + 14);
                }
            }
        }
    }
}
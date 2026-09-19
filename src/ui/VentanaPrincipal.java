package ui;

import model.GestorTrafico;
import model.HiloPeaton;
import model.HiloVehiculo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private final GestorTrafico gestor = new GestorTrafico();
    private final List<HiloVehiculo> listaVehiculos = Collections.synchronizedList(new ArrayList<>());
    private final List<HiloPeaton> listaPeatones = Collections.synchronizedList(new ArrayList<>());
    private final List<int[]> posicionesPeatones = Collections.synchronizedList(new ArrayList<>());

    private volatile boolean simulacionActiva = false;
    private Thread hiloGenerador;
    private JButton btnAutoSimulacion;

    public VentanaPrincipal() {
        super("Simulador de Interseccion con Semaforos");
        setSize(980, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Actualiza el cache de coordenadas de peatones en tiempo real
        Timer sincronizador = new Timer(25, e -> actualizarCoordenadasPeatones());
        sincronizador.start();

        PanelAnimacion panelAnimacion = new PanelAnimacion(gestor, listaVehiculos, listaPeatones);
        add(panelAnimacion, BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panelControles.setBackground(new Color(24, 28, 36));

        btnAutoSimulacion = crearBoton("Iniciar Simulación Automática", new Color(39, 174, 96));
        JButton btnVehiculo = crearBoton("+1 Vehículo", new Color(41, 128, 185));
        JButton btnPeaton = crearBoton("+1 Peatón", new Color(211, 84, 0));

        btnAutoSimulacion.addActionListener(e -> alternarSimulacion());
        btnVehiculo.addActionListener(e -> despacharVehiculo());
        btnPeaton.addActionListener(e -> despacharPeaton());

        panelControles.add(btnAutoSimulacion);
        panelControles.add(btnVehiculo);
        panelControles.add(btnPeaton);

        add(panelControles, BorderLayout.SOUTH);
    }

    private void actualizarCoordenadasPeatones() {
        synchronized (listaPeatones) {
            synchronized (posicionesPeatones) {
                posicionesPeatones.clear();
                for (HiloPeaton p : listaPeatones) {
                    if (!p.isFinalizado()) {
                        posicionesPeatones.add(new int[]{p.getX(), p.getY()});
                    }
                }
            }
        }
    }

    private synchronized void alternarSimulacion() {
        if (!simulacionActiva) {
            simulacionActiva = true;
            btnAutoSimulacion.setText("Detener Simulación");
            btnAutoSimulacion.setBackground(new Color(192, 57, 43));

            hiloGenerador = new Thread(() -> {
                long ultimoSpawnAuto = 0;
                long ultimoSpawnPeaton = 0;
                long proximoDeltaAuto = 1800;
                long proximoDeltaPeaton = 1200;

                try {
                    while (simulacionActiva) {
                        long ahora = System.currentTimeMillis();

                        if (ahora - ultimoSpawnAuto >= proximoDeltaAuto) {
                            despacharVehiculo();
                            ultimoSpawnAuto = ahora;
                            proximoDeltaAuto = 1500 + (long)(Math.random() * 2000);
                        }

                        if (ahora - ultimoSpawnPeaton >= proximoDeltaPeaton) {
                            despacharPeaton();
                            ultimoSpawnPeaton = ahora;
                            proximoDeltaPeaton = 1000 + (long)(Math.random() * 1800);
                        }

                        Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
            hiloGenerador.setDaemon(true);
            hiloGenerador.start();

        } else {
            simulacionActiva = false;
            btnAutoSimulacion.setText("Iniciar Simulación Automática");
            btnAutoSimulacion.setBackground(new Color(39, 174, 96));
            if (hiloGenerador != null) {
                hiloGenerador.interrupt();
            }
        }
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void despacharVehiculo() {
        int xCarril = Math.random() > 0.5 ? 360 : 540;
        int yMinima = -90;
        synchronized (listaVehiculos) {
            for (HiloVehiculo v : listaVehiculos) {
                if (!v.isFinalizado() && Math.abs(v.getX() - xCarril) < 20) {
                    if (v.getY() < yMinima + 110) {
                        yMinima = v.getY() - 110;
                    }
                }
            }
        }

        // Conexión por lambda/desacoplada con GestorTrafico
        HiloVehiculo vehiculo = new HiloVehiculo(
                (x, y) -> gestor.puedeCruzarCarril(x, y, posicionesPeatones),
                listaVehiculos,
                posicionesPeatones,
                xCarril,
                yMinima
        );

        synchronized (listaVehiculos) {
            listaVehiculos.add(vehiculo);
        }
        new Thread(vehiculo).start();
    }

    private void despacharPeaton() {
        int yAleatoria = 335 + (int)(Math.random() * 105);
        int xOffset = 760 + (int)(Math.random() * 150);

        // Conexión por referencia a método/desacoplada con GestorTrafico
        HiloPeaton peaton = new HiloPeaton(
                gestor::puedeEntrarPeaton,
                xOffset,
                yAleatoria
        );

        synchronized (listaPeatones) {
            listaPeatones.add(peaton);
        }
        new Thread(peaton).start();
    }
}
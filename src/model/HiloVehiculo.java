package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HiloVehiculo implements Runnable {

    @FunctionalInterface
    public interface ValidadorCruceVehiculo {
        boolean puedePasar(int xCarril, int yActual);
    }

    private static int contador = 0;
    private final int id;
    private final ValidadorCruceVehiculo validadorCruce;
    private final List<HiloVehiculo> listaVehiculos;
    private final List<int[]> posicionesPeatones;
    private int x;
    private int y;
    private final int velocidad = 4;
    private final int yLineaFreno = 175;
    private final int altoAuto = 80;
    private final int anchoAuto = 48;
    private final int distanciaSeguridad = 20;
    private boolean finalizado = false;

    public HiloVehiculo(ValidadorCruceVehiculo validadorCruce, List<HiloVehiculo> listaVehiculos, List<int[]> posicionesPeatones, int x, int y) {
        this.id = ++contador;
        this.validadorCruce = validadorCruce;
        this.listaVehiculos = listaVehiculos;
        this.posicionesPeatones = posicionesPeatones;
        this.x = x;
        this.y = y;
    }

    @Override
    public void run() {
        try {
            while (y < 850) {
                if (hayVehiculoEnfrente()) {
                    Thread.sleep(30);
                    continue;
                }

                if (y < yLineaFreno && (y + velocidad) >= yLineaFreno && validadorCruce != null && !validadorCruce.puedePasar(this.x, y)) {
                    Thread.sleep(30);
                    continue;
                }

                if (hayPeatonEnTrayectoria()) {
                    Thread.sleep(30);
                    continue;
                }

                y += velocidad;
                Thread.sleep(25);
            }
            finalizado = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean hayVehiculoEnfrente() {
        if (listaVehiculos == null) return false;
        synchronized (listaVehiculos) {
            for (HiloVehiculo otro : listaVehiculos) {
                if (otro.id == this.id || otro.isFinalizado()) {
                    continue;
                }
                if (Math.abs(otro.getX() - this.x) < 20) {
                    int distanciaY = otro.getY() - this.y;
                    if (distanciaY > 0 && distanciaY < (altoAuto + distanciaSeguridad)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hayPeatonEnTrayectoria() {
        if (posicionesPeatones == null) return false;
        synchronized (posicionesPeatones) {
            for (int[] p : posicionesPeatones) {
                int px = p[0];
                int py = p[1];
                boolean enMismoX = px >= (this.x - 20) && px <= (this.x + anchoAuto + 10);
                int distanciaY = py - (this.y + altoAuto);
                if (enMismoX && distanciaY >= -15 && distanciaY <= 35) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getId() { return id; }
    public boolean isFinalizado() { return finalizado; }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[TEST UNITARIO 3] Probando frenado en cola de HiloVehiculo...");
        List<HiloVehiculo> autos = Collections.synchronizedList(new ArrayList<>());

        ValidadorCruceVehiculo cruceLibre = (x, y) -> true;

        HiloVehiculo v1 = new HiloVehiculo(cruceLibre, autos, null, 360, 50);
        HiloVehiculo v2 = new HiloVehiculo(cruceLibre, autos, null, 360, -60);
        autos.add(v1);
        autos.add(v2);

        new Thread(v1).start();
        new Thread(v2).start();

        for (int i = 0; i < 20; i++) {
            System.out.printf("V1 y=%d | V2 y=%d | Separacion=%d px%n",
                    v1.getY(), v2.getY(), (v1.getY() - v2.getY()));
            Thread.sleep(300);
            if (v1.isFinalizado() && v2.isFinalizado()) break;
        }
        System.out.println("[TEST UNITARIO 3] Recorrido completado sin solapes.");
    }
}
//lo que hice fue gestionar el funcionamiento
// del semaforo, si esta en verde puede pasar y si no pues no puede pasar
package model;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GestorTrafico {

    public enum EstadoSemaforo {
        VERDE, AMARILLO, ROJO
    }

    private volatile EstadoSemaforo estado = EstadoSemaforo.VERDE;

    public GestorTrafico() {
        iniciarCiclo();
    }

    private void iniciarCiclo() {
        Thread ciclo = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    estado = EstadoSemaforo.VERDE;
                    TimeUnit.SECONDS.sleep(6);

                    estado = EstadoSemaforo.AMARILLO;
                    TimeUnit.SECONDS.sleep(3);

                    estado = EstadoSemaforo.ROJO;
                    TimeUnit.SECONDS.sleep(7);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        ciclo.setDaemon(true);
        ciclo.start();
    }

    public EstadoSemaforo getEstado() {
        return estado;
    }

    public boolean puedeCruzarCarril(int xCarril, int yActual, List<int[]> posicionesPeatones) {
        if (yActual > 175) {
            return true;
        }

        if (estado == EstadoSemaforo.ROJO) {
            return false;
        }

        if (xCarril <= 480) {
            if (posicionesPeatones != null) {
                synchronized (posicionesPeatones) {
                    for (int[] pos : posicionesPeatones) {
                        int xP = pos[0];
                        if (xP < 665 && xP > 270) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        if (estado != EstadoSemaforo.VERDE) {
            return false;
        }

        if (posicionesPeatones != null) {
            synchronized (posicionesPeatones) {
                for (int[] pos : posicionesPeatones) {
                    int xP = pos[0];
                    if (xP <= 665 && xP >= 460) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean puedeEntrarPeaton(int xActual) {
        if (xActual < 665) {
            return true;
        }
        return estado == EstadoSemaforo.ROJO;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[TEST UNITARIO 1] Iniciando ciclo del semaforo...");
        GestorTrafico gestor = new GestorTrafico();

        for (int i = 0; i < 16; i++) {
            System.out.printf("T=%02ds | Estado: %-9s | ¿Peaton puede entrar?: %b%n",
                    i, gestor.getEstado(), gestor.puedeEntrarPeaton(700));
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
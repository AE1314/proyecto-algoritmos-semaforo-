package model;

import java.util.function.Predicate;

public class HiloPeaton implements Runnable {
    private static int contador = 0;
    private final int id;
    private final Predicate<Integer> validadorPaso;
    private int x;
    private int y;
    private final int velocidad = 2;
    private final int xBordeEntrada = 665;
    private boolean finalizado = false;

    public HiloPeaton(Predicate<Integer> validadorPaso, int x, int y) {
        this.id = ++contador;
        this.validadorPaso = validadorPaso;
        this.x = x;
        this.y = y;
    }

    @Override
    public void run() {
        try {
            while (x > -40) {
                if (x - velocidad <= xBordeEntrada && validadorPaso != null && !validadorPaso.test(x)) {
                    Thread.sleep(40);
                    continue;
                }

                x -= velocidad;
                Thread.sleep(25);
            }
            finalizado = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getId() { return id; }
    public boolean isFinalizado() { return finalizado; }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[TEST UNITARIO 2] Iniciando HiloPeaton con semaforo simulado en lambda...");

        // Simulamos el semaforo: los primeros 3 segundos en falso, luego habilita paso
        long tiempoInicio = System.currentTimeMillis();
        Predicate<Integer> reglaSemaforoPrueba = x -> (System.currentTimeMillis() - tiempoInicio) > 3000;

        HiloPeaton p = new HiloPeaton(reglaSemaforoPrueba, 700, 350);
        Thread t = new Thread(p);
        t.start();

        while (!p.isFinalizado()) {
            System.out.printf("Posicion Peaton: x=%d | Finalizado: %b%n", p.getX(), p.isFinalizado());
            Thread.sleep(400);
        }
        System.out.println("[TEST UNITARIO 2] Peaton cruzo exitosamente.");
    }
}
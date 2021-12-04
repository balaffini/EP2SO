import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class AcessoDireto {
    static Semaphore mutex = new Semaphore(1); //mutex para controlar o acesso no banco de dados
    static List <String> banco = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException{
        File file = new File("bd.txt");
        Scanner scan = new Scanner(file);
        while(scan.hasNext())
            banco.add(scan.nextLine());
        scan.close();

        for(int l = 0; l <= 100; l++) {
            long t50 = 0;
            for (int k = 0; k < 50; k++) {
                List<Thread> t = new LinkedList<>();
                for (int i = 0; i < 100; i++) {
                    if (i < l)
                        t.add(new Leitor());
                    else
                        t.add(new Escritor());
                }
                Collections.shuffle(t);
                long t0 = System.currentTimeMillis();
                t.forEach(Thread::start);
                t.forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t50 += System.currentTimeMillis() - t0;
            }
            System.out.println("Tempo medio de 50 execucoes com " + l + " leitores e " + (100-l) + " escritores: " + t50/50.0 + "ms");
        }
    }

    static class Leitor extends Thread {
        String lida;

        @Override
        public void run() {
            try {
                mutex.acquire(); //down para tentar acessar o banco de dados
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 100; i++)
                    lida = banco.get(r.nextInt(banco.size()));
                sleep(1);
                mutex.release(); //up para liberar o bd
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Escritor extends Thread{
        @Override
        public void run() {
            try {
                mutex.acquire();
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 100; i++) {
                    banco.set(r.nextInt(banco.size()), "MODIFICADO");
                }
                sleep(1);
                mutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

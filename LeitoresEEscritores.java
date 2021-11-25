package EP2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class LeitoresEEscritores {
    static Semaphore mutex = new Semaphore(1);
    static Semaphore bd = new Semaphore(1);
    static int leitores = 0;
    static List <String> banco;

    public static void main(String[] args) throws FileNotFoundException{
        File file = new File("src/EP2/bd.txt");
        Scanner scan = new Scanner(file);
        List <String> hk = new ArrayList<>();
        while(scan.hasNext())
            hk.add(scan.nextLine());
        banco = hk;

        for(int l = 0; l <= 100; l++) {
            List<Thread> t = new LinkedList<>();
            for(int i = 0; i < 100; i++) {
                if(i < l)
                    t.add(new Leitor());
                else
                    t.add(new Escritor());
            }
            Collections.shuffle(t);
            long t0 = System.currentTimeMillis();
            t.forEach(Thread::start);
            t.forEach(thread->{
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            System.out.println(System.currentTimeMillis() - t0);
        }
    }

    static class Leitor extends Thread {
        String lida;

        @Override
        public void run() {
            try {
                mutex.acquire(); //down
                if(++leitores == 1)
                    bd.acquire();
                mutex.release(); //up
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 100; i++)
                    lida = banco.get(r.nextInt(100));
                mutex.acquire();
                if(--leitores == 0)
                    bd.release();
                mutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Escritor extends Thread{
        @Override
        public void run() {
            try {
                bd.acquire();
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 100; i++) {
                    banco.set(r.nextInt(banco.size()), "MODIFICADO");
                }
                sleep(1);
                bd.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
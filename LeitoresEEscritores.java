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
    static Semaphore mutex = new Semaphore(1); //mutex para a variavel contendo o numero de leitores
    static Semaphore bd = new Semaphore(1); //mutex para acesso ao bando de dados
    static int leitores = 0; //quantidade de leitores acessando o banco
    static List <String> banco = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException{
        File file = new File("bd.txt");
        Scanner scan = new Scanner(file);
        while(scan.hasNext())
            banco.add(scan.nextLine()); //alimenta a lista com os dados do arquivo bd.txt
        scan.close();

        for(int l = 0; l <= 100; l++) { //numero de leitores variando de 0 a 100
            long t50 = 0; //tempo de execucao para a media de 50
            for (int k = 0; k < 50; k++) {
                List<Thread> t = new LinkedList<>(); //lista contendo todas as threads de leitores e escritores
                for (int i = 0; i < 100; i++) { //cria a lista com 100 elementos baseado no valor de l
                    if (i < l)
                        t.add(new Leitor());
                    else
                        t.add(new Escritor());
                }
                Collections.shuffle(t); //embaralha a lista para haver diferenca entre os testes
                long t0 = System.currentTimeMillis(); //tempo da execucao atual
                t.forEach(Thread::start); //inicia todas as threads da lista
                t.forEach(thread -> { //executa o join para todas as threads
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t50 += System.currentTimeMillis() - t0; //soma na media de 50 o tempo gasto pela execucao atual
            }
            System.out.println("Tempo medio de 50 execucoes com " + l + " leitores e " + (100-l) + " escritores: " + t50/50.0 + "ms");
        }
    }

    static class Leitor extends Thread {
        String lida;

        @Override
        public void run() {
            try {
                mutex.acquire(); //down para ler a variavel leitores
                if(++leitores == 1) //caso seja o primeiro leitor, trava o bando de dados
                    bd.acquire();
                mutex.release(); //up para liberar o numero de leitores
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 100; i++)
                    lida = banco.get(r.nextInt(banco.size())); //aleatoriamente seleciona uma entrada do banco e le para a variavel local
                sleep(1); //tempo de espera
                mutex.acquire();
                if(--leitores == 0) //caso seja o ultimo leitor, libera o banco de dados
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
                bd.acquire(); //down para tentar acessar o banco
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 100; i++)
                    banco.set(r.nextInt(banco.size()), "MODIFICADO"); //seleciona aleatoriamente uma entrada do banco para modificar
                sleep(1); //tempo de espera
                bd.release(); //up para liberar o banco
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

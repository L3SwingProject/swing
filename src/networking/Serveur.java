package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.BadFormatMessageException;
import main.*;

public class Serveur extends Thread {
    private static NavigableSet<Capteur> list;
    private static NavigableMap<String, Capteur> keyList = new TreeMap<>();
    private int badFormat = 0;
    final static int port = 8952;
    private static boolean isRunning = true;
    private static List<Serveur> serveurs = new ArrayList<>();
    private Socket socket;

    /**
     * Fonction main du thread qui lance le serveur.
     * @param list - list of captors
     */
    public static void listenSimul(NavigableSet<Capteur> list, NavigableMap<String, Capteur> keyList){
        Serveur.list = list;
        Serveur.keyList = keyList;
        try{
            ServerSocket socketServeur = new ServerSocket(port);
            while(isRunning){
                Socket socketClient = socketServeur.accept();
                serveurs.add(new Serveur(socketClient));
                serveurs.get(serveurs.size()-1).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private Serveur(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        traitements();
    }

    private void traitements(){
        try {
            String message;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            badFormat = 0;
            while (!socket.isClosed() && isRunning) {
                message = in.readLine();
                if (message != null) {
                    String[] infos = message.split(" ");
                    System.out.println(message);
                    switch (infos[0]) {
                        case "Deconnexion":
                            socket.close();
                            Capteur toDel = keyList.get(infos[1]);
                            toDel.deconnexion();
                            Lock ld = new ReentrantLock();
                            ld.lock();
                            try{
                                InterfaceSwing.removeCapteurFromTable(toDel);
                                //InterfaceSwing.resetLieuxFiltres();
                            }finally{
                                ld.unlock();
                            }
                            return;
                        case "Connexion":
                            badFormat = 0;
                            Capteur toAdd;
                            try{
                                toAdd = new Capteur(infos[1], infos[2]);
                            }catch(BadFormatMessageException e){
                                e.printStackTrace();
                                System.out.println("message invalide a la connexion, fermeture de la socket.");
                                socket.close();
                                return;
                            }
                            Lock lc = new ReentrantLock();
                            lc.lock();       //lock mutex
                            try{
                                InterfaceSwing.addCapteur(toAdd);
                                //InterfaceSwing.resetLieuxFiltres();
                            }finally{
                                lc.unlock(); //unlock mutex
                            }
                            break;
                        case "Donnee":
                            badFormat = 0;
                            //TODO: check if it exceeds bounds
                            float newValue = Float.parseFloat(infos[2]);
                            Capteur toSet = keyList.get(infos[1]);
                            Lock lv = new ReentrantLock();
                            lv.lock();       //lock mutex
                            try{
                                toSet.update(newValue);
                                DatabaseManager.addValeur(newValue, keyList.get(infos[1]));
                                InterfaceSwing.capteurUpdate(toSet);
                            }finally{
                                lv.unlock(); //unlock mutex
                            }
                            break;
                        default:
                            badFormat++;
                            if (badFormat == 3){
                                socket.close();
                                return;
                            }
                    }
                }else{
                    socket.close();
                    return;
                }
            }
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void exit(){
        isRunning = false;
        for (Serveur serveur : serveurs){
            serveur.interrupt();
        }
    }
}
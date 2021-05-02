import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class EnviaLista extends Thread{
    MulticastSocket socket;
    BufferedReader br;
    String origen;
    public EnviaLista(MulticastSocket m, BufferedReader br){
        this.socket=m;
        this.br=br;
    }
    public void run(){
        try{
            String dir= "230.0.0.8";
            int pto=8000;
            InetAddress gpo = InetAddress.getByName(dir);

            for(;;){
                String mensaje= origen+": ";
                System.out.println("Escribe un mensaje para ser enviado:");
                mensaje += br.readLine();
                byte[] b = mensaje.getBytes();
                DatagramPacket p = new DatagramPacket(b,b.length,gpo,pto);
                socket.send(p);
            }//for
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class RecibeNombre extends Thread{
    MulticastSocket socket;
    ArrayList usuarios;
    public RecibeNombre(MulticastSocket m, ArrayList usuarios){
        this.socket=m;
        this.usuarios=usuarios;
    }
    public void run(){
        try{

            for(;;){
                DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                socket.receive(p);
                String msj = new String(p.getData(),0,p.getLength());

                if(msj.charAt(0)=='}') {
                    usuarios.add(msj.substring(1));
                    //System.out.print(usuarios);
                    for (int i =0; i<usuarios.size(); i++){
                        System.out.println(usuarios.get(i));
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

public class Server {
    public static void main(String[] args){
        ArrayList<String> usuarios = new ArrayList<>();

        try{
            int pto = 8000;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.ISO_8859_1));

            System.out.print("\nServer ejecutando");

            MulticastSocket m= new MulticastSocket(pto);
            m.setReuseAddress(true);
            m.setTimeToLive(255);
            String dir= "230.0.0.8";
            InetAddress gpo = InetAddress.getByName(dir);
            //InetAddress gpo = InetAddress.getByName("ff3e:40:2001::1");
            SocketAddress dirm;
            try{
                dirm = new InetSocketAddress(gpo,pto);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
            m.joinGroup(dirm, null);
            System.out.println("Socket unido al grupo "+dir);

            RecibeNombre r = new RecibeNombre(m, usuarios);
            EnviaLista e = new EnviaLista(m, br);
            e.setPriority(10);
            r.start();
            e.start();
            r.join();
            e.join();
        }catch(Exception e){}
    }
}

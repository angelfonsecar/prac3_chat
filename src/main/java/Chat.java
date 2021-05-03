import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

class Envia extends Thread{
    MulticastSocket socket;
    BufferedReader br;
    String origen;
    public Envia(MulticastSocket m, BufferedReader br, String origen){
        this.socket=m;
        this.br=br;
        this.origen=origen;
    }
    public void run(){
        try{
            String dir= "230.0.0.8";
            int pto=8000;
            InetAddress gpo = InetAddress.getByName(dir);

            for(;;){
                String mensaje= origen+": ";
                //System.out.print(">\n");
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

class Recibe extends Thread{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_PURPLE = "\u001B[35m";

    boolean listaRecibida = false;
    MulticastSocket socket;
    public Recibe(MulticastSocket m){
        this.socket=m;
    }
    public void run(){
        try{

            for(;;){
                DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                System.out.println("Listo para recibir mensajes...");
                socket.receive(p);
                String msj = new String(p.getData(),0,p.getLength());
                System.out.println("Mensaje recibido: "+msj);
            } //for
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


public class Chat {
    public static void main(String[] args){
        try{
            int pto= 8000;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.ISO_8859_1));

            System.out.print("\nNombre:");
            String nombre = null;
            try{
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                nombre= bufferRead.readLine();

            } catch(IOException e)
            {
                e.printStackTrace();
            }

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

            //con el caracter especial } se identifica que el mensaje es el nombre de un nuevo usuario
            String mensaje = "}"+nombre;

            byte[] b = mensaje.getBytes();
            DatagramPacket p = new DatagramPacket(b,b.length,gpo,pto);
            m.send(p);

            Recibe r = new Recibe(m);
            Envia e = new Envia(m, br, nombre);
            e.setPriority(10);
            r.start();
            e.start();
            r.join();
            e.join();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
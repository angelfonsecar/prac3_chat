import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class RecibeNombre extends Thread{
    MulticastSocket socket;
    ArrayList<String> usuarios;
    public RecibeNombre(MulticastSocket m, ArrayList<String> usuarios){
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
                    enviaListaUsuarios();   //enviar la lista actual
                    usuarios.add(msj.substring(1));
                    System.out.println("\nUsuarios conectados: " + usuarios.toString());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void enviaListaUsuarios(){
        try {
            String dir = "230.0.0.8";
            int pto = 8000;
            InetAddress gpo = InetAddress.getByName(dir);

            //con el caracter especial \ se identifica que el mensaje es la lista de usuarios
            String s = "\\" + usuarios.toString();
            byte[] b = s.getBytes();
            DatagramPacket p = new DatagramPacket(b,b.length,gpo,pto);
            if (!usuarios.isEmpty())
                socket.send(p);

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

            System.out.println("\nServer iniciando");

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
            System.out.println("Servidor corriendo en el grupo "+dir);

            RecibeNombre r = new RecibeNombre(m, usuarios);
            r.start();
            r.join();
        }catch(Exception e){ e.printStackTrace();}
    }
}

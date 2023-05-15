import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public class servidor {
    public static void main(String[] args) {
        // Crea e instala el gestor de seguridad.
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        
        try {
            // Crea una instancia de contador.
            //System.setProperty("java.rmi.server.hostname","192.168.1.107");
            Registry reg = LocateRegistry.createRegistry(port);

            Replica replicaInicial = new Replica(host, port, 1);
            Replica replica1 = new Replica(host, port, 2);
            Replica replica2 = new Replica(host, port, 3);

            // Colocamos las referencias de las réplicas en forma circular (como se indica en el diagrama).

            replicaInicial.replicaSiguiente = replica1;
            replicaInicial.replicaAnterior = replica2;
            replica1.replicaSiguiente = replica2;
            replica1.replicaAnterior = replicaInicial;
            replica2.replicaSiguiente = replicaInicial;
            replica2.replicaAnterior = replica1;

            reg.rebind("ReplicaInicial", replicaInicial);
            reg.rebind("ReplicaDerecha", replica1);
            reg.rebind("ReplicaIzquierda", replica2);

            System.out.println("Servidor RemoteException | MalformedURLExceptiondor preparado.");
        } catch (RemoteException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
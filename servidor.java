import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
//import contador.contador;

public class servidor {
    public static void main(String[] args) {
        // Crea e instala el gestor de seguridad.
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            // Crea una instancia de contador.
            //System.setProperty("java.rmi.server.hostname","192.168.1.107");
            Registry reg=LocateRegistry.createRegistry(1999);

            Replica replicaInicial = new Replica();
            Replica replica1 = new Replica();
            Replica replica2 = new Replica();

            // Colocamos las referencias de las réplicas en forma circular (como se indica en el diagrama).
            replicaInicial.replicaSiguiente = replica1;
            replicaInicial.replicaAnterior = replica2;
            replica1.replicaSiguiente = replica2;
            replica1.replicaAnterior = replicaInicial;
            replica2.replicaSiguiente = replicaInicial;
            replica2.replicaAnterior = replica1;

            Naming.rebind("ReplicaInicial", replicaInicial);
            Naming.rebind("ReplicaDerecha", replica1);
            Naming.rebind("ReplicaIzquierda", replica2);

            System.out.println("Servidor RemoteException | MalformedURLExceptiondor preparado.");
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
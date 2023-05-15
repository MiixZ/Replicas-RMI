import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Replica extends UnicastRemoteObject implements I_Replica {
    private float totalDonadoLocal = 0;
    Replica replicaSiguiente = null;
    Replica replicaAnterior = null;
    ArrayList<String> clientes = new ArrayList<String>();
    ArrayList<Boolean> clienteDona = new ArrayList<Boolean>();
    private Registry registroReplica;
    
    public Replica(String host, int port) throws RemoteException {
        this.registroReplica = LocateRegistry.getRegistry(host, port);    
    }

    // Métodos locales.
    public void addCliente(String cliente) {
        clientes.add(cliente);
        clienteDona.add(false);
    }

    public Boolean tengoAlCliente(String cliente) {
        return clientes.contains(cliente);
    }

    public int numClientes() {
        return clientes.size();
    }

    public int quienTieneMenosClientes() {
        int idReplica = 0;      // yo

        if(replicaSiguiente.numClientes() < numClientes()) {
            idReplica = 2;          // La réplica siguiente.
        } else if(replicaAnterior.numClientes() < numClientes()) {
            idReplica = 1;          // La réplica anterior.
        }

        return idReplica;
    }

    public float getTotalDonadoLocal() {
        return totalDonadoLocal;
    }

    // Métodos remotos de la interfaz.
    @Override
    public void RegistrarCliente(String cliente) throws RemoteException {
        System.out.println("Registrando cliente: " + cliente);

        // Como debe ser de forma transparente, no se indica que se va a hacer en otra réplica si no se puede registrar en esta.
        if(!tengoAlCliente(cliente) && !replicaAnterior.tengoAlCliente(cliente) && !replicaSiguiente.tengoAlCliente(cliente)) {
            int idReplica = quienTieneMenosClientes();      // Esta es la réplica que tiene menos clientes.

            switch(idReplica) {
                case 0:
                    addCliente(cliente);
                    break;
                case 1:
                    replicaAnterior.addCliente(cliente);
                    break;
                case 2:
                    replicaSiguiente.addCliente(cliente);
                    break;
            }
        } else {
            System.out.println("El cliente ya está registrado en alguna réplica.\n");
        }
    }

    @Override
    public int donar(String cliente, float cantidad) throws RemoteException {
        System.out.println("Cliente " + cliente + " intenta donar " + cantidad);

        // Si está registrado, puede donar, sino se comprueba que la réplica siguiente o anterior lo tenga para que done ahí.
        if(tengoAlCliente(cliente)) {
            totalDonadoLocal += cantidad;
            clienteDona.set(clientes.indexOf(cliente), true);
            System.out.println("El cliente ha donado " + cantidad + "€.\n");
            return 0;
        } else if(replicaAnterior.tengoAlCliente(cliente)) {
            replicaAnterior.donar(cliente, cantidad);
        } else if(replicaSiguiente.tengoAlCliente(cliente)) {
            replicaSiguiente.donar(cliente, cantidad);
        } else {
            System.out.println("El cliente no está registrado.\n");
            return -1;
        }

        return 0;
    }

    @Override
    public float totalDonado(String cliente) throws RemoteException {
        // Si está registrado y ha donado, puede ver el total de todas las réplicas, sino se comprueba que la réplica siguiente o anterior lo tenga para que lo vea desde ahí.
        if(tengoAlCliente(cliente)) {
            if(clienteDona.get(clientes.indexOf(cliente))) {
                float totalDonado = totalDonadoLocal + replicaAnterior.getTotalDonadoLocal() + replicaSiguiente.getTotalDonadoLocal();
                return totalDonado;
            } else {
                System.out.println("El cliente no ha donado nada.\n");
                return -1;
            }
        } else if(replicaAnterior.tengoAlCliente(cliente)) {
            return replicaAnterior.totalDonado(cliente);
        } else if(replicaSiguiente.tengoAlCliente(cliente)) {
            return replicaSiguiente.totalDonado(cliente);
        } else {
            System.out.println("El cliente no está registrado.\n");
            return -2;
        }
    }
}
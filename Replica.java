import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Replica extends UnicastRemoteObject implements I_Replica {
    private int ID;
    private float totalDonadoLocal = 0;

    Replica replicaSiguiente = null;
    Replica replicaAnterior = null;

    ArrayList<String> clientes = new ArrayList<String>();
    ArrayList<Boolean> clienteDona = new ArrayList<Boolean>();
    ArrayList<Map<String, Float>> donacionesCliente = new ArrayList<Map<String, Float>>();

    private Registry registroReplica;
    
    public Replica(String host, int port, int id) throws RemoteException {
        this.ID = id;
        this.registroReplica = LocateRegistry.getRegistry(host, port);    
    }

    // Métodos locales.
    public void addCliente(String cliente) {
        System.out.println("Réplica " + ID + ": Registrando cliente: " + cliente + "\n");
        clientes.add(cliente);
        clienteDona.add(false);

        // Crea una donación vacía para el cliente.
        Map<String, Float> donaciones = new HashMap<String, Float>();
        donaciones.put(cliente, (float) 0.0);
        donacionesCliente.add(donaciones);
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

    public void actualizarDonacionCliente(String cliente, float NuevaDonacion) {
        int indiceCliente = clientes.indexOf(cliente);
        Map<String, Float> donaciones = donacionesCliente.get(indiceCliente);

        // Sumo la nueva donación a la donación total del cliente.
        float donacionTotal = donaciones.get(cliente) + NuevaDonacion;
        donaciones.put(cliente, donacionTotal);
        donacionesCliente.set(indiceCliente, donaciones);
    }

    // Muestra el número de clientes de cada réplica por su ID.
    public void mostrarClientes() {
        System.out.println("Réplica " + ID + ": " + numClientes() + " clientes.");
        System.out.println("Réplica " + replicaSiguiente.ID + ": " + replicaSiguiente.numClientes() + " clientes.");
        System.out.println("Réplica " + replicaAnterior.ID + ": " + replicaAnterior.numClientes() + " clientes.");
    }

    // Métodos remotos de la interfaz.
    @Override
    public void RegistrarCliente(String cliente) throws RemoteException {
        System.out.println("Registrando cliente: " + cliente);
        mostrarClientes();

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

            actualizarDonacionCliente(cliente, cantidad);

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

    // Consulta la donación de un cliente en una réplica.
    @Override
    public String consultarDonacion(String cliente) throws RemoteException {
        if(tengoAlCliente(cliente)) {
            float donacion = donacionesCliente.get(clientes.indexOf(cliente)).get(cliente);
            return "El cliente " + cliente + " ha donado en total: " + donacion + "€.\n";
        } else if (replicaAnterior.tengoAlCliente(cliente)) {
            return replicaAnterior.consultarDonacion(cliente);
        } else if (replicaSiguiente.tengoAlCliente(cliente)) {
            return replicaSiguiente.consultarDonacion(cliente);
        } else {
            return "No estás registrado.\n";
        }
    }
}
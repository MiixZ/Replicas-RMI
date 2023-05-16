import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_Replica extends Remote {
    void RegistrarCliente(String cliente) throws RemoteException;

    int donar(String cliente, float cantidad) throws RemoteException;

    float totalDonado(String cliente) throws RemoteException;

    String consultarDonacion(String cliente) throws RemoteException;
}
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class cliente {
    public static void menu() {
        System.out.println("1. Registrarse.");
        System.out.println("2. Donar.");
        System.out.println("3. Total donado.");
        System.out.println("4. Salir (cambiar usuario).");
        System.out.println("5. Salir del programa.\n");
    }

    public static int elegirOpcion() {
        Scanner sc = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.print("Elige una opción válida: \n");
            menu();
            opcion = sc.nextInt();
        } while(opcion < 1 || opcion > 4);

        return opcion;
    }

    public static float elegirCantidad() {
        Scanner sc = new Scanner(System.in);
        float cantidad = 0;

        do {
            System.out.print("Introduce una cantidad válida: ");
            cantidad = sc.nextFloat();
        } while(cantidad < 0);

        return cantidad;
    }

    public static String elegirNombre() {
        Scanner sc = new Scanner(System.in);
        String nombre = "";

        do {
            System.out.print("Introduce un nombre válido: ");
            nombre = sc.nextLine();
        } while(nombre.length() < 1);

        return nombre;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println("Uso: cliente host");
            return;
        }

        // Crea e instala el gestor de seguridad
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {       // Siempre puerto 1999.
            String host = args[0];
            Registry reg = LocateRegistry.getRegistry(host, 1999);
            
            // Invocando el objeto remoto.
            I_Replica replica = (I_Replica) reg.lookup("ReplicaInicial");
            System.out.println("Objeto remoto encontrado.");

            String nombreActual = elegirNombre();
            System.out.println("Bienvenido, " + nombreActual + ".\n");
            int opcion = 0;

            do {
                opcion = elegirOpcion();
                System.out.println("-----------------------------------");

                switch(opcion) {
                    case 1:
                        replica.RegistrarCliente(nombreActual);
                        break;

                    case 2:
                        float cantidad = elegirCantidad();
                        if(cantidad > 0)
                            replica.donar(nombreActual, cantidad);
                        else
                            System.out.println("Debes donar una cantidad válida.\n");
                        break;

                    case 3:
                        float cantidadTotal = replica.totalDonado(nombreActual);
                        if(cantidadTotal >= 0)
                            System.out.println("Total donado: " + cantidadTotal + "€.");
                        else if(cantidadTotal == -1) 
                            System.out.println("No has donado nada, no puedes ver cuánto se ha donado.\n");
                            else if(cantidadTotal == -2)
                                System.out.println("Regístrate para poder donar y ver cuánto se ha donado.\n");
                        break;

                    case 4:
                        nombreActual = elegirNombre();
                        break;

                    case 5:
                        System.out.println("Saliendo del programa...");
                        break;

                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            } while (opcion != 5);

        } catch(NotBoundException | RemoteException e) {
            System.err.println("Exception del sistema: " + e);
        }
        System.exit(0);
    }
}

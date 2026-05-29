package pbo.f01;

import pbo.f01.model.ParkingArea;
import pbo.f01.model.Vehicle;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class App {
    
    static {
        System.setProperty("org.jboss.logging.provider", "jdk");
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.SEVERE);
    }

    private static EntityManagerFactory factory = Persistence.createEntityManagerFactory("pbo-f01-pu");

    public static void main(String[] args) {
        EntityManager manager = factory.createEntityManager();
        Scanner inputReader = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning && inputReader.hasNextLine()) {
            String rawInput = inputReader.nextLine().trim();
            if (rawInput.isEmpty()) continue;

            String[] data = rawInput.split("#");
            String commandType = data[0];

            switch (commandType) {
                case "area-add":
                    handleAddArea(manager, data);
                    break;
                case "vehicle-add":
                    handleAddVehicle(manager, data);
                    break;
                case "park":
                    handleParkVehicle(manager, data);
                    break;
                case "display-all":
                    handleDisplayData(manager);
                    isRunning = false; 
                    break;
                default:
                    break;
            }
        }

        inputReader.close();
        if (manager.isOpen()) manager.close();
        if (factory.isOpen()) factory.close();
    }

    // --- HELPER METHODS --- //

    private static void handleAddArea(EntityManager em, String[] params) {
        if (params.length != 4) return;
        
        em.getTransaction().begin();
        ParkingArea targetArea = em.find(ParkingArea.class, params[1]);
        
        if (targetArea == null) {
            int capacityLimit = Integer.parseInt(params[2]);
            ParkingArea newArea = new ParkingArea(params[1], capacityLimit, params[3]);
            em.persist(newArea);
        }
        em.getTransaction().commit();
    }

    private static void handleAddVehicle(EntityManager em, String[] params) {
        if (params.length != 4) return;

        em.getTransaction().begin();
        Vehicle targetVehicle = em.find(Vehicle.class, params[1]);
        
        if (targetVehicle == null) {
            Vehicle newVehicle = new Vehicle(params[1], params[2], params[3]);
            em.persist(newVehicle);
        }
        em.getTransaction().commit();
    }

    private static void handleParkVehicle(EntityManager em, String[] params) {
        if (params.length != 3) return;

        em.getTransaction().begin();
        Vehicle v = em.find(Vehicle.class, params[1]);
        ParkingArea p = em.find(ParkingArea.class, params[2]);

        if (v != null && p != null && p.isEligibleToPark(v)) {
            p.assignVehicle(v);
            em.merge(p);
        }
        em.getTransaction().commit();
    }

    private static void handleDisplayData(EntityManager em) {
        List<ParkingArea> allAreas = em.createQuery("SELECT pa FROM ParkingArea pa", ParkingArea.class).getResultList();
        
        Collections.sort(allAreas);

        for (ParkingArea pa : allAreas) {
            System.out.println(pa.toString());
            
            List<Vehicle> listKendaraan = pa.getVehicles();
            Collections.sort(listKendaraan);
            
            for (Vehicle k : listKendaraan) {
                System.out.println(k.toString());
            }
        }
    }
}
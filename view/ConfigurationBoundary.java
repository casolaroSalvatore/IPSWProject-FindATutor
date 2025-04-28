package logic.view;

import logic.model.dao.DaoFactory;
import logic.model.domain.InterfaceProvider;
import logic.model.domain.PersistenceProvider;

import java.util.Scanner;

public class ConfigurationBoundary {

    private static InterfaceProvider interfaceProvider;

    public static void configurePersistence() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nSelect persistence type:");
        System.out.println("1 - In-Memory (Demo)");
        System.out.println("2 - File System");
        System.out.println("3 - Database");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> DaoFactory.setPersistenceProvider(PersistenceProvider.IN_MEMORY);
            case 2 -> DaoFactory.setPersistenceProvider(PersistenceProvider.FILE_SYSTEM);
            case 3 -> DaoFactory.setPersistenceProvider(PersistenceProvider.DB);
            default -> {
                System.out.println("Invalid choice. Defaulting to In-Memory.");
                DaoFactory.setPersistenceProvider(PersistenceProvider.IN_MEMORY);
            }
        }
    }

    public static void configureInterface() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nSelect interface mode:");
        System.out.println("1 - Graphical User Interface (Colored)");
        System.out.println("2 - Command Line Interface (Black and White - BW)");
        System.out.print("Choice: ");

        int uiChoice = scanner.nextInt();
        switch (uiChoice) {
            case 1 -> interfaceProvider = InterfaceProvider.COLORED;
            case 2 -> interfaceProvider = InterfaceProvider.BW;
            default -> {
                System.out.println("Invalid choice. Defaulting to GUI Colored.");
                interfaceProvider = InterfaceProvider.COLORED;
            }
        }
    }

    public static void configureAll() {
        configurePersistence();
        configureInterface();
    }

    public static InterfaceProvider getInterfaceProvider() {
        return interfaceProvider;
    }
}

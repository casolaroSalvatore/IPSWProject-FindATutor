package Logic.View;

import Logic.Model.Dao.DaoFactory;
import Logic.Model.Domain.PersistenceProvider;

import java.util.Scanner;

import static Logic.Model.Dao.DaoFactory.setPersistenceProvider;
import static Logic.Model.Domain.PersistenceProvider.IN_MEMORY;

public class ConfigurationBoundary {

    // Configura il tipo di persistenza
    public static void configurePersistence() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Seleziona il tipo di persistenza:");
        System.out.println("1 - In-Memory (Demo)");
        System.out.println("2 - File System");
        System.out.println("3 - Database");
        System.out.print("Scelta: ");

        int scelta = scanner.nextInt();
        switch (scelta) {
            case 1 -> DaoFactory.setPersistenceProvider(IN_MEMORY);
            // case 2 -> DaoFactory.setPersistenceProvider(FILE);
            // case 3 -> DaoFactory.setPersistenceProvider(DB);
            default -> {
                System.out.println("Scelta non valida. Uso default: In-Memory.");
                DaoFactory.setPersistenceProvider(IN_MEMORY);
            }
        }
    }
}

package lk.ijse.fleet;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private FleetManager fleetManager;
    private UserManager userManager;
    private Scanner scanner;
    private SystemUser loggedInUser; // null until login succeeds

    public static void main(String[] args) {
        new Main().run();
    }

    public Main() {
        this.fleetManager = new FleetManager();
        this.userManager = new UserManager();
        this.scanner = new Scanner(System.in);

        // Lambda implementation of the custom functional interface
        fleetManager.setRentalEventListener(agreement ->
                System.out.println("[EVENT] New rental created: " + agreement.getAgreementId())
        );

        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();
    }

    public void run() {
        try {
            while (true) {
                if (loggedInUser == null) {
                    showAuthMenu();
                } else {
                    showMainMenu();
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Goodbye!");
        }
    }

    // ---------------------------------------------------------------- auth

    public void showAuthMenu() {
        while (loggedInUser == null) {
            String line = "=".repeat(56);
            System.out.println(line);
            System.out.println("FLEET RENTAL MANAGEMENT SYSTEM (FRMS)");
            System.out.println(line);
            System.out.println("[1] Login");
            System.out.println("[2] Register New User");
            System.out.println("[0] Exit");
            int choice = readInt("Enter choice > ");
            System.out.println(line);
            switch (choice) {
                case 1 -> {
                    String username = readText("Username: ");
                    String password = readText("Password: ");
                    SystemUser user = userManager.login(username, password);
                    if (user != null) {
                        loggedInUser = user;
                    } else {
                        System.out.println("Invalid username or password");
                    }
                }
                case 2 -> {
                    String username = readText("Choose username: ");
                    String password = readText("Choose password: ");
                    String fullName = readText("Full name: ");
                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        System.out.println("All fields are required.");
                    } else {
                        userManager.registerUser(username, password, fullName);
                    }
                }
                case 0 -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ---------------------------------------------------------------- main menu

    public void showMainMenu() {
        System.out.println("Welcome, " + loggedInUser.getFullName() + "! Loading fleet dashboard...");
        String line = "=".repeat(56);
        while (loggedInUser != null) {
            System.out.println(line);
            System.out.println("FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName());
            System.out.println(line);
            System.out.println("[1] Add Vehicle");
            System.out.println("[2] Add Customer");
            System.out.println("[3] View Available Vehicles");
            System.out.println("[4] Create Rental Agreement");
            System.out.println("[5] Return Vehicle");
            System.out.println("[6] View Revenue Report");
            System.out.println("[7] View Vehicles by Fuel Type");
            System.out.println("[8] View Combined Fleet & Customer Report");
            System.out.println("[9] View All Customers");
            System.out.println("[10] View All Rental Agreements");
            System.out.println("[0] Logout");
            int choice = readInt("Enter choice > ");
            System.out.println(line);
            switch (choice) {
                case 1 -> addVehicle();
                case 2 -> addCustomer();
                case 3 -> printList(fleetManager.getAvailableVehicles(), "No available vehicles.");
                case 4 -> createRental();
                case 5 -> fleetManager.returnVehicle(readText("Agreement ID: "));
                case 6 -> {
                    String dbl = "=".repeat(64);
                    System.out.println(dbl);
                    System.out.println("FLEET REVENUE REPORT");
                    System.out.println(dbl);
                    System.out.printf("Total Revenue: %,.2f%n", fleetManager.generateRevenueReport());
                    System.out.println("-".repeat(64));
                }
                case 7 -> {
                    FuelType type = readFuelType();
                    printList(fleetManager.getVehiclesByFuelType(type), "No vehicles with that fuel type.");
                }
                case 8 -> fleetManager.printAllReportables();
                case 9 -> printList(fleetManager.getAllCustomers(), "No customers registered.");
                case 10 -> {
                    List<RentalAgreement> all = fleetManager.getAllAgreements();
                    if (all.isEmpty()) {
                        System.out.println("No rental agreements.");
                    }
                    for (RentalAgreement a : all) {
                        System.out.println(a);
                    }
                }
                case 0 -> {
                    System.out.println("Logged out.");
                    loggedInUser = null;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ---------------------------------------------------------------- actions

    private void addVehicle() {
        System.out.println("Vehicle type: [1] Car  [2] Van  [3] Truck");
        int type = readInt("Type > ");
        if (type < 1 || type > 3) {
            System.out.println("Invalid vehicle type.");
            return;
        }
        String reg = readText("Registration number: ");
        String brand = readText("Brand: ");
        String model = readText("Model: ");
        double rate = readDouble("Daily rate: ");
        if (rate <= 0) {
            System.out.println("Daily rate must be positive.");
            return;
        }
        FuelType fuel = readFuelType();
        Vehicle v;
        switch (type) {
            case 1 -> v = new Car(reg, brand, model, rate, fuel);
            case 2 -> v = new Van(reg, brand, model, rate, fuel, readInt("Passenger capacity: "));
            default -> v = new Truck(reg, brand, model, rate, fuel, readDouble("Cargo capacity (tons): "));
        }
        fleetManager.addVehicle(v);
        System.out.println("Vehicle added: " + v.getVehicleId());
    }

    private void addCustomer() {
        System.out.println("Customer type: [1] Individual  [2] Corporate");
        int type = readInt("Type > ");
        if (type != 1 && type != 2) {
            System.out.println("Invalid customer type.");
            return;
        }
        String name = readText("Full name / company: ");
        String contact = readText("Contact number: ");
        Customer c;
        if (type == 1) {
            c = new IndividualCustomer(name, contact, readInt("Loyalty points: "));
        } else {
            c = new CorporateCustomer(name, contact, readDouble("Credit limit: "));
        }
        fleetManager.addCustomer(c);
        System.out.println("Customer added: " + c.getCustomerId());
    }

    private void createRental() {
        String vehicleId = readText("Vehicle ID: ");
        String customerId = readText("Customer ID: ");
        int days = readInt("Rental days: ");
        if (days <= 0) {
            System.out.println("Rental days must be at least 1.");
            return;
        }
        try {
            RentalAgreement agreement = fleetManager.createRentalAgreement(vehicleId, customerId, days);
            if (agreement != null) {
                System.out.println(agreement);
            }
        } catch (VehicleNotAvailableException | InsufficientCreditException e) {
            System.out.println("Rental failed: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- helpers

    private <T> void printList(List<T> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (T item : items) {
            System.out.println(item);
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private FuelType readFuelType() {
        while (true) {
            String input = readText("Fuel type (PETROL/DIESEL/ELECTRIC/HYBRID): ").toUpperCase();
            try {
                return FuelType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown fuel type. Try again.");
            }
        }
    }
}

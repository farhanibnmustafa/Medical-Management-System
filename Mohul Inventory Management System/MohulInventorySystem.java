import java.io.*;
import java.util.*;

// Main Class
public class MohulInventorySystem {
    public static void main(String[] args) {
        System.out.println("\n\t\t\tMOHUL\n\tInventory Management System\n");
        InventorySystem system = new InventorySystem();
        system.start();
    }
}

// Core Class for Managing the System
class InventorySystem {
    private final Scanner scanner = new Scanner(System.in);
    private final UserManager userManager = new UserManager();
    private final Inventory inventory = new Inventory();

    public InventorySystem() {
        userManager.loadUsers();
        inventory.loadInventory();
    }

    public void start() {
        while (true) {
            System.out.println("1. Log In\n2. Sign Up\n3. Exit");
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> login();
                case 2 -> signUp();
                case 3 -> {
                    userManager.saveUsers();
                    inventory.saveInventory();
                    System.out.println("Exiting... Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void login() {
        System.out.println("\n--- Log In ---");

        System.out.print("Enter ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        User user = userManager.authenticate(id, password);
        if (user != null) {
            System.out.println("\nWelcome, " + user.getName() + "!");
            user.showMenu(scanner, inventory, userManager);
        } else {
            System.out.println("Invalid ID or Password.");
        }
    }

    private void signUp() {
        System.out.println("\n--- Sign Up ---");

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter ID: ");
        String id = scanner.nextLine().trim();

        String designation = "";
        while (true) {
            System.out.println("Enter Designation (Choose one): ");
            System.out.println("1. Owner\n2. Manager\n3. Staff");
            int designationChoice = getIntInput("Enter your choice: ");
            if (designationChoice == 1) {
                designation = "Owner";
                break;
            } else if (designationChoice == 2) {
                designation = "Manager";
                break;
            } else if (designationChoice == 3) {
                designation = "Staff";
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        if (userManager.isIdTaken(id)) {
            System.out.println("Error: ID already taken. Please try again with a different ID.");
        } else {
            User user;
            switch (designation) {
                case "Owner" -> user = new Owner(name, id, password);
                case "Manager" -> user = new Manager(name, id, password);
                case "Staff" -> user = new Staff(name, id, password);
                default -> throw new IllegalStateException("Unexpected value: " + designation);
            }
            userManager.registerUser(user);
            System.out.println("User Registered Successfully!");
        }
    }

    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Try again: ");
            scanner.next();
        }
        int input = scanner.nextInt();
        scanner.nextLine();
        return input;
    }
}

// User Management Class
class UserManager {
    private final Map<String, User> users = new HashMap<>();
    private static final String USER_FILE = "users.txt";

    public User authenticate(String id, String password) {
        User user = users.get(id);
        return (user != null && user.getPassword().equals(password)) ? user : null;
    }

    public void registerUser(User user) {
        users.put(user.getId(), user);
    }

    public boolean isIdTaken(String id) {
        return users.containsKey(id);
    }

    public void displayAllEmployees() {
        System.out.println("\n--- Employee Information ---");
        users.values().forEach(user -> {
            System.out.println("Name: " + user.getName() + ", ID: " + user.getId() + ", Designation: " + user.getDesignation());
        });
    }

    public void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name = parts[0];
                    String id = parts[1];
                    String designation = parts[2];
                    String password = parts[3];
                    User user;
                    switch (designation) {
                        case "Owner" -> user = new Owner(name, id, password);
                        case "Manager" -> user = new Manager(name, id, password);
                        case "Staff" -> user = new Staff(name, id, password);
                        default -> throw new IllegalArgumentException("Invalid designation in file: " + designation);
                    }
                    users.put(id, user);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : users.values()) {
                writer.write(user.getName() + "," + user.getId() + "," + user.getDesignation() + "," + user.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}

// Abstract User Class for Polymorphism
abstract class User {
    private final String name;
    private final String id;
    private final String designation;
    private final String password;

    public User(String name, String id, String designation, String password) {
        this.name = name;
        this.id = id;
        this.designation = designation;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public String getPassword() {
        return password;
    }

    public abstract void showMenu(Scanner scanner, Inventory inventory, UserManager userManager);
}

// Specialized User Classes
class Owner extends User {
    public Owner(String name, String id, String password) {
        super(name, id, "Owner", password);
    }

    @Override
    public void showMenu(Scanner scanner, Inventory inventory, UserManager userManager) {
        while (true) {
            System.out.println("--- Owner Menu ---");
            System.out.println("1. View Stock\n2. Add Production\n3. Sell Product\n4. Return Product\n5. View Employee Info\n6. Log Out");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> inventory.viewStock();
                case 2 -> inventory.addProductionMenu(scanner);
                case 3 -> inventory.sellProduct(scanner);
                case 4 -> inventory.returnProduct(scanner);
                case 5 -> userManager.displayAllEmployees();
                case 6 -> {
                    System.out.println("Logging Out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

class Manager extends User {
    public Manager(String name, String id, String password) {
        super(name, id, "Manager", password);
    }

    @Override
    public void showMenu(Scanner scanner, Inventory inventory, UserManager userManager) {
        while (true) {
            System.out.println("--- Manager Menu ---");
            System.out.println("1. View Stock\n2. Add Production\n3. Sell Product\n4. Return Product\n5. View Employee Info\n6. Log Out");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> inventory.viewStock();
                case 2 -> inventory.addProductionMenu(scanner);
                case 3 -> inventory.sellProduct(scanner);
                case 4 -> inventory.returnProduct(scanner);
                case 5 -> userManager.displayAllEmployees();
                case 6 -> {
                    System.out.println("Logging Out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

class Staff extends User {
    public Staff(String name, String id, String password) {
        super(name, id, "Staff", password);
    }

    @Override
    public void showMenu(Scanner scanner, Inventory inventory, UserManager userManager) {
        while (true) {
            System.out.println("--- Staff Menu ---");
            System.out.println("1. View Stock\n2. Add Production\n3. Sell Product\n4. Return Product\n5. Log Out");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> inventory.viewStock();
                case 2 -> inventory.addProductionMenu(scanner);
                case 3 -> inventory.sellProduct(scanner);
                case 4 -> inventory.returnProduct(scanner);
                case 5 -> {
                    System.out.println("Logging Out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

// Inventory Management Class
class Inventory {
    private final Map<String, Product> products = new HashMap<>();
    private static final String INVENTORY_FILE = "inventory.txt";

    public Inventory() {
        products.put("Hair Oil", new Product("Hair Oil"));
        products.put("Hair Pack", new Product("Hair Pack"));
        products.put("Hair Spray", new Product("Hair Spray"));
    }

    public void addProductionMenu(Scanner scanner) {
        System.out.println("\n--- Add Production ---");
        System.out.println("Available Products:");
        List<String> productNames = new ArrayList<>(products.keySet());
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println((i + 1) + ". " + productNames.get(i));
        }
        System.out.print("Choose a product: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= productNames.size()) {
            String productName = productNames.get(choice - 1);
            System.out.print("Enter Quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter Batch Number: ");
            String batchNumber = scanner.nextLine();
            System.out.print("Enter Date: ");
            String date = scanner.nextLine();
            addProduction(productName, quantity, batchNumber, date);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    public void addProduction(String productName, int quantity, String batchNumber, String date) {
        Product product = products.get(productName);
        if (product != null) {
            product.addStock(quantity);
            System.out.println(quantity + " units of " + productName + " added. Batch: " + batchNumber + ", Date: " + date);
        } else {
            System.out.println("Invalid product name.");
        }
    }

    public void viewStock() {
        System.out.println("\n--- Current Stock ---");
        products.forEach((name, product) -> {
            System.out.println(name + ": " + product.getQuantity() + " units");
            if (product.getQuantity() < 20) {
                System.out.println("\tWarning: Running Low!");
            }
        });
    }

    public void sellProduct(Scanner scanner) {
        System.out.println("\n--- Sell Product ---");
        System.out.println("Available Products:");
        List<String> productNames = new ArrayList<>(products.keySet());
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println((i + 1) + ". " + productNames.get(i));
        }
        System.out.print("Choose a product: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= productNames.size()) {
            String productName = productNames.get(choice - 1);
            System.out.print("Enter Quantity to Sell: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();
            Product product = products.get(productName);
            if (product != null && product.getQuantity() >= quantity) {
                product.reduceStock(quantity);
                System.out.println(quantity + " units of " + productName + " sold.");
            } else {
                System.out.println("Insufficient stock or invalid product.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    public void returnProduct(Scanner scanner) {
        System.out.println("\n--- Return Product ---");
        System.out.println("Available Products:");
        List<String> productNames = new ArrayList<>(products.keySet());
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println((i + 1) + ". " + productNames.get(i));
        }
        System.out.print("Choose a product: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= productNames.size()) {
            String productName = productNames.get(choice - 1);
            System.out.print("Enter Quantity to Return: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();
            Product product = products.get(productName);
            if (product != null) {
                product.addStock(quantity);
                System.out.println(quantity + " units of " + productName + " returned.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    public void loadInventory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String productName = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    Product product = products.get(productName);
                    if (product != null) {
                        product.addStock(quantity);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }
    public void saveInventory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INVENTORY_FILE))) {
            for (Map.Entry<String, Product> entry : products.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue().getQuantity());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
        }
    }
}
class Product {
    private final String name;
    private int quantity;

    public Product(String name) {
        this.name = name;
        this.quantity = 0;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addStock(int quantity) {
        this.quantity += quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity <= this.quantity) {
            this.quantity -= quantity;
        } else {
            System.out.println("Not enough product.");
        }
    }
}
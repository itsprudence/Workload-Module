import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Farmer class (Common across all modules)
class Farmer {
    private String name;
    private String id;
    private String geolocation;
    private String phone;

    public Farmer(String name, String id, String geolocation,String phone) {
        this.name = name;
        this.id = id;
        this.geolocation = geolocation;
        this.phone  = phone;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
    public String getPhone(){
        return phone ;
    }
}

// Collector class (Handles work allocation)
class Collector {
    private String name;
    private List<Farmer> assignedFarmers;
    private String collectionPoint;

    public Collector(String name, String collectionPoint) {
        this.name = name;
        this.collectionPoint = collectionPoint;
        this.assignedFarmers = new ArrayList<>();
    }

    public void assignFarmer(Farmer farmer) {
        assignedFarmers.add(farmer);
    }

    public void displayAssignments() {
        System.out.println("Collector: " + name + " (Collection Point: " + collectionPoint + ")");
        for (Farmer farmer : assignedFarmers) {
            System.out.println("  - Assigned Farmer: " + farmer.getName() + " (ID: " + farmer.getId() + ")");
        }
    }
}

// FarmerGroup class (Handles group coordination)
class FarmerGroup {
    private String groupName;
    private List<Farmer> farmers;
    private double milkQuantity;

    public FarmerGroup(String groupName, double milkQuantity) {
        this.groupName = groupName;
        this.milkQuantity = milkQuantity;
        this.farmers = new ArrayList<>();
    }

    public void addFarmer(Farmer farmer) {
        farmers.add(farmer);
    }

    public void displayGroupInfo() {
        System.out.println("Group: " + groupName + " | Expected Milk: " + milkQuantity + " Liters");
        for (Farmer farmer : farmers) {
            System.out.println("  - Member: " + farmer.getName() + " (ID: " + farmer.getId() + ")");
        }
    }
}

// FarmerYield class (Handles yield management)
class FarmerYield {
    private String farmerId;
    private Map<String, Double> yieldHistory;

    public FarmerYield(String farmerId) {
        this.farmerId = farmerId;
        this.yieldHistory = new HashMap<>();
    }

    public void recordYield(String date, double quantity) {
        yieldHistory.put(date, quantity);
    }

    public void displayYield() {
        System.out.println("Yield History for Farmer ID: " + farmerId);
        for (Map.Entry<String, Double> entry : yieldHistory.entrySet()) {
            System.out.println("  - Date: " + entry.getKey() + " | Yield: " + entry.getValue() + "L");
        }
    }

    // Firebase Integration to store yield data
    public void storeYieldInFirebase(String jsonData) {
        try {
            String firebaseUrl = "https://workload-module.firebaseio.com/yield.json"; // Firebase URL
            URL url = new URL("https://workload-module.firebaseio.com/yield.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] postDataBytes = jsonData.getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(postDataBytes);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Data added to Firebase Firestore!");
            } else {
                System.out.println("Failed to add data, Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Notification class (Handles notifications via Twilio)
class Notification {
    // Twilio SMS integration
    public void sendNotification(String message, String phoneNumber) {
        try {
            String accountSid ="AC969a9746a4185133357876ad67f4cfc7";  // Replace with your Twilio Account SID
            String authToken = "2c05d919af99a8b5bbb725e8c9e5ac14";    // Replace with your Twilio Auth Token
            String fromPhone = "+12723813350"; // Your Twilio phone number

            // Twilio API URL
            String urlString = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + getEncodedCredentials(accountSid, authToken));

            String postData = "To=" + phoneNumber + "&From=" + fromPhone + "&Body=" + message;

            byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(postDataBytes);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("SMS sent successfully!");
            } else {
                System.out.println("Failed to send SMS, Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getEncodedCredentials(String accountSid, String authToken) {
        String credentials = accountSid + ":" + authToken;
        return new String(java.util.Base64.getEncoder().encode(credentials.getBytes()));
    }
}

// Main class (Integrates all modules)
public class MilkProcessingSystem {
    public static void main(String[] args) {
        System.out.println("=== MILK PROCESSING SYSTEM ===\n");

        // Work Allocation
        System.out.println("** Work Allocation **");
        Farmer farmer1 = new Farmer("John", "F001", "Kenya, Nairobi","+254780265847");
        Farmer farmer2 = new Farmer("Alice", "F002", "Kenya, Kisumu","+254717116455");

        Collector collector1 = new Collector("James", "Point A");
        collector1.assignFarmer(farmer1);
        collector1.assignFarmer(farmer2);
        collector1.displayAssignments();
        System.out.println();

        // Group Coordination
        System.out.println("** Group Coordination **");
        FarmerGroup group1 = new FarmerGroup("Group A", 500);
        group1.addFarmer(farmer1);
        group1.addFarmer(farmer2);
        group1.displayGroupInfo();

        // Yield Management
        System.out.println("** Yield Management **");
        FarmerYield farmerYield = new FarmerYield("F001");
        farmerYield.recordYield("2025-04-01", 100);
        farmerYield.recordYield("2025-04-02", 120);
        farmerYield.displayYield();

        // Store in Firebase
        String yieldData = "{ \"farmerId\": \"F001\", \"yield\": {\"2025-04-01\": 100, \"2025-04-02\": 120}}";
        farmerYield.storeYieldInFirebase(yieldData);

        Notification notify = new Notification();
        String message = "Next collection: April 5, 2025.";

        // Sending notification to farmer1 and farmer2
        notify.sendNotification(message, farmer1.getPhone());
        notify.sendNotification(message, farmer2.getPhone());
        System.out.println();

        System.out.println("=== END OF SYSTEM ===");
    }
}

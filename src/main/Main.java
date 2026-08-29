package main;

import adt.BinaryMaxHeap;
import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.LinkedQueue;
import adt.LinkedStack;
import boundary.MainMenuUI;
import control.FrontDeskController;
import control.LoyaltyController;
import control.StandardBookingController;
import control.VIPAllocationController;
import control.VIPAllocationController.VIPReservation;
import entity.BillingRecord;
import entity.CustomerReferral;
import entity.FrontDeskLog;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Partner;
import entity.RedemptionTransaction;
import entity.Reservation;
import entity.Room;

/**
 * Main entry point for the TARUMT Resorts System.
 *
 * 1. Creates the shared ADT data structures (Master Registry, queues, tree,
 * partner lists).
 * 2. Injects them into the Controllers (Control layer).
 * 3. Passes the Controllers into the MainMenuUI (Boundary layer).
 * 4. Starts the application loop.
 */
public class Main {

    public static void main(String[] args) {

        // ============================
        // 1. Create shared ADT instances
        // ============================
        DoublyLinkedList<Guest> masterRegistry = new DoublyLinkedList<>();
        LinkedQueue<Reservation> standardQueue = new LinkedQueue<>();
        BinaryMaxHeap<VIPReservation> vipQueue = new BinaryMaxHeap<>();
        BinarySearchTree<Reservation> searchTree = new BinarySearchTree<>();
        DoublyLinkedList<Room> roomInventory = new DoublyLinkedList<>();
        DoublyLinkedList<LoyaltyAccount> loyaltyAccounts = new DoublyLinkedList<>();
        DoublyLinkedList<RedemptionTransaction> redemptionLog = new DoublyLinkedList<>();
        DoublyLinkedList<Partner> partnerRegistry = new DoublyLinkedList<>();
        DoublyLinkedList<CustomerReferral> referralLog = new DoublyLinkedList<>();
        DoublyLinkedList<FrontDeskLog> checkInLog = new DoublyLinkedList<>();
        DoublyLinkedList<FrontDeskLog> checkOutLog = new DoublyLinkedList<>();
        DoublyLinkedList<FrontDeskLog> cancellationLog = new DoublyLinkedList<>();
        DoublyLinkedList<BillingRecord> billingLog = new DoublyLinkedList<>();

        // ============================
        // Check & Load Persisted Text Files into ADTs
        // ============================
        if (utility.FilePersistenceUtils.dataFilesExist()) {
            utility.FilePersistenceUtils.loadAllData(
                    masterRegistry, roomInventory, standardQueue, vipQueue, searchTree,
                    loyaltyAccounts, redemptionLog, partnerRegistry, referralLog,
                    checkInLog, checkOutLog, cancellationLog, billingLog);
        } else {
            // Seed Room Inventory (20 rooms)
            roomInventory.add(new Room("101", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("102", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("103", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("104", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("105", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("106", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("107", "STANDARD", 100.0, "AVAILABLE"));
            roomInventory.add(new Room("108", "STANDARD", 100.0, "AVAILABLE"));

            roomInventory.add(new Room("201", "DELUXE", 200.0, "AVAILABLE"));
            roomInventory.add(new Room("202", "DELUXE", 200.0, "AVAILABLE"));
            roomInventory.add(new Room("203", "DELUXE", 200.0, "AVAILABLE"));
            roomInventory.add(new Room("204", "DELUXE", 200.0, "AVAILABLE"));
            roomInventory.add(new Room("205", "DELUXE", 200.0, "AVAILABLE"));
            roomInventory.add(new Room("206", "DELUXE", 200.0, "AVAILABLE"));

            roomInventory.add(new Room("301", "SUITE", 500.0, "AVAILABLE"));
            roomInventory.add(new Room("302", "SUITE", 500.0, "AVAILABLE"));
            roomInventory.add(new Room("303", "SUITE", 500.0, "AVAILABLE"));
            roomInventory.add(new Room("304", "SUITE", 500.0, "AVAILABLE"));
            roomInventory.add(new Room("305", "SUITE", 500.0, "AVAILABLE"));
            roomInventory.add(new Room("306", "SUITE", 500.0, "MAINTENANCE"));

            // Seed initial guests and loyalty profiles
            Guest guest1 = new Guest("Alice Tan", "980101-14-1234", "012-3456789", "alice@test.com", "DIAMOND");
            Guest guest2 = new Guest("Bob Lim", "950202-10-5678", "019-8765432", "bob@test.com", "GOLD");
            Guest guest3 = new Guest("Charlie Brown", "900303-08-9012", "011-11112222", "charlie@brown.com",
                    "STANDARD");
            masterRegistry.add(guest1);
            masterRegistry.add(guest2);
            masterRegistry.add(guest3);

            LoyaltyAccount acc1 = new LoyaltyAccount(guest1.getGuestId());
            acc1.setTotalPoints(12000);
            acc1.setTierStatus("DIAMOND");
            acc1.addHistoryEntry("Initial Seed Points");
            loyaltyAccounts.add(acc1);

            LoyaltyAccount acc2 = new LoyaltyAccount(guest2.getGuestId());
            acc2.setTotalPoints(3500);
            acc2.setTierStatus("GOLD");
            acc2.addHistoryEntry("Initial Seed Points");
            loyaltyAccounts.add(acc2);

            // Seed Strategic Partners
            Partner p1 = new Partner("Sime Darby Property", "PROPERTY_DEVELOPER", "Dato' Ahmad", "03-78448000",
                    "info@simedarbyproperty.com", "Luxury Township & Residential Purchasing");
            Partner p2 = new Partner("SP Setia", "PROPERTY_DEVELOPER", "Tan Sri Liew", "03-33448888",
                    "contact@spsetia.com", "Eco-Sanctuary Property & Modern Living Packages");
            Partner p3 = new Partner("Sunway Property", "PROPERTY_DEVELOPER", "Jeffrey Cheah", "03-56398888",
                    "enquiry@sunwayproperty.com", "Integrated Resort & City Living Homes");
            Partner p4 = new Partner("Apex Renovation Contractors", "RENOVATION_CONTRACTOR", "Eng Seng", "012-8889900",
                    "sales@apexrenovation.com", "Full House Renovation, Extensions & Wet Works");
            Partner p5 = new Partner("VoltCraft Electrical Contractors", "ELECTRICAL_CONTRACTOR", "David Wong",
                    "016-7776655", "support@voltcraft.my", "Smart Home Rewiring, DB Box & Solar Fittings");
            Partner p6 = new Partner("Elegance Interior Design", "INTERIOR_DESIGN_FIRM", "Sophia Chen", "019-3332211",
                    "creative@eleganceid.com", "Bespoke Interior Design & Custom Furniture Upgrades");

            partnerRegistry.add(p1);
            partnerRegistry.add(p2);
            partnerRegistry.add(p3);
            partnerRegistry.add(p4);
            partnerRegistry.add(p5);
            partnerRegistry.add(p6);

            // Save seeded data to text files
            utility.FilePersistenceUtils.saveAllData(
                    masterRegistry, roomInventory, standardQueue, vipQueue,
                    searchTree,
                    loyaltyAccounts, redemptionLog, partnerRegistry, referralLog,
                    checkInLog, checkOutLog, cancellationLog, billingLog);
        }

        // ============================
        // 2. Create Controllers (Control layer)
        // — inject the shared data structures
        // ============================
        LinkedStack<entity.UndoAction> undoStack = new LinkedStack<>();
        control.UndoController undoController = new control.UndoController(undoStack);

        LoyaltyController mod4 = new LoyaltyController(masterRegistry, loyaltyAccounts, redemptionLog);
        mod4.setReservationResources(roomInventory, searchTree);
        FrontDeskController mod3 = new FrontDeskController(
                searchTree, masterRegistry, roomInventory, mod4,
                checkInLog, checkOutLog, cancellationLog, billingLog);
        mod3.backfillStoredRecordsIfEmpty();
        StandardBookingController mod1 = new StandardBookingController(standardQueue, masterRegistry, roomInventory,
                searchTree);
        VIPAllocationController mod2 = new VIPAllocationController(vipQueue, masterRegistry, loyaltyAccounts,
                roomInventory, searchTree);

        mod1.setUndoController(undoController);
        mod1.setLoyaltyController(mod4);
        mod1.setVipQueue(vipQueue);
        mod2.setUndoController(undoController);
        mod3.setUndoController(undoController);
        mod4.setUndoController(undoController);

        // Register auto-save shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            utility.FilePersistenceUtils.saveAllData(
                    masterRegistry, roomInventory, standardQueue, vipQueue,
                    searchTree,
                    loyaltyAccounts, redemptionLog, partnerRegistry, referralLog,
                    checkInLog, checkOutLog, cancellationLog, billingLog);
        }));

        // ============================
        // 3. Create Boundary (UI layer)
        // — inject the controllers
        // ============================
        MainMenuUI mainMenu = new MainMenuUI(mod1, mod2, mod3, mod4);

        // ============================
        // 4. Start the application
        // ============================
        mainMenu.start();

        // Save data on exiting main menu
        utility.FilePersistenceUtils.saveAllData(
                masterRegistry, roomInventory, standardQueue, vipQueue,
                searchTree,
                loyaltyAccounts, redemptionLog, partnerRegistry, referralLog,
                checkInLog, checkOutLog, cancellationLog, billingLog);
    }
}

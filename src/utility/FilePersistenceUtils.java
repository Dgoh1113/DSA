package utility;

import adt.BinaryMaxHeap;
import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.LinkedQueue;
import control.VIPAllocationController.VIPReservation;
import entity.CustomerReferral;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Partner;
import entity.RedemptionTransaction;
import entity.Reservation;
import entity.Room;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Utility: FilePersistenceUtils — Text File Persistence Manager for ADTs.
 * Saves and loads all module data to/from text files (.txt) inside the ./data/ directory.
 *
 * Data files saved:
 * 1. guests.txt             [ DoublyLinkedList<Guest> ]
 * 2. rooms.txt              [ DoublyLinkedList<Room> ]
 * 3. standard_queue.txt     [ LinkedQueue<Reservation> ]
 * 4. vip_queue.txt          [ BinaryMaxHeap<VIPReservation> ]
 * 5. loyalty_accounts.txt   [ DoublyLinkedList<LoyaltyAccount> ]
 * 6. redemptions.txt        [ DoublyLinkedList<RedemptionTransaction> ]
 * 7. partners.txt           [ DoublyLinkedList<Partner> ]
 * 8. referrals.txt          [ DoublyLinkedList<CustomerReferral> ]
 */
public class FilePersistenceUtils {

    private static final String DATA_DIR = "data";

    private static void ensureDataDirExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Checks if persisted text files already exist in ./data/ directory.
     */
    public static boolean dataFilesExist() {
        File guestFile = new File(DATA_DIR + "/guests.txt");
        return guestFile.exists() && guestFile.length() > 0;
    }

    // =========================================================================
    // SAVE ALL ADTs TO TEXT FILES
    // =========================================================================

    public static void saveAllData(DoublyLinkedList<Guest> guestRegistry,
                                   DoublyLinkedList<Room> roomInventory,
                                   LinkedQueue<Reservation> standardQueue,
                                   BinaryMaxHeap<VIPReservation> vipQueue,
                                   BinarySearchTree<Reservation> searchTree,
                                   DoublyLinkedList<LoyaltyAccount> loyaltyAccounts,
                                   DoublyLinkedList<RedemptionTransaction> redemptionLog,
                                   DoublyLinkedList<Partner> partnerRegistry,
                                   DoublyLinkedList<CustomerReferral> referralLog) {
        ensureDataDirExists();
        saveGuests(guestRegistry);
        saveRooms(roomInventory);
        saveStandardQueue(standardQueue);
        saveVIPQueue(vipQueue);
        saveReservations(searchTree);
        saveLoyaltyAccounts(loyaltyAccounts);
        saveRedemptions(redemptionLog);
        savePartners(partnerRegistry);
        saveReferrals(referralLog);
    }

    private static void saveGuests(DoublyLinkedList<Guest> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/guests.txt"))) {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Guest g = list.getEntry(i);
                if (g != null) {
                    writer.println(g.getGuestId() + "|" + g.getName() + "|" + g.getIcPassport() + "|"
                            + g.getContactNo() + "|" + g.getEmail() + "|" + g.getLoyaltyTier());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving guests.txt: " + e.getMessage());
        }
    }

    private static void saveRooms(DoublyLinkedList<Room> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/rooms.txt"))) {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Room r = list.getEntry(i);
                if (r != null) {
                    writer.println(r.getRoomNo() + "|" + r.getRoomType() + "|" + r.getNightlyRate() + "|" + r.getStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving rooms.txt: " + e.getMessage());
        }
    }

    private static void saveStandardQueue(LinkedQueue<Reservation> queue) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/standard_queue.txt"))) {
            DoublyLinkedList<Reservation> list = queue.toList();
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Reservation r = list.getEntry(i);
                if (r != null) {
                    writer.println(serializeReservation(r));
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving standard_queue.txt: " + e.getMessage());
        }
    }

    private static void saveVIPQueue(BinaryMaxHeap<VIPReservation> queue) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/vip_queue.txt"))) {
            DoublyLinkedList<VIPReservation> list = queue.toList();
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                VIPReservation vip = list.getEntry(i);
                if (vip != null && vip.getReservation() != null) {
                    writer.println(serializeReservation(vip.getReservation()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving vip_queue.txt: " + e.getMessage());
        }
    }

    private static void saveReservations(BinarySearchTree<Reservation> searchTree) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/reservations.txt"))) {
            DoublyLinkedList<Reservation> list = searchTree.inOrderTraversal();
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Reservation reservation = list.getEntry(i);
                if (reservation != null) writer.println(serializeReservation(reservation));
            }
        } catch (Exception e) {
            System.err.println("Error saving reservations.txt: " + e.getMessage());
        }
    }

    private static void saveLoyaltyAccounts(DoublyLinkedList<LoyaltyAccount> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/loyalty_accounts.txt"))) {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                LoyaltyAccount a = list.getEntry(i);
                if (a != null) {
                    StringBuilder history = new StringBuilder();
                    DoublyLinkedList<String> histList = a.getPointHistoryList();
                    if (histList != null) {
                        for (int j = 1; j <= histList.getNumberOfEntries(); j++) {
                            if (j > 1) history.append(";;;");
                            history.append(histList.getEntry(j));
                        }
                    }
                    writer.println(a.getMemberId() + "|" + a.getTotalPoints() + "|" + a.getTierStatus() + "|"
                            + (a.getPointsExpiryDate() == null ? "" : a.getPointsExpiryDate()) + "|" + history.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving loyalty_accounts.txt: " + e.getMessage());
        }
    }

    private static void saveRedemptions(DoublyLinkedList<RedemptionTransaction> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/redemptions.txt"))) {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                RedemptionTransaction t = list.getEntry(i);
                if (t != null) {
                    writer.println(t.getTransactionId() + "|" + t.getMemberId() + "|" + t.getRewardItem() + "|"
                            + t.getPointsDeducted() + "|" + t.getRequestDate() + "|" + t.getStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving redemptions.txt: " + e.getMessage());
        }
    }

    private static void savePartners(DoublyLinkedList<Partner> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/partners.txt"))) {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                Partner p = list.getEntry(i);
                if (p != null) {
                    writer.println(p.getPartnerId() + "|" + p.getCompanyName() + "|" + p.getPartnerCategory() + "|"
                            + p.getContactPerson() + "|" + p.getContactPhone() + "|" + p.getEmail() + "|"
                            + p.getOfferedServices() + "|" + p.getTotalReferralsCount() + "|" + p.getTotalRevenueGenerated());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving partners.txt: " + e.getMessage());
        }
    }

    private static void saveReferrals(DoublyLinkedList<CustomerReferral> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/referrals.txt"))) {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                CustomerReferral r = list.getEntry(i);
                if (r != null) {
                    writer.println(r.getReferralId() + "|" + r.getPartnerId() + "|" + (r.getGuestId() == null ? "" : r.getGuestId()) + "|"
                            + r.getCustomerName() + "|" + r.getCustomerStage() + "|" + r.getProductIntroduced() + "|"
                            + r.getDealAmount() + "|" + r.getReferralDate() + "|" + r.getStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving referrals.txt: " + e.getMessage());
        }
    }

    private static String serializeReservation(Reservation r) {
        return r.getConfirmationNo() + "|" + r.getGuestId() + "|" + r.getRoomType() + "|"
                + (r.getAssignedRoomNo() == null ? "" : r.getAssignedRoomNo()) + "|" + r.getCheckInDate() + "|"
                + r.getCheckOutDate() + "|" + r.getBookingStatus() + "|" + r.getPriorityScore() + "|" + r.getTimestamp();
    }

    // =========================================================================
    // LOAD ALL ADTs FROM TEXT FILES
    // =========================================================================

    public static void loadAllData(DoublyLinkedList<Guest> guestRegistry,
                                   DoublyLinkedList<Room> roomInventory,
                                   LinkedQueue<Reservation> standardQueue,
                                   BinaryMaxHeap<VIPReservation> vipQueue,
                                   BinarySearchTree<Reservation> searchTree,
                                   DoublyLinkedList<LoyaltyAccount> loyaltyAccounts,
                                   DoublyLinkedList<RedemptionTransaction> redemptionLog,
                                   DoublyLinkedList<Partner> partnerRegistry,
                                   DoublyLinkedList<CustomerReferral> referralLog) {
        loadGuests(guestRegistry);
        loadRooms(roomInventory);
        File reservationsFile = new File(DATA_DIR + "/reservations.txt");
        if (reservationsFile.exists()) {
            loadReservations(guestRegistry, standardQueue, vipQueue, searchTree);
        } else {
            loadStandardQueue(standardQueue, searchTree, guestRegistry);
            loadVIPQueue(vipQueue, searchTree, guestRegistry);
        }
        loadLoyaltyAccounts(loyaltyAccounts);
        loadRedemptions(redemptionLog);
        loadPartners(partnerRegistry);
        loadReferrals(referralLog);
    }

    private static void loadGuests(DoublyLinkedList<Guest> list) {
        File file = new File(DATA_DIR + "/guests.txt");
        if (!file.exists()) return;

        int maxId = 1000;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    Guest g = new Guest(parts[1], parts[2], parts[3], parts[4], parts[5]);
                    g.setGuestId(parts[0]);
                    list.add(g);

                    try {
                        int num = Integer.parseInt(parts[0].replace("G", ""));
                        if (num >= maxId) maxId = num + 1;
                    } catch (Exception ignored) {}
                }
            }
            Guest.updateIdCounter(maxId);
        } catch (Exception e) {
            System.err.println("Error loading guests.txt: " + e.getMessage());
        }
    }

    private static void loadRooms(DoublyLinkedList<Room> list) {
        File file = new File(DATA_DIR + "/rooms.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    Room r = new Room(parts[0], parts[1], Double.parseDouble(parts[2]), parts[3]);
                    list.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading rooms.txt: " + e.getMessage());
        }
    }

    private static void loadStandardQueue(LinkedQueue<Reservation> queue, BinarySearchTree<Reservation> searchTree,
                                          DoublyLinkedList<Guest> guests) {
        File file = new File(DATA_DIR + "/standard_queue.txt");
        if (!file.exists()) return;

        int maxConf = 10000000;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Reservation r = deserializeReservation(line);
                if (r != null) {
                    r.setGuest(findGuest(guests, r.getGuestId()));
                    queue.enqueue(r);
                    searchTree.insert(r);
                    try {
                        int num = Integer.parseInt(r.getConfirmationNo());
                        if (num >= maxConf) maxConf = num + 1;
                    } catch (Exception ignored) {}
                }
            }
            Reservation.updateConfirmCounter(maxConf);
        } catch (Exception e) {
            System.err.println("Error loading standard_queue.txt: " + e.getMessage());
        }
    }

    private static void loadVIPQueue(BinaryMaxHeap<VIPReservation> vipQueue, BinarySearchTree<Reservation> searchTree,
                                     DoublyLinkedList<Guest> guests) {
        File file = new File(DATA_DIR + "/vip_queue.txt");
        if (!file.exists()) return;

        int maxConf = 10000000;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Reservation r = deserializeReservation(line);
                if (r != null) {
                    r.setGuest(findGuest(guests, r.getGuestId()));
                    vipQueue.enqueue(new VIPReservation(r));
                    searchTree.insert(r);
                    try {
                        int num = Integer.parseInt(r.getConfirmationNo());
                        if (num >= maxConf) maxConf = num + 1;
                    } catch (Exception ignored) {}
                }
            }
            Reservation.updateConfirmCounter(maxConf);
        } catch (Exception e) {
            System.err.println("Error loading vip_queue.txt: " + e.getMessage());
        }
    }

    private static void loadReservations(DoublyLinkedList<Guest> guests,
                                         LinkedQueue<Reservation> standardQueue,
                                         BinaryMaxHeap<VIPReservation> vipQueue,
                                         BinarySearchTree<Reservation> searchTree) {
        File file = new File(DATA_DIR + "/reservations.txt");
        int maxConf = 10000000;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Reservation reservation = deserializeReservation(line);
                if (reservation == null) continue;
                reservation.setGuest(findGuest(guests, reservation.getGuestId()));
                searchTree.insert(reservation);
                if ("PENDING".equals(reservation.getBookingStatus())) {
                    if (reservation.getPriorityScore() > 0) vipQueue.enqueue(new VIPReservation(reservation));
                    else standardQueue.enqueue(reservation);
                }
                try {
                    int num = Integer.parseInt(reservation.getConfirmationNo());
                    if (num >= maxConf) maxConf = num + 1;
                } catch (Exception ignored) {}
            }
            Reservation.updateConfirmCounter(maxConf);
        } catch (Exception e) {
            System.err.println("Error loading reservations.txt: " + e.getMessage());
        }
    }

    private static Guest findGuest(DoublyLinkedList<Guest> guests, String guestId) {
        for (int i = 1; i <= guests.getNumberOfEntries(); i++) {
            Guest guest = guests.getEntry(i);
            if (guest != null && guest.getGuestId().equals(guestId)) return guest;
        }
        return null;
    }

    private static void loadLoyaltyAccounts(DoublyLinkedList<LoyaltyAccount> list) {
        File file = new File(DATA_DIR + "/loyalty_accounts.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    LoyaltyAccount a = new LoyaltyAccount(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3]);
                    if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                        String[] entries = parts[4].split(";;;");
                        for (String entry : entries) {
                            if (!entry.trim().isEmpty()) a.addHistoryEntry(entry);
                        }
                    }
                    list.add(a);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading loyalty_accounts.txt: " + e.getMessage());
        }
    }

    private static void loadRedemptions(DoublyLinkedList<RedemptionTransaction> list) {
        File file = new File(DATA_DIR + "/redemptions.txt");
        if (!file.exists()) return;

        int maxTxn = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    RedemptionTransaction t = new RedemptionTransaction(parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]);
                    t.setTransactionId(parts[0]);
                    t.setStatus(parts[5]);
                    list.add(t);

                    try {
                        int num = Integer.parseInt(parts[0].replace("TXN", ""));
                        if (num >= maxTxn) maxTxn = num + 1;
                    } catch (Exception ignored) {}
                }
            }
            RedemptionTransaction.updateTxnCounter(maxTxn);
        } catch (Exception e) {
            System.err.println("Error loading redemptions.txt: " + e.getMessage());
        }
    }

    private static void loadPartners(DoublyLinkedList<Partner> list) {
        File file = new File(DATA_DIR + "/partners.txt");
        if (!file.exists()) return;

        int maxId = 1000;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 9) {
                    Partner p = new Partner(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                    p.setPartnerId(parts[0]);
                    p.setTotalReferralsCount(Integer.parseInt(parts[7]));
                    p.setTotalRevenueGenerated(Double.parseDouble(parts[8]));
                    list.add(p);

                    try {
                        int num = Integer.parseInt(parts[0].replace("P", ""));
                        if (num >= maxId) maxId = num + 1;
                    } catch (Exception ignored) {}
                }
            }
            Partner.updateIdCounter(maxId);
        } catch (Exception e) {
            System.err.println("Error loading partners.txt: " + e.getMessage());
        }
    }

    private static void loadReferrals(DoublyLinkedList<CustomerReferral> list) {
        File file = new File(DATA_DIR + "/referrals.txt");
        if (!file.exists()) return;

        int maxId = 1000;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 9) {
                    CustomerReferral r = new CustomerReferral(parts[1], parts[2], parts[3], parts[4], parts[5], Double.parseDouble(parts[6]), parts[7]);
                    r.setReferralId(parts[0]);
                    r.setStatus(parts[8]);
                    list.add(r);

                    try {
                        int num = Integer.parseInt(parts[0].replace("REF", ""));
                        if (num >= maxId) maxId = num + 1;
                    } catch (Exception ignored) {}
                }
            }
            CustomerReferral.updateIdCounter(maxId);
        } catch (Exception e) {
            System.err.println("Error loading referrals.txt: " + e.getMessage());
        }
    }

    private static Reservation deserializeReservation(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 9) {
            Reservation r = new Reservation(parts[1], parts[2], parts[4], parts[5]);
            r.setConfirmationNo(parts[0]);
            if (!parts[3].isEmpty()) r.setAssignedRoomNo(parts[3]);
            r.setBookingStatus(parts[6]);
            r.setPriorityScore(Integer.parseInt(parts[7]));
            r.setTimestamp(Long.parseLong(parts[8]));
            return r;
        }
        return null;
    }
}

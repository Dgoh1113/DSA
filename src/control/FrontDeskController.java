package control;

import adt.BinarySearchTree;
import adt.DoublyLinkedList;
import adt.SortAlgorithms;
import entity.BillingRecord;
import entity.FrontDeskLog;
import entity.Guest;
import entity.LoyaltyAccount;
import entity.Reservation;
import entity.Room;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import utility.FilePersistenceUtils;

/**
 * Controller: Module 3 — Front-Desk Service.
 * Handles BST searching, check-in/check-out, billing, and reservation management.
 *
 * Business Rules:
 * - Front-desk agents search reservations by 8-digit confirmationNo.
 * - BST search traverses left/right based on numeric comparison — O(log n) average.
 * - In-order traversal prints sorted booking logs efficiently.
 * Receives shared ADT instances from Main.java.
 */
public class FrontDeskController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String ROOM_AVAILABLE = "AVAILABLE";
    private static final String ROOM_OCCUPIED = "OCCUPIED";
    private static final String ROOM_MAINTENANCE = "MAINTENANCE";
    private static final String PAYMENT_PAID = "PAID";
    private static final String PAYMENT_UNPAID = "UNPAID";
    private static final String BILL_OPEN = "OPEN";
    private static final String BILL_FINAL = "FINAL";
    private static final String BILL_VOID = "VOID";
    private static final double TAX_RATE = 0.06;

    private BinarySearchTree<Reservation> searchTree;
    private DoublyLinkedList<Guest> guestRegistry;
    private DoublyLinkedList<Room> roomInventory;
    private LoyaltyController loyaltyController;
    private UndoController undoController;
    private DoublyLinkedList<FrontDeskLog> checkInLog;
    private DoublyLinkedList<FrontDeskLog> checkOutLog;
    private DoublyLinkedList<FrontDeskLog> cancellationLog;
    private DoublyLinkedList<BillingRecord> billingLog;

    public FrontDeskController(BinarySearchTree<Reservation> searchTree,
                               DoublyLinkedList<Guest> guestRegistry,
                               DoublyLinkedList<Room> roomInventory,
                               LoyaltyController loyaltyController,
                               DoublyLinkedList<FrontDeskLog> checkInLog,
                               DoublyLinkedList<FrontDeskLog> checkOutLog,
                               DoublyLinkedList<FrontDeskLog> cancellationLog,
                               DoublyLinkedList<BillingRecord> billingLog) {
        this.searchTree = searchTree;
        this.guestRegistry = guestRegistry;
        this.roomInventory = roomInventory;
        this.loyaltyController = loyaltyController;
        this.checkInLog = checkInLog != null ? checkInLog : new DoublyLinkedList<FrontDeskLog>();
        this.checkOutLog = checkOutLog != null ? checkOutLog : new DoublyLinkedList<FrontDeskLog>();
        this.cancellationLog = cancellationLog != null ? cancellationLog : new DoublyLinkedList<FrontDeskLog>();
        this.billingLog = billingLog != null ? billingLog : new DoublyLinkedList<BillingRecord>();
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    /**
     * Creates a dummy Reservation with the given confirmationNo for comparison.
     * Average O(log n) time complexity.
     *
     * @param confirmationNo The 8-digit confirmation number to search for.
     * @return The matching Reservation, or null if not found.
     */
    public Reservation searchReservation(String confirmationNo) {
        Reservation searchKey = new Reservation();
        searchKey.setConfirmationNo(confirmationNo);
        return searchTree.search(searchKey);
    }

    /**
     * Checks in a guest: marks reservation as CHECKED_IN and room as OCCUPIED.
     *
     * @param confirmationNo The 8-digit confirmation number.
     * @return true if check-in was successful, false if reservation not found or invalid state.
     */
    public boolean checkIn(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return false;
        }

        if (!STATUS_CONFIRMED.equals(reservation.getBookingStatus())) {
            return false; // Can only check in from CONFIRMED status
        }

        if (!isCheckInDateReached(reservation.getCheckInDate())) {
            return false;
        }

        Room room = findRoom(reservation.getAssignedRoomNo());
        if (room == null || ROOM_MAINTENANCE.equals(room.getStatus())) {
            return false;
        }

        String previousRoomStatus = room.getStatus();
        reservation.setBookingStatus(STATUS_CHECKED_IN);
        room.setStatus(ROOM_OCCUPIED);

        appendCheckInLog(reservation);
        upsertBillingSnapshot(reservation);

        if (undoController != null) {
            undoController.recordAction(
                "CHECK_IN",
                "Module 3: Front-Desk Service",
                "Check-In Conf #" + confirmationNo + " (Room " + reservation.getAssignedRoomNo() + ")",
                () -> {
                    reservation.setBookingStatus(STATUS_CONFIRMED);
                    room.setStatus(previousRoomStatus != null ? previousRoomStatus : ROOM_OCCUPIED);
                    persistFrontDeskData();
                }
            );
        }

        persistFrontDeskData();
        return true;
    }

    /**
     * Checks out a guest without changing payment status.
     */
    public Reservation checkOut(String confirmationNo) {
        return checkOut(confirmationNo, false);
    }

    /**
     * Checks out a guest: marks reservation as CHECKED_OUT, frees room,
     * records payment if received, and triggers loyalty points accrual.
     *
     * @param confirmationNo The 8-digit confirmation number.
     * @param paymentReceived true when the guest has settled the bill at check-out.
     * @return The check-out Reservation, or null if not found or invalid state.
     */
    public Reservation checkOut(String confirmationNo, boolean paymentReceived) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return null;
        }

        if (!STATUS_CHECKED_IN.equals(reservation.getBookingStatus())) {
            return null; // Can only check out from CHECKED_IN status
        }

        String previousPayment = reservation.getPaymentStatus();
        reservation.setBookingStatus(STATUS_CHECKED_OUT);
        if (paymentReceived) {
            reservation.setPaymentStatus(PAYMENT_PAID);
        } else if (reservation.getPaymentStatus() == null || reservation.getPaymentStatus().trim().isEmpty()) {
            reservation.setPaymentStatus(PAYMENT_UNPAID);
        }

        Room room = findRoom(reservation.getAssignedRoomNo());
        Guest guest = findGuest(reservation.getGuestId());
        int nights = calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
        int pointsAwarded = 0;

        if (room != null) {
            room.setStatus(ROOM_AVAILABLE);

            if (loyaltyController != null && guest != null) {
                pointsAwarded = (int) (room.getNightlyRate() * nights);
                loyaltyController.accruePointsByContactNo(guest.getContactNo(),
                                                          room.getNightlyRate(), nights);
            }
        }

        BillingDetails bill = buildBillingDetails(reservation);
        appendCheckOutLog(reservation, bill);
        upsertBillingSnapshot(reservation);

        final int pointsToReverse = pointsAwarded;
        final Guest checkedOutGuest = guest;

        if (undoController != null) {
            undoController.recordAction(
                "CHECK_OUT",
                "Module 3: Front-Desk Service",
                "Check-Out Conf #" + confirmationNo + " (Room " + reservation.getAssignedRoomNo() + ")",
                () -> {
                    reservation.setBookingStatus(STATUS_CHECKED_IN);
                    reservation.setPaymentStatus(previousPayment);
                    if (room != null) {
                        room.setStatus(ROOM_OCCUPIED);
                    }
                    reverseAccruedPoints(checkedOutGuest, pointsToReverse, confirmationNo);
                    persistFrontDeskData();
                }
            );
        }

        persistFrontDeskData();
        return reservation;
    }

    /**
     * Cancels a reservation.
     *
     * @param confirmationNo The confirmation number to cancel.
     * @return true if cancellation was successful.
     */
    public boolean cancelReservation(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return false;
        }

        if (!isCancellable(reservation)) {
            return false;
        }

        String prevStatus = reservation.getBookingStatus();
        reservation.setBookingStatus(STATUS_CANCELLED);

        Room room = findRoom(reservation.getAssignedRoomNo());
        String previousRoomStatus = room != null ? room.getStatus() : null;
        if (room != null && STATUS_CONFIRMED.equals(prevStatus)) {
            room.setStatus(ROOM_AVAILABLE);
        }

        appendCancellationLog(reservation, prevStatus);
        upsertBillingSnapshot(reservation);

        if (undoController != null) {
            undoController.recordAction(
                "CANCEL_RESERVATION",
                "Module 3: Front-Desk Service",
                "Cancelled Conf #" + confirmationNo,
                () -> {
                    reservation.setBookingStatus(prevStatus);
                    if (room != null && STATUS_CONFIRMED.equals(prevStatus)
                            && ROOM_AVAILABLE.equals(room.getStatus())) {
                        room.setStatus(previousRoomStatus != null ? previousRoomStatus : ROOM_OCCUPIED);
                    }
                    persistFrontDeskData();
                }
            );
        }

        persistFrontDeskData();
        return true;
    }

    /**
     * Returns all reservations in sorted order via BST in-order traversal.
     * Used for printing sorted booking logs.
     */
    public DoublyLinkedList<Reservation> getAllReservationsSorted() {
        return searchTree.inOrderTraversal();
    }

    /** Returns reservations matching a booking status, sorted by confirmation number. */
    public DoublyLinkedList<Reservation> getReservationsByStatus(String bookingStatus) {
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        if (bookingStatus == null) return matches;

        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null
                    && bookingStatus.equalsIgnoreCase(reservation.getBookingStatus())) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    /** Returns allocated bookings that have not started and can still be cancelled. */
    public DoublyLinkedList<Reservation> getCancellableReservations() {
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation != null && isCancellable(reservation)) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    private boolean isCancellable(Reservation reservation) {
        if (STATUS_CONFIRMED.equals(reservation.getBookingStatus())) {
            return true;
        }
        return STATUS_PENDING.equals(reservation.getBookingStatus())
                && hasAssignedRoom(reservation);
    }

    private boolean hasAssignedRoom(Reservation reservation) {
        String roomNo = reservation.getAssignedRoomNo();
        return roomNo != null && !roomNo.trim().isEmpty();
    }

    /** Returns reservations that still have an outstanding (UNPAID) bill. */
    public DoublyLinkedList<Reservation> getUnpaidReservations() {
        DoublyLinkedList<Reservation> matches = new DoublyLinkedList<>();
        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation == null || STATUS_CANCELLED.equals(reservation.getBookingStatus())) {
                continue;
            }
            String payment = reservation.getPaymentStatus();
            if (payment == null || PAYMENT_UNPAID.equalsIgnoreCase(payment)) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    /**
     * Builds a live billing snapshot for the given confirmation number.
     */
    public BillingDetails queryBillingDetails(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null) {
            return null;
        }
        return buildBillingDetails(reservation);
    }

    /**
     * Marks a reservation bill as PAID and refreshes the stored billing snapshot.
     */
    public boolean recordPayment(String confirmationNo) {
        Reservation reservation = searchReservation(confirmationNo);
        if (reservation == null || STATUS_CANCELLED.equals(reservation.getBookingStatus())) {
            return false;
        }
        if (PAYMENT_PAID.equalsIgnoreCase(reservation.getPaymentStatus())) {
            return false;
        }

        String previousPayment = reservation.getPaymentStatus();
        reservation.setPaymentStatus(PAYMENT_PAID);
        upsertBillingSnapshot(reservation);

        if (undoController != null) {
            undoController.recordAction(
                "RECORD_PAYMENT",
                "Module 3: Front-Desk Service",
                "Recorded payment Conf #" + confirmationNo,
                () -> {
                    reservation.setPaymentStatus(previousPayment != null ? previousPayment : PAYMENT_UNPAID);
                    upsertBillingSnapshot(reservation);
                    persistFrontDeskData();
                }
            );
        }

        persistFrontDeskData();
        return true;
    }

    public DoublyLinkedList<FrontDeskLog> getCheckInLog() {
        return checkInLog;
    }

    public DoublyLinkedList<FrontDeskLog> getCheckOutLog() {
        return checkOutLog;
    }

    public DoublyLinkedList<FrontDeskLog> getCancellationLog() {
        return cancellationLog;
    }

    public DoublyLinkedList<BillingRecord> getBillingLog() {
        return billingLog;
    }

    /**
     * Combines stored check-in, check-out, cancellation, and billing files
     * into one list, then filters by room type, record status, and payment
     * status. Matching rows are ranked with MergeSort.
     */
    public StoredFrontDeskReport generateStoredFrontDeskReport(
            String roomType, String recordStatus, String paymentStatus) {
        DoublyLinkedList<StoredFrontDeskRecord> combined = new DoublyLinkedList<>();
        appendLogRecords(combined, checkInLog, "CHECK_IN");
        appendLogRecords(combined, checkOutLog, "CHECK_OUT");
        appendLogRecords(combined, cancellationLog, "CANCELLED");
        appendBillingRecords(combined);

        DoublyLinkedList<StoredFrontDeskRecord> matches = new DoublyLinkedList<>();
        for (int i = 1; i <= combined.getNumberOfEntries(); i++) {
            StoredFrontDeskRecord record = combined.getEntry(i);
            if (record == null) {
                continue;
            }
            if (!"ALL".equals(roomType) && !roomType.equalsIgnoreCase(safeText(record.getRoomType()))) {
                continue;
            }
            if (!"ALL".equals(recordStatus)
                    && !recordStatus.equalsIgnoreCase(safeText(record.getRecordStatus()))) {
                continue;
            }
            if (!"ALL".equals(paymentStatus)
                    && !paymentStatus.equalsIgnoreCase(safeText(record.getPaymentStatus()))) {
                continue;
            }
            matches.add(record);
        }

        DoublyLinkedList<StoredFrontDeskRecord> sortedMatches = SortAlgorithms.mergeSort(matches,
                (first, second) -> {
                    int statusComparison = recordStatusOrder(first.getRecordStatus())
                            - recordStatusOrder(second.getRecordStatus());
                    if (statusComparison != 0) {
                        return statusComparison;
                    }
                    int paymentComparison = safeText(first.getPaymentStatus())
                            .compareTo(safeText(second.getPaymentStatus()));
                    if (paymentComparison != 0) {
                        return paymentComparison;
                    }
                    return safeText(first.getConfirmationNo())
                            .compareTo(safeText(second.getConfirmationNo()));
                });
        return new StoredFrontDeskReport(roomType, recordStatus, paymentStatus, sortedMatches);
    }

    private void appendLogRecords(DoublyLinkedList<StoredFrontDeskRecord> destination,
                                  DoublyLinkedList<FrontDeskLog> logs,
                                  String recordStatus) {
        for (int i = 1; i <= logs.getNumberOfEntries(); i++) {
            FrontDeskLog log = logs.getEntry(i);
            if (log == null) {
                continue;
            }
            String payment = log.getPaymentStatus() == null || log.getPaymentStatus().trim().isEmpty()
                    ? PAYMENT_UNPAID : log.getPaymentStatus();
            destination.add(new StoredFrontDeskRecord(
                    log.getConfirmationNo(),
                    log.getGuestId(),
                    log.getGuestName(),
                    log.getRoomNo(),
                    log.getRoomType(),
                    recordStatus,
                    payment,
                    log.getGrandTotal(),
                    log.getLoggedAt()));
        }
    }

    private void appendBillingRecords(DoublyLinkedList<StoredFrontDeskRecord> destination) {
        for (int i = 1; i <= billingLog.getNumberOfEntries(); i++) {
            BillingRecord bill = billingLog.getEntry(i);
            if (bill == null) {
                continue;
            }
            String recordStatus = toRecordStatus(bill.getBookingStatus());
            String payment = bill.getPaymentStatus() == null || bill.getPaymentStatus().trim().isEmpty()
                    ? PAYMENT_UNPAID : bill.getPaymentStatus();
            destination.add(new StoredFrontDeskRecord(
                    bill.getConfirmationNo(),
                    bill.getGuestId(),
                    bill.getGuestName(),
                    bill.getRoomNo(),
                    bill.getRoomType(),
                    recordStatus,
                    payment,
                    bill.getGrandTotal(),
                    bill.getRecordedAt()));
        }
    }

    private String toRecordStatus(String bookingStatus) {
        if (STATUS_CHECKED_IN.equalsIgnoreCase(bookingStatus)) {
            return "CHECK_IN";
        }
        if (STATUS_CHECKED_OUT.equalsIgnoreCase(bookingStatus)) {
            return "CHECK_OUT";
        }
        if (STATUS_CANCELLED.equalsIgnoreCase(bookingStatus)) {
            return "CANCELLED";
        }
        return bookingStatus == null ? "" : bookingStatus.trim().toUpperCase();
    }

    private int recordStatusOrder(String recordStatus) {
        if ("CHECK_IN".equalsIgnoreCase(recordStatus)) return 1;
        if ("CHECK_OUT".equalsIgnoreCase(recordStatus)) return 2;
        if ("CANCELLED".equalsIgnoreCase(recordStatus)) return 3;
        return 4;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Reconstructs stored front-desk files from live reservations when the
     * log lists were loaded empty (for example after an older data set).
     */
    public void backfillStoredRecordsIfEmpty() {
        DoublyLinkedList<Reservation> reservations = searchTree.inOrderTraversal();
        boolean fillCheckIns = checkInLog.isEmpty();
        boolean fillCheckOuts = checkOutLog.isEmpty();
        boolean fillCancellations = cancellationLog.isEmpty();
        boolean fillBilling = billingLog.isEmpty();

        if (!fillCheckIns && !fillCheckOuts && !fillCancellations && !fillBilling) {
            return;
        }

        for (int i = 1; i <= reservations.getNumberOfEntries(); i++) {
            Reservation reservation = reservations.getEntry(i);
            if (reservation == null) {
                continue;
            }
            String status = reservation.getBookingStatus();
            if (fillCheckIns && (STATUS_CHECKED_IN.equals(status) || STATUS_CHECKED_OUT.equals(status))) {
                appendCheckInLog(reservation);
            }
            if (fillCheckOuts && STATUS_CHECKED_OUT.equals(status)) {
                appendCheckOutLog(reservation, buildBillingDetails(reservation));
            }
            if (fillCancellations && STATUS_CANCELLED.equals(status)) {
                appendCancellationLog(reservation, STATUS_CONFIRMED);
            }
            if (fillBilling) {
                upsertBillingSnapshot(reservation);
            }
        }
        persistFrontDeskData();
    }

    /**
     * Returns the total number of reservations in the BST.
     */
    public int getReservationCount() {
        return searchTree.size();
    }

    /**
     * Finds a guest by guestId in the guest registry.
     */
    public Guest findGuest(String guestId) {
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest guest = guestRegistry.getEntry(i);
            if (guest.getGuestId().equals(guestId)) {
                return guest;
            }
        }
        return null;
    }

    /**
     * Finds a room by room number in the inventory.
     */
    public Room findRoom(String roomNo) {
        if (roomNo == null) return null;
        for (int i = 1; i <= roomInventory.getNumberOfEntries(); i++) {
            Room room = roomInventory.getEntry(i);
            if (room.getRoomNo().equals(roomNo)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Gets all rooms and their current status.
     */
    public DoublyLinkedList<Room> getRoomInventory() {
        return roomInventory;
    }

    /** Returns true when the confirmation number is an 8-digit value. */
    public boolean isValidConfirmationNo(String confirmationNo) {
        return confirmationNo != null && confirmationNo.matches("\\d{8}");
    }

    /** Returns true when today's date is on or after the booked check-in date. */
    public boolean isCheckInDateReached(String checkInDate) {
        LocalDate parsed = parseDate(checkInDate);
        if (parsed == null) {
            return true;
        }
        return !LocalDate.now().isBefore(parsed);
    }

    /**
     * Night calculation from date strings (format: YYYY-MM-DD).
     * Uses calendar dates so month and year boundaries are counted correctly.
     */
    public int calculateNights(String checkIn, String checkOut) {
        LocalDate inDate = parseDate(checkIn);
        LocalDate outDate = parseDate(checkOut);
        if (inDate == null || outDate == null) {
            return 1;
        }
        long nights = ChronoUnit.DAYS.between(inDate, outDate);
        return (nights > 0) ? (int) nights : 1;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reverses points credited at check-out using LoyaltyController public APIs
     * without changing Module 4 source.
     */
    private void reverseAccruedPoints(Guest guest, int pointsAwarded, String confirmationNo) {
        if (loyaltyController == null || guest == null || pointsAwarded <= 0) {
            return;
        }
        LoyaltyAccount account = loyaltyController.findAccount(guest.getGuestId());
        if (account == null) {
            return;
        }
        int restored = Math.max(0, account.getTotalPoints() - pointsAwarded);
        account.setTotalPoints(restored);
        account.addHistoryEntry("-" + pointsAwarded + " pts (Undo check-out Conf #" + confirmationNo + ")");
        loyaltyController.checkAndUpgradeTier(guest.getGuestId());
    }

    private BillingDetails buildBillingDetails(Reservation reservation) {
        Guest guest = findGuest(reservation.getGuestId());
        Room room = findRoom(reservation.getAssignedRoomNo());
        String tier = guest != null && guest.getLoyaltyTier() != null ? guest.getLoyaltyTier() : "STANDARD";
        int nights = calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
        double nightlyRate = room != null ? room.getNightlyRate() : 0.0;
        double roomCharges = nightlyRate * nights;
        double discountRate = discountRateForTier(tier);
        double discountAmount = roomCharges * discountRate;
        double taxable = Math.max(0.0, roomCharges - discountAmount);
        double taxAmount = taxable * TAX_RATE;
        double grandTotal = taxable + taxAmount;

        String bookingStatus = reservation.getBookingStatus();
        String billStatus = STATUS_CANCELLED.equals(bookingStatus) ? BILL_VOID
                : STATUS_CHECKED_OUT.equals(bookingStatus) ? BILL_FINAL
                : BILL_OPEN;
        String paymentStatus = reservation.getPaymentStatus() == null || reservation.getPaymentStatus().trim().isEmpty()
                ? PAYMENT_UNPAID : reservation.getPaymentStatus();
        if (BILL_VOID.equals(billStatus)) {
            grandTotal = 0.0;
            taxAmount = 0.0;
            discountAmount = 0.0;
        }

        BillingRecord record = new BillingRecord();
        record.setConfirmationNo(reservation.getConfirmationNo());
        record.setGuestId(reservation.getGuestId());
        record.setGuestName(guest != null ? guest.getName() : reservation.getGuestId());
        record.setLoyaltyTier(tier);
        record.setRoomNo(reservation.getAssignedRoomNo());
        record.setRoomType(reservation.getRoomType());
        record.setCheckInDate(reservation.getCheckInDate());
        record.setCheckOutDate(reservation.getCheckOutDate());
        record.setNights(nights);
        record.setNightlyRate(nightlyRate);
        record.setRoomCharges(roomCharges);
        record.setDiscountRate(discountRate);
        record.setDiscountAmount(discountAmount);
        record.setTaxRate(TAX_RATE);
        record.setTaxAmount(taxAmount);
        record.setGrandTotal(grandTotal);
        record.setBillStatus(billStatus);
        record.setPaymentStatus(paymentStatus);
        record.setBookingStatus(bookingStatus);
        record.setRecordedAt(nowStamp());
        return new BillingDetails(record);
    }

    private double discountRateForTier(String tier) {
        if (tier == null) {
            return 0.0;
        }
        switch (tier.toUpperCase()) {
            case "SILVER":
                return 0.05;
            case "GOLD":
                return 0.10;
            case "PLATINUM":
                return 0.15;
            case "DIAMOND":
                return 0.20;
            default:
                return 0.0;
        }
    }

    private void upsertBillingSnapshot(Reservation reservation) {
        BillingDetails details = buildBillingDetails(reservation);
        BillingRecord snapshot = details.toRecord();
        for (int i = 1; i <= billingLog.getNumberOfEntries(); i++) {
            BillingRecord existing = billingLog.getEntry(i);
            if (existing != null && snapshot.getConfirmationNo().equals(existing.getConfirmationNo())) {
                copyBilling(snapshot, existing);
                return;
            }
        }
        billingLog.add(snapshot);
    }

    private void copyBilling(BillingRecord from, BillingRecord to) {
        to.setGuestId(from.getGuestId());
        to.setGuestName(from.getGuestName());
        to.setLoyaltyTier(from.getLoyaltyTier());
        to.setRoomNo(from.getRoomNo());
        to.setRoomType(from.getRoomType());
        to.setCheckInDate(from.getCheckInDate());
        to.setCheckOutDate(from.getCheckOutDate());
        to.setNights(from.getNights());
        to.setNightlyRate(from.getNightlyRate());
        to.setRoomCharges(from.getRoomCharges());
        to.setDiscountRate(from.getDiscountRate());
        to.setDiscountAmount(from.getDiscountAmount());
        to.setTaxRate(from.getTaxRate());
        to.setTaxAmount(from.getTaxAmount());
        to.setGrandTotal(from.getGrandTotal());
        to.setBillStatus(from.getBillStatus());
        to.setPaymentStatus(from.getPaymentStatus());
        to.setBookingStatus(from.getBookingStatus());
        to.setRecordedAt(from.getRecordedAt());
    }

    private void appendCheckInLog(Reservation reservation) {
        Guest guest = findGuest(reservation.getGuestId());
        FrontDeskLog log = new FrontDeskLog();
        log.setLoggedAt(nowStamp());
        log.setConfirmationNo(reservation.getConfirmationNo());
        log.setGuestId(reservation.getGuestId());
        log.setGuestName(guest != null ? guest.getName() : reservation.getGuestId());
        log.setRoomNo(reservation.getAssignedRoomNo());
        log.setRoomType(reservation.getRoomType());
        log.setCheckInDate(reservation.getCheckInDate());
        checkInLog.add(log);
    }

    private void appendCheckOutLog(Reservation reservation, BillingDetails bill) {
        Guest guest = findGuest(reservation.getGuestId());
        FrontDeskLog log = new FrontDeskLog();
        log.setLoggedAt(nowStamp());
        log.setConfirmationNo(reservation.getConfirmationNo());
        log.setGuestId(reservation.getGuestId());
        log.setGuestName(guest != null ? guest.getName() : reservation.getGuestId());
        log.setRoomNo(reservation.getAssignedRoomNo());
        log.setRoomType(reservation.getRoomType());
        log.setCheckInDate(reservation.getCheckInDate());
        log.setCheckOutDate(reservation.getCheckOutDate());
        log.setNights(bill != null ? bill.getNights() : calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate()));
        log.setPaymentStatus(reservation.getPaymentStatus());
        log.setGrandTotal(bill != null ? bill.getGrandTotal() : 0.0);
        checkOutLog.add(log);
    }

    private void appendCancellationLog(Reservation reservation, String previousStatus) {
        Guest guest = findGuest(reservation.getGuestId());
        FrontDeskLog log = new FrontDeskLog();
        log.setLoggedAt(nowStamp());
        log.setConfirmationNo(reservation.getConfirmationNo());
        log.setGuestId(reservation.getGuestId());
        log.setGuestName(guest != null ? guest.getName() : reservation.getGuestId());
        log.setRoomNo(reservation.getAssignedRoomNo());
        log.setRoomType(reservation.getRoomType());
        log.setPreviousStatus(previousStatus);
        cancellationLog.add(log);
    }

    private void persistFrontDeskData() {
        FilePersistenceUtils.saveFrontDeskData(
                checkInLog, checkOutLog, cancellationLog, billingLog, searchTree, roomInventory);
    }

    private String nowStamp() {
        return LocalDateTime.now().toString();
    }

    /**
     * Live billing view used by the front-desk UI.
     */
    public static class BillingDetails {
        private final BillingRecord record;

        private BillingDetails(BillingRecord record) {
            this.record = record;
        }

        private BillingRecord toRecord() {
            return record;
        }

        public String getConfirmationNo() { return record.getConfirmationNo(); }
        public String getBillStatus() { return record.getBillStatus(); }
        public String getPaymentStatus() { return record.getPaymentStatus(); }
        public String getBookingStatus() { return record.getBookingStatus(); }
        public String getGuestId() { return record.getGuestId(); }
        public String getGuestName() { return record.getGuestName(); }
        public String getLoyaltyTier() { return record.getLoyaltyTier(); }
        public String getRoomNo() { return record.getRoomNo(); }
        public String getRoomType() { return record.getRoomType(); }
        public String getCheckInDate() { return record.getCheckInDate(); }
        public String getCheckOutDate() { return record.getCheckOutDate(); }
        public int getNights() { return record.getNights(); }
        public double getNightlyRate() { return record.getNightlyRate(); }
        public double getRoomCharges() { return record.getRoomCharges(); }
        public double getDiscountAmount() { return record.getDiscountAmount(); }
        public double getDiscountRate() { return record.getDiscountRate(); }
        public double getTaxRate() { return record.getTaxRate(); }
        public double getTaxAmount() { return record.getTaxAmount(); }
        public double getGrandTotal() { return record.getGrandTotal(); }
    }

    /** One combined row from stored check-in, check-out, cancellation, or billing files. */
    public static class StoredFrontDeskRecord {
        private final String confirmationNo;
        private final String guestId;
        private final String guestName;
        private final String roomNo;
        private final String roomType;
        private final String recordStatus;
        private final String paymentStatus;
        private final double grandTotal;
        private final String recordedAt;

        private StoredFrontDeskRecord(String confirmationNo, String guestId,
                                      String guestName, String roomNo, String roomType,
                                      String recordStatus, String paymentStatus,
                                      double grandTotal, String recordedAt) {
            this.confirmationNo = confirmationNo;
            this.guestId = guestId;
            this.guestName = guestName;
            this.roomNo = roomNo;
            this.roomType = roomType;
            this.recordStatus = recordStatus;
            this.paymentStatus = paymentStatus;
            this.grandTotal = grandTotal;
            this.recordedAt = recordedAt;
        }

        public String getConfirmationNo() { return confirmationNo; }
        public String getGuestId() { return guestId; }
        public String getGuestName() { return guestName; }
        public String getRoomNo() { return roomNo; }
        public String getRoomType() { return roomType; }
        public String getRecordStatus() { return recordStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public double getGrandTotal() { return grandTotal; }
        public String getRecordedAt() { return recordedAt; }
    }

    /** Filter + MergeSort result for the combined stored front-desk files report. */
    public static class StoredFrontDeskReport {
        private final String roomType;
        private final String recordStatus;
        private final String paymentStatus;
        private final DoublyLinkedList<StoredFrontDeskRecord> records;
        private int checkInCount;
        private int checkOutCount;
        private int cancelledCount;
        private int paidCount;
        private int unpaidCount;

        private StoredFrontDeskReport(String roomType, String recordStatus,
                                      String paymentStatus,
                                      DoublyLinkedList<StoredFrontDeskRecord> records) {
            this.roomType = roomType;
            this.recordStatus = recordStatus;
            this.paymentStatus = paymentStatus;
            this.records = records;
            summarise();
        }

        private void summarise() {
            for (int i = 1; i <= records.getNumberOfEntries(); i++) {
                StoredFrontDeskRecord record = records.getEntry(i);
                String status = record.getRecordStatus();
                if ("CHECK_IN".equalsIgnoreCase(status)) checkInCount++;
                else if ("CHECK_OUT".equalsIgnoreCase(status)) checkOutCount++;
                else if ("CANCELLED".equalsIgnoreCase(status)) cancelledCount++;

                if ("PAID".equalsIgnoreCase(record.getPaymentStatus())) paidCount++;
                else unpaidCount++;
            }
        }

        public String getRoomType() { return roomType; }
        public String getRecordStatus() { return recordStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public DoublyLinkedList<StoredFrontDeskRecord> getRecords() { return records; }
        public int getTotalRecords() { return records.getNumberOfEntries(); }
        public int getCheckInCount() { return checkInCount; }
        public int getCheckOutCount() { return checkOutCount; }
        public int getCancelledCount() { return cancelledCount; }
        public int getPaidCount() { return paidCount; }
        public int getUnpaidCount() { return unpaidCount; }
    }
}

package control;

import adt.DoublyLinkedList;
import adt.SortAlgorithms;
import entity.CustomerReferral;
import entity.Guest;
import entity.Partner;

/**
 * Controller: PartnerController — Manages business logic for Module 5 (Strategic Partners & Referrals).
 * Coordinates strategic partner profiles, customer referrals, product introductions based on customer stage,
 * and management reporting using MergeSort and QuickSort.
 */
public class PartnerController {

    private DoublyLinkedList<Partner> partnerRegistry;
    private DoublyLinkedList<CustomerReferral> referralLog;
    private DoublyLinkedList<Guest> guestRegistry;
    private UndoController undoController;

    public PartnerController(DoublyLinkedList<Partner> partnerRegistry,
                             DoublyLinkedList<CustomerReferral> referralLog,
                             DoublyLinkedList<Guest> guestRegistry) {
        this.partnerRegistry = partnerRegistry;
        this.referralLog = referralLog;
        this.guestRegistry = guestRegistry;
    }

    public void setUndoController(UndoController undoController) {
        this.undoController = undoController;
    }

    /**
     * Registers a new strategic partner (Property Developer, Contractor, or Interior Design Firm).
     */
    public Partner registerPartner(String companyName, String category, String contactPerson,
                                   String contactPhone, String email, String offeredServices) {
        Partner partner = new Partner(companyName, category.toUpperCase(), contactPerson, contactPhone, email, offeredServices);
        partnerRegistry.add(partner);

        if (undoController != null) {
            undoController.recordAction(
                "REGISTER_PARTNER",
                "Module 5: Strategic Partners",
                "Registered Strategic Partner: " + companyName + " (" + category + ")",
                () -> {
                    for (int i = 1; i <= partnerRegistry.getNumberOfEntries(); i++) {
                        if (partnerRegistry.getEntry(i).equals(partner)) {
                            partnerRegistry.remove(i);
                            break;
                        }
                    }
                }
            );
        }

        return partner;
    }

    /**
     * Finds a partner by partnerId (e.g. P1001).
     */
    public Partner findPartnerById(String partnerId) {
        for (int i = 1; i <= partnerRegistry.getNumberOfEntries(); i++) {
            Partner p = partnerRegistry.getEntry(i);
            if (p != null && p.getPartnerId().equalsIgnoreCase(partnerId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns all registered partners.
     */
    public DoublyLinkedList<Partner> getAllPartners() {
        return partnerRegistry;
    }

    /**
     * Returns partners filtered by category (PROPERTY_DEVELOPER, RENOVATION_CONTRACTOR, ELECTRICAL_CONTRACTOR, INTERIOR_DESIGN_FIRM).
     */
    public DoublyLinkedList<Partner> getPartnersByCategory(String category) {
        DoublyLinkedList<Partner> filtered = new DoublyLinkedList<>();
        for (int i = 1; i <= partnerRegistry.getNumberOfEntries(); i++) {
            Partner p = partnerRegistry.getEntry(i);
            if (p != null && p.getPartnerCategory().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    /**
     * Records a new customer product/service referral introduced by a partner.
     * Updates the partner's referral count and total revenue generated.
     */
    public CustomerReferral recordReferral(String partnerId, String guestId, String customerName,
                                          String customerStage, String productIntroduced,
                                          double dealAmount, String referralDate) {
        Partner partner = findPartnerById(partnerId);
        if (partner == null) {
            return null;
        }

        // Auto-fetch guest name if guestId provided and customerName is empty
        if (guestId != null && !guestId.trim().isEmpty() && (customerName == null || customerName.trim().isEmpty())) {
            Guest guest = findGuestById(guestId);
            if (guest != null) {
                customerName = guest.getName();
            }
        }

        CustomerReferral referral = new CustomerReferral(partnerId, guestId, customerName,
                customerStage.toUpperCase(), productIntroduced, dealAmount, referralDate);

        referralLog.add(referral);
        partner.incrementReferrals(dealAmount);

        if (undoController != null) {
            undoController.recordAction(
                "RECORD_REFERRAL",
                "Module 5: Strategic Partners",
                "Logged Referral for " + partner.getCompanyName() + ": " + productIntroduced + " ($" + String.format("%.2f", dealAmount) + ")",
                () -> {
                    for (int i = 1; i <= referralLog.getNumberOfEntries(); i++) {
                        if (referralLog.getEntry(i).equals(referral)) {
                            referralLog.remove(i);
                            break;
                        }
                    }
                    partner.setTotalReferralsCount(partner.getTotalReferralsCount() - 1);
                    partner.setTotalRevenueGenerated(partner.getTotalRevenueGenerated() - dealAmount);
                }
            );
        }

        return referral;
    }

    /**
     * Returns all recorded customer referrals.
     */
    public DoublyLinkedList<CustomerReferral> getAllReferrals() {
        return referralLog;
    }

    /**
     * Returns referrals for a specific partner.
     */
    public DoublyLinkedList<CustomerReferral> getReferralsByPartner(String partnerId) {
        DoublyLinkedList<CustomerReferral> list = new DoublyLinkedList<>();
        for (int i = 1; i <= referralLog.getNumberOfEntries(); i++) {
            CustomerReferral ref = referralLog.getEntry(i);
            if (ref != null && ref.getPartnerId().equalsIgnoreCase(partnerId)) {
                list.add(ref);
            }
        }
        return list;
    }

    /**
     * Recommends strategic partners according to the customer's active stage:
     * - PURCHASING -> Property Developers (Sime Darby Property, SP Setia, Sunway Property)
     * - RENOVATING -> Renovation & Electrical Contractors
     * - UPGRADING  -> Interior Design Firms & Renovation Contractors
     */
    public DoublyLinkedList<Partner> getRecommendedPartnersForStage(String stage) {
        DoublyLinkedList<Partner> recommended = new DoublyLinkedList<>();
        String normalizedStage = stage.toUpperCase().trim();

        for (int i = 1; i <= partnerRegistry.getNumberOfEntries(); i++) {
            Partner p = partnerRegistry.getEntry(i);
            if (p == null) continue;

            String cat = p.getPartnerCategory();
            if ("PURCHASING".equals(normalizedStage)) {
                if ("PROPERTY_DEVELOPER".equals(cat)) {
                    recommended.add(p);
                }
            } else if ("RENOVATING".equals(normalizedStage)) {
                if ("RENOVATION_CONTRACTOR".equals(cat) || "ELECTRICAL_CONTRACTOR".equals(cat)) {
                    recommended.add(p);
                }
            } else if ("UPGRADING".equals(normalizedStage)) {
                if ("INTERIOR_DESIGN_FIRM".equals(cat) || "RENOVATION_CONTRACTOR".equals(cat)) {
                    recommended.add(p);
                }
            }
        }
        return recommended;
    }

    /**
     * Management Report: Top Strategic Partners ranked by total referrals count using MergeSort (descending).
     */
    public DoublyLinkedList<Partner> getTopPartnersReportByReferrals() {
        return SortAlgorithms.mergeSort(partnerRegistry, (p1, p2) ->
                Integer.compare(p2.getTotalReferralsCount(), p1.getTotalReferralsCount()));
    }

    /**
     * Management Report: Strategic Partners ranked by total revenue generated using QuickSort (descending).
     */
    public DoublyLinkedList<Partner> getTopPartnersReportByRevenue() {
        return SortAlgorithms.quickSort(partnerRegistry, (p1, p2) ->
                Double.compare(p2.getTotalRevenueGenerated(), p1.getTotalRevenueGenerated()));
    }

    private Guest findGuestById(String guestId) {
        for (int i = 1; i <= guestRegistry.getNumberOfEntries(); i++) {
            Guest g = guestRegistry.getEntry(i);
            if (g != null && g.getGuestId().equalsIgnoreCase(guestId)) {
                return g;
            }
        }
        return null;
    }
}

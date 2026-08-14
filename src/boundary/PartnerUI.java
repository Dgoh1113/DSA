package boundary;

import adt.DoublyLinkedList;
import control.PartnerController;
import entity.CustomerReferral;
import entity.Partner;
import java.util.Scanner;

/**
 * Boundary: Strategic Partner & Referral UI (Module 5).
 * Console screen for managing strategic partners (Property Developers like Sime Darby Property,
 * SP Setia, Sunway Property, Contractors, and Interior Design Firms) and customer product referrals.
 * All System.out / Scanner interactions live here — no business logic.
 */
public class PartnerUI {

    private PartnerController controller;
    private Scanner scanner;

    public PartnerUI(PartnerController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void show() {
        boolean exitToMainMenu = false;
        do {
            utility.UIUtils.clearScreen();
            utility.UIUtils.printModule5Header();
            displayMenu();
            int choice = utility.UIUtils.safeReadInt(scanner);

            switch (choice) {
                case 1:
                    if (registerPartner()) {
                        exitToMainMenu = true;
                    } else {
                        exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    }
                    break;
                case 2:
                    viewAllPartners();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 3:
                    if (recordCustomerReferral()) {
                        exitToMainMenu = true;
                    } else {
                        exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    }
                    break;
                case 4:
                    recommendPartnersByStage();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 5:
                    topPartnersByReferralsReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 6:
                    topPartnersByRevenueReport();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 7:
                    viewReferralHistory();
                    exitToMainMenu = utility.UIUtils.promptPostOperationNavigation(scanner);
                    break;
                case 0:
                    exitToMainMenu = true;
                    break;
                default:
                    System.out.println(utility.UIUtils.RED + "Invalid option. Please try again." + utility.UIUtils.RESET);
                    utility.UIUtils.pressEnterToContinue(scanner);
            }
        } while (!exitToMainMenu);
    }

    private void displayMenu() {
        utility.UIUtils.printSectionHeader("PARTNER REGISTRATION & REFERRALS", utility.UIUtils.BLUE);
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "1." + utility.UIUtils.RESET + " Register Strategic Partner");
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "2." + utility.UIUtils.RESET + " View All Strategic Partners");
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "3." + utility.UIUtils.RESET + " Record Customer Product Referral");
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "4." + utility.UIUtils.RESET + " Recommend Partners by Customer Stage");

        utility.UIUtils.printSectionHeader("REVENUE & REFERRAL REPORTS", utility.UIUtils.BLUE);
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "5." + utility.UIUtils.RESET + " Report: Top Partners by Referrals (MergeSort O(n log n))");
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "6." + utility.UIUtils.RESET + " Report: Top Partners by Revenue (QuickSort O(n log n))");
        System.out.println("  " + utility.UIUtils.BLUE + utility.UIUtils.BOLD + "7." + utility.UIUtils.RESET + " View Customer Referral History Log");

        utility.UIUtils.printSectionHeader("NAVIGATION", utility.UIUtils.RED);
        System.out.println("  " + utility.UIUtils.RED + utility.UIUtils.BOLD + "0." + utility.UIUtils.RESET + " Back to Main Menu");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.print(utility.UIUtils.BOLD + "Enter your choice: " + utility.UIUtils.RESET);
    }

    private boolean registerPartner() {
        utility.UIUtils.printSubHeader("MODULE 5 > REGISTER STRATEGIC PARTNER", utility.UIUtils.BLUE);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        String name = "";
        String category = "";
        String contactPerson = "";
        String contactPhone = "";
        String email = "";
        String services = "";

        int step = 0;
        while (step >= 0 && step <= 5) {
            switch (step) {
                case 0: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 1/6 - Company Name          ", name, false);
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    name = res.getValue();
                    step++;
                    break;
                }
                case 1: {
                    System.out.println("  Partner Categories: PROPERTY_DEVELOPER | RENOVATION_CONTRACTOR | ELECTRICAL_CONTRACTOR | INTERIOR_DESIGN_FIRM");
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 2/6 - Category              ", category, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    category = res.getValue().toUpperCase();
                    step++;
                    break;
                }
                case 2: {
                    utility.StepResult res = utility.ValidationUtils.readValidNameStep(scanner, "Step 3/6 - Contact Person        ", contactPerson);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    contactPerson = res.getValue();
                    step++;
                    break;
                }
                case 3: {
                    utility.StepResult res = utility.ValidationUtils.readValidContactNoStep(scanner, "Step 4/6 - Contact Phone         ", contactPhone);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    contactPhone = res.getValue();
                    step++;
                    break;
                }
                case 4: {
                    utility.StepResult res = utility.ValidationUtils.readValidEmailStep(scanner, "Step 5/6 - Email Address         ", email);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    email = res.getValue();
                    step++;
                    break;
                }
                case 5: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 6/6 - Products / Services   ", services, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    services = res.getValue();
                    step++;
                    break;
                }
            }
        }

        if (step < 0) {
            System.out.println("\n  [!] Partner registration cancelled. No data saved.");
            return false;
        }

        Partner partner = controller.registerPartner(name, category, contactPerson, contactPhone, email, services);
        System.out.println("\nSUCCESS: Strategic Partner registered!");
        System.out.println("Partner ID  : " + partner.getPartnerId());
        System.out.println("Company     : " + partner.getCompanyName());
        System.out.println("Category    : " + partner.getPartnerCategory());
        return false;
    }

    private void viewAllPartners() {
        utility.UIUtils.printSubHeader("MODULE 5 > VIEW ALL STRATEGIC PARTNERS", utility.UIUtils.BLUE);
        DoublyLinkedList<Partner> partners = controller.getAllPartners();
        displayPartnerTable(partners);
    }

    private boolean recordCustomerReferral() {
        utility.UIUtils.printSubHeader("MODULE 5 > RECORD CUSTOMER REFERRAL", utility.UIUtils.BLUE);
        System.out.println(utility.UIUtils.YELLOW + "  [ TIP: Type 'b' to go BACK | Type '0' to QUIT TO MAIN MENU | Type 'cancel' to exit ]" + utility.UIUtils.RESET + "\n");

        String partnerId = "";
        String guestId = "";
        String customerName = "";
        String stage = "";
        String product = "";
        double amount = 0.0;
        String date = "";

        int step = 0;
        while (step >= 0 && step <= 6) {
            switch (step) {
                case 0: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 1/7 - Partner ID (e.g. P1000) ", partnerId, false);
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    partnerId = res.getValue().toUpperCase();
                    Partner partner = controller.findPartnerById(partnerId);
                    if (partner == null) {
                        System.out.println("  [!] ERROR: Partner ID not found. Try P1000 or a registered partner ID.");
                        break;
                    }
                    System.out.println("  Found Partner: " + partner.getCompanyName() + " (" + partner.getPartnerCategory() + ")");
                    step++;
                    break;
                }
                case 1: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 2/7 - Guest/Customer ID (Optional)", guestId, true);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    guestId = res.getValue();
                    step++;
                    break;
                }
                case 2: {
                    utility.StepResult res = utility.ValidationUtils.readValidNameStep(scanner, "Step 3/7 - Customer Name          ", customerName);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    customerName = res.getValue();
                    step++;
                    break;
                }
                case 3: {
                    System.out.println("  Customer Stages: PURCHASING | RENOVATING | UPGRADING");
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 4/7 - Customer Stage         ", stage, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    stage = res.getValue().toUpperCase();
                    step++;
                    break;
                }
                case 4: {
                    utility.StepResult res = utility.ValidationUtils.readValidStringStep(scanner, "Step 5/7 - Product / Service      ", product, false);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    product = res.getValue();
                    step++;
                    break;
                }
                case 5: {
                    utility.StepResult res = utility.ValidationUtils.readValidPositiveDoubleStep(scanner, "Step 6/7 - Deal Amount ($)        ", amount > 0 ? amount : null);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    amount = Double.parseDouble(res.getValue());
                    step++;
                    break;
                }
                case 6: {
                    utility.StepResult res = utility.ValidationUtils.readValidDateStep(scanner, "Step 7/7 - Referral Date (YYYY-MM-DD)", date);
                    if (res.isGoBack()) { step--; break; }
                    if (res.isQuitToMain()) return true;
                    if (res.isCancel()) return false;
                    date = res.getValue();
                    step++;
                    break;
                }
            }
        }

        if (step < 0) {
            System.out.println("\n  [!] Referral recording cancelled. No data saved.");
            return false;
        }

        CustomerReferral ref = controller.recordReferral(partnerId, guestId, customerName, stage, product, amount, date);
        if (ref != null) {
            System.out.println("\nSUCCESS: Customer referral logged!");
            System.out.println("Referral ID   : " + ref.getReferralId());
            System.out.println("Customer      : " + ref.getCustomerName());
            System.out.println("Stage         : " + ref.getCustomerStage());
            System.out.println("Product       : " + ref.getProductIntroduced());
            System.out.println("Deal Amount   : $" + String.format("%.2f", ref.getDealAmount()));
        } else {
            System.out.println("ERROR: Failed to record referral.");
        }
        return false;
    }

    private void recommendPartnersByStage() {
        utility.UIUtils.printSubHeader("MODULE 5 > RECOMMEND PARTNERS BY STAGE", utility.UIUtils.BLUE);
        System.out.println("Select Customer Stage:");
        System.out.println("  1. PURCHASING (Target: Property Developers)");
        System.out.println("  2. RENOVATING (Target: Renovation & Electrical Contractors)");
        System.out.println("  3. UPGRADING  (Target: Interior Design Firms & Renovation Contractors)");
        System.out.print("Choice: ");
        int choice = utility.UIUtils.safeReadInt(scanner);

        String stage;
        switch (choice) {
            case 1:  stage = "PURCHASING"; break;
            case 2:  stage = "RENOVATING"; break;
            case 3:  stage = "UPGRADING"; break;
            default: stage = "PURCHASING"; break;
        }

        System.out.println("\nStrategic Partners recommended for [" + stage + "] stage:");
        DoublyLinkedList<Partner> matches = controller.getRecommendedPartnersForStage(stage);
        displayPartnerTable(matches);
    }

    private void topPartnersByReferralsReport() {
        utility.UIUtils.printSubHeader("MODULE 5 > REPORT: TOP PARTNERS BY REFERRALS (MERGESORT)", utility.UIUtils.BLUE);
        DoublyLinkedList<Partner> sorted = controller.getTopPartnersReportByReferrals();
        displayPartnerTable(sorted);
    }

    private void topPartnersByRevenueReport() {
        utility.UIUtils.printSubHeader("MODULE 5 > REPORT: TOP PARTNERS BY REVENUE (QUICKSORT)", utility.UIUtils.BLUE);
        DoublyLinkedList<Partner> sorted = controller.getTopPartnersReportByRevenue();
        displayPartnerTable(sorted);
    }

    private void viewReferralHistory() {
        utility.UIUtils.printSubHeader("MODULE 5 > VIEW REFERRAL HISTORY LOG", utility.UIUtils.BLUE);
        DoublyLinkedList<CustomerReferral> refs = controller.getAllReferrals();
        if (refs.isEmpty()) {
            System.out.println("No referrals recorded yet.");
            return;
        }

        System.out.println(String.format("%-10s %-10s %-18s %-12s %-25s %-12s",
                "Ref ID", "Partner ID", "Customer Name", "Stage", "Product Introduced", "Deal ($)"));
        System.out.println("-----------------------------------------------------------------------------------------");
        for (int i = 1; i <= refs.getNumberOfEntries(); i++) {
            CustomerReferral r = refs.getEntry(i);
            System.out.println(String.format("%-10s %-10s %-18s %-12s %-25s $%-11.2f",
                    r.getReferralId(), r.getPartnerId(), truncate(r.getCustomerName(), 17),
                    r.getCustomerStage(), truncate(r.getProductIntroduced(), 24), r.getDealAmount()));
        }
    }

    private void displayPartnerTable(DoublyLinkedList<Partner> partners) {
        if (partners.isEmpty()) {
            System.out.println("No strategic partners found.");
            return;
        }

        System.out.println(String.format("%-8s %-24s %-22s %-10s %-14s",
                "ID", "Company Name", "Category", "Referrals", "Revenue ($)"));
        System.out.println("----------------------------------------------------------------------------------");
        for (int i = 1; i <= partners.getNumberOfEntries(); i++) {
            Partner p = partners.getEntry(i);
            System.out.println(String.format("%-8s %-24s %-22s %-10d $%-13.2f",
                    p.getPartnerId(), truncate(p.getCompanyName(), 23),
                    p.getPartnerCategory(), p.getTotalReferralsCount(), p.getTotalRevenueGenerated()));
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private double readDouble() {
        while (!scanner.hasNextDouble()) {
            System.out.print("Please enter a valid amount: ");
            scanner.next();
        }
        return scanner.nextDouble();
    }
}

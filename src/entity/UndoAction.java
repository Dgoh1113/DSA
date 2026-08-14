package entity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity: UndoAction — Represents a reversible system transaction.
 * Pure data blueprint containing action details and a Runnable reversal task.
 * Queued inside CustomQueue<UndoAction> for FIFO undo processing.
 */
public class UndoAction {

    private static int counter = 1000;

    private String actionId;      // PK e.g. UND-1001
    private String actionType;    // e.g. WALK_IN_REGISTRATION, CHECK_IN, REDEEM_POINTS
    private String moduleName;    // e.g. Module 1: Standard Booking
    private String description;   // Detailed description of the performed action
    private String timestamp;     // Time created
    private Runnable reversalTask;// Reversal logic executed when undone

    public UndoAction(String actionType, String moduleName, String description, Runnable reversalTask) {
        this.actionId = "UND-" + (counter++);
        this.actionType = actionType;
        this.moduleName = moduleName;
        this.description = description;
        this.reversalTask = reversalTask;
        this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // --- Getters & Setters ---

    public String getActionId() {
        return actionId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Runnable getReversalTask() {
        return reversalTask;
    }

    @Override
    public String toString() {
        return "[" + actionId + "] (" + timestamp + ") " + moduleName + " -> " + description;
    }
}

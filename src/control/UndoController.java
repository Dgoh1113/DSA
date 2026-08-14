package control;

import adt.DoublyLinkedList;
import adt.LinkedStack;
import entity.UndoAction;

/**
 * Controller: Undo Management System.
 * Manages the LinkedStack<UndoAction> (LIFO Stack ADT) for transaction reversals.
 *
 * Business Rules:
 * - When a state-altering action occurs in any module, an UndoAction is pushed onto undoStack in O(1).
 * - When processNextUndo() is invoked, the top UndoAction is popped from the LinkedStack in O(1)
 *   and its reversalTask is executed to restore system state (canonical LIFO Undo behavior).
 */
public class UndoController {

    private LinkedStack<UndoAction> undoStack;

    public UndoController(LinkedStack<UndoAction> undoStack) {
        this.undoStack = undoStack;
    }

    /**
     * Records a new reversible action onto the Undo Stack (LIFO - Last-In, First-Out).
     */
    public UndoAction recordAction(String actionType, String moduleName, String description, Runnable reversalTask) {
        UndoAction action = new UndoAction(actionType, moduleName, description, reversalTask);
        undoStack.push(action);
        return action;
    }

    /**
     * Processes and executes the most recent undo action from the top of the stack (LIFO).
     * Pops the action and runs its reversal task.
     *
     * @return The executed UndoAction, or null if stack is empty.
     */
    public UndoAction processNextUndo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        UndoAction action = undoStack.pop();
        if (action != null && action.getReversalTask() != null) {
            action.getReversalTask().run();
        }
        return action;
    }

    /**
     * Peeks at the top undo action on the stack without removing it.
     */
    public UndoAction peekNextUndo() {
        return undoStack.peek();
    }

    /**
     * Returns all pending undo actions currently stored in the LinkedStack (top first).
     */
    public DoublyLinkedList<UndoAction> getUndoQueueList() {
        return undoStack.toList();
    }

    /**
     * Returns the size of the undo stack.
     */
    public int getQueueSize() {
        return undoStack.size();
    }

    /**
     * Checks if the undo stack is empty.
     */
    public boolean isQueueEmpty() {
        return undoStack.isEmpty();
    }

    /**
     * Clears all stack actions.
     */
    public void clearUndoQueue() {
        undoStack.clear();
    }
}

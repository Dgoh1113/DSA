package adt;

/**
 * A Linked Stack implementation of StackInterface (LIFO - Last-In, First-Out).
 * Used by System Utility (Undo Control Center) for transaction reversals.
 * No java.util collections used.
 *
 * Operations:
 * - push: O(1)
 * - pop: O(1)
 * - peek: O(1)
 *
 * @param <T> The type of elements in the stack.
 */
public class LinkedStack<T> implements StackInterface<T> {

    private Node topNode;
    private int numberOfEntries;

    public LinkedStack() {
        topNode = null;
        numberOfEntries = 0;
    }

    @Override
    public void push(T newEntry) {
        Node newNode = new Node(newEntry, topNode);
        topNode = newNode;
        numberOfEntries++;
    }

    @Override
    public T pop() {
        T top = peek();
        if (topNode != null) {
            topNode = topNode.next;
            numberOfEntries--;
        }
        return top;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return topNode.data;
    }

    @Override
    public boolean isEmpty() {
        return topNode == null;
    }

    @Override
    public void clear() {
        topNode = null;
        numberOfEntries = 0;
    }

    @Override
    public int size() {
        return numberOfEntries;
    }

    /**
     * Converts the stack elements to a DoublyLinkedList (top of stack first).
     */
    public DoublyLinkedList<T> toList() {
        DoublyLinkedList<T> list = new DoublyLinkedList<>();
        Node current = topNode;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    private class Node {
        private T data;
        private Node next;

        private Node(T dataPortion, Node linkPortion) {
            data = dataPortion;
            next = linkPortion;
        }
    }
}

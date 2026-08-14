package adt;

/**
 * A custom Singly Linked Stack implementation of StackInterface (LIFO - Last-In, First-Out).
 * Used by Undo Management System for transaction reversals.
 * No java.util collections used.
 *
 * Operations:
 * - push: O(1)
 * - pop: O(1)
 * - peek: O(1)
 *
 * @param <T> The type of elements in the stack.
 */
public class CustomStack<T> implements StackInterface<T> {

    private Node topNode;
    private int numberOfEntries;

    public CustomStack() {
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
     * Converts the stack elements to a CustomLinkedList (top of stack first).
     */
    public CustomLinkedList<T> toList() {
        CustomLinkedList<T> list = new CustomLinkedList<>();
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

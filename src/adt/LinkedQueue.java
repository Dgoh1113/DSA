package adt;

/**
 * A Linked Queue implementation of QueueInterface (FIFO - First-In, First-Out).
 * Used for Standard Bookings waiting list (Module 1) — FIFO ordering.
 * No java.util collections used.
 *
 * @param <T> The type of elements in the queue.
 */
public class LinkedQueue<T> implements QueueInterface<T> {

    private Node firstNode;
    private Node lastNode;
    private int count;

    public LinkedQueue() {
        clear();
    }

    @Override
    public void enqueue(T newEntry) {
        Node newNode = new Node(newEntry);
        if (isEmpty()) {
            firstNode = newNode;
        } else {
            lastNode.next = newNode;
        }
        lastNode = newNode;
        count++;
    }

    @Override
    public T dequeue() {
        T front = getFront();
        if (front != null) {
            firstNode = firstNode.next;
            if (firstNode == null) {
                lastNode = null;
            }
            count--;
        }
        return front;
    }

    @Override
    public T getFront() {
        if (isEmpty()) {
            return null;
        }
        return firstNode.data;
    }

    @Override
    public boolean isEmpty() {
        return firstNode == null;
    }

    @Override
    public final void clear() {
        firstNode = null;
        lastNode = null;
        count = 0;
    }

    @Override
    public int size() {
        return count;
    }

    /**
     * Returns all queued items as a DoublyLinkedList without removing them.
     */
    public DoublyLinkedList<T> toList() {
        DoublyLinkedList<T> list = new DoublyLinkedList<>();
        Node current = firstNode;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    // Inner Node class
    private class Node {
        private T data;
        private Node next;

        private Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
}

package adt;

/**
 * A custom linked list implementation of ListInterface.
 * Acts as the Master Guest Registry (Module 5).
 * No java.util collections used.
 *
 * @param <T> The type of elements in the list.
 */
public class CustomLinkedList<T> implements ListInterface<T> {

    private Node firstNode;
    private int numberOfEntries;

    public CustomLinkedList() {
        clear();
    }

    @Override
    public void add(T newEntry) {
        Node newNode = new Node(newEntry);
        if (isEmpty()) {
            firstNode = newNode;
        } else {
            Node lastNode = getNodeAt(numberOfEntries);
            lastNode.next = newNode;
        }
        numberOfEntries++;
    }

    @Override
    public boolean add(int newPosition, T newEntry) {
        if (newPosition >= 1 && newPosition <= numberOfEntries + 1) {
            Node newNode = new Node(newEntry);
            if (newPosition == 1) {
                newNode.next = firstNode;
                firstNode = newNode;
            } else {
                Node nodeBefore = getNodeAt(newPosition - 1);
                newNode.next = nodeBefore.next;
                nodeBefore.next = newNode;
            }
            numberOfEntries++;
            return true;
        }
        return false;
    }

    @Override
    public T remove(int givenPosition) {
        T result = null;
        if (givenPosition >= 1 && givenPosition <= numberOfEntries) {
            if (givenPosition == 1) {
                result = firstNode.data;
                firstNode = firstNode.next;
            } else {
                Node nodeBefore = getNodeAt(givenPosition - 1);
                Node nodeToRemove = nodeBefore.next;
                result = nodeToRemove.data;
                nodeBefore.next = nodeToRemove.next;
            }
            numberOfEntries--;
        }
        return result;
    }

    @Override
    public final void clear() {
        firstNode = null;
        numberOfEntries = 0;
    }

    @Override
    public T replace(int givenPosition, T newEntry) {
        if (givenPosition >= 1 && givenPosition <= numberOfEntries) {
            Node desiredNode = getNodeAt(givenPosition);
            T originalData = desiredNode.data;
            desiredNode.data = newEntry;
            return originalData;
        }
        return null;
    }

    @Override
    public T getEntry(int givenPosition) {
        if (givenPosition >= 1 && givenPosition <= numberOfEntries) {
            return getNodeAt(givenPosition).data;
        }
        return null;
    }

    @Override
    public boolean contains(T anEntry) {
        Node currentNode = firstNode;
        while (currentNode != null) {
            if (anEntry.equals(currentNode.data)) {
                return true;
            }
            currentNode = currentNode.next;
        }
        return false;
    }

    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    private Node getNodeAt(int givenPosition) {
        Node currentNode = firstNode;
        for (int counter = 1; counter < givenPosition; counter++) {
            currentNode = currentNode.next;
        }
        return currentNode;
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

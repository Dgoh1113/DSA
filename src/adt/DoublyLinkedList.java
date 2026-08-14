package adt;

/**
 * A Doubly Linked List implementation of ListInterface.
 * Used for dynamic list management across modules.
 * No java.util collections used.
 *
 * Features prev/next pointers and lastNode for efficient operations
 * at both ends of the list.
 *
 * @param <T> The type of elements in the list.
 */
public class DoublyLinkedList<T> implements ListInterface<T> {

    private Node firstNode;
    private Node lastNode;
    private int numberOfEntries;

    public DoublyLinkedList() {
        clear();
    }

    @Override
    public void add(T newEntry) {
        Node newNode = new Node(newEntry);
        if (isEmpty()) {
            firstNode = newNode;
            lastNode = newNode;
        } else {
            newNode.prev = lastNode;
            lastNode.next = newNode;
            lastNode = newNode;
        }
        numberOfEntries++;
    }

    @Override
    public boolean add(int newPosition, T newEntry) {
        if (newPosition >= 1 && newPosition <= numberOfEntries + 1) {
            Node newNode = new Node(newEntry);
            if (newPosition == 1) {
                // Insert at front
                newNode.next = firstNode;
                if (firstNode != null) {
                    firstNode.prev = newNode;
                }
                firstNode = newNode;
                if (numberOfEntries == 0) {
                    lastNode = newNode;
                }
            } else if (newPosition == numberOfEntries + 1) {
                // Insert at end
                newNode.prev = lastNode;
                lastNode.next = newNode;
                lastNode = newNode;
            } else {
                // Insert in the middle
                Node nodeBefore = getNodeAt(newPosition - 1);
                Node nodeAfter = nodeBefore.next;
                newNode.prev = nodeBefore;
                newNode.next = nodeAfter;
                nodeBefore.next = newNode;
                if (nodeAfter != null) {
                    nodeAfter.prev = newNode;
                }
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
            Node nodeToRemove = getNodeAt(givenPosition);
            result = nodeToRemove.data;

            if (numberOfEntries == 1) {
                // Removing the only node
                firstNode = null;
                lastNode = null;
            } else if (givenPosition == 1) {
                // Removing first node
                firstNode = firstNode.next;
                firstNode.prev = null;
            } else if (givenPosition == numberOfEntries) {
                // Removing last node
                lastNode = lastNode.prev;
                lastNode.next = null;
            } else {
                // Removing from middle
                Node prevNode = nodeToRemove.prev;
                Node nextNode = nodeToRemove.next;
                prevNode.next = nextNode;
                nextNode.prev = prevNode;
            }
            numberOfEntries--;
        }
        return result;
    }

    @Override
    public final void clear() {
        firstNode = null;
        lastNode = null;
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

    /**
     * Gets the node at a given 1-indexed position.
     * Optimized: traverses from whichever end is closer.
     */
    private Node getNodeAt(int givenPosition) {
        if (givenPosition <= numberOfEntries / 2) {
            // Traverse from front
            Node currentNode = firstNode;
            for (int counter = 1; counter < givenPosition; counter++) {
                currentNode = currentNode.next;
            }
            return currentNode;
        } else {
            // Traverse from back
            Node currentNode = lastNode;
            for (int counter = numberOfEntries; counter > givenPosition; counter--) {
                currentNode = currentNode.prev;
            }
            return currentNode;
        }
    }

    // Inner Node class — Doubly linked with prev and next pointers
    private class Node {
        private T data;
        private Node prev;
        private Node next;

        private Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }
}

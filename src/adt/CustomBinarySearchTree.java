package adt;

/**
 * A custom Binary Search Tree (BST) implementation.
 * For Front-Desk fast searching of reservations by confirmationNo (Module 3).
 * No java.util collections used.
 *
 * Supports: insert, search, delete, in-order traversal, size.
 *
 * @param <T> The type of elements, must implement Comparable.
 */
public class CustomBinarySearchTree<T extends Comparable<T>> {

    private Node root;
    private int size;

    public CustomBinarySearchTree() {
        root = null;
        size = 0;
    }

    /**
     * Inserts a new element into the BST.
     */
    public void insert(T data) {
        root = insertHelper(root, data);
    }

    private Node insertHelper(Node node, T data) {
        if (node == null) {
            size++;
            return new Node(data);
        }
        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = insertHelper(node.left, data);
        } else if (cmp > 0) {
            node.right = insertHelper(node.right, data);
        } else {
            // Duplicate key: update the data in place
            node.data = data;
        }
        return node;
    }

    /**
     * Searches for an element matching the given criteria.
     * Average O(log n) time complexity.
     */
    public T search(T criteria) {
        return searchHelper(root, criteria);
    }

    private T searchHelper(Node node, T criteria) {
        if (node == null) {
            return null;
        }
        int cmp = criteria.compareTo(node.data);
        if (cmp == 0) {
            return node.data;
        } else if (cmp < 0) {
            return searchHelper(node.left, criteria);
        } else {
            return searchHelper(node.right, criteria);
        }
    }

    /**
     * Deletes an element from the BST.
     * Returns the deleted element, or null if not found.
     */
    public T delete(T data) {
        T[] result = (T[]) new Comparable[1]; // wrapper to capture deleted value
        root = deleteHelper(root, data, result);
        return result[0];
    }

    @SuppressWarnings("unchecked")
    private Node deleteHelper(Node node, T data, T[] result) {
        if (node == null) {
            return null;
        }
        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = deleteHelper(node.left, data, result);
        } else if (cmp > 0) {
            node.right = deleteHelper(node.right, data, result);
        } else {
            // Found the node to delete
            result[0] = node.data;
            size--;

            if (node.left == null && node.right == null) {
                return null; // Leaf node
            } else if (node.left == null) {
                return node.right; // Only right child
            } else if (node.right == null) {
                return node.left; // Only left child
            } else {
                // Two children: find in-order successor (smallest in right subtree)
                Node successor = findMin(node.right);
                node.data = successor.data;
                // Delete the successor from the right subtree
                T[] dummy = (T[]) new Comparable[1];
                size++; // compensate for the recursive delete decrement
                node.right = deleteHelper(node.right, successor.data, dummy);
            }
        }
        return node;
    }

    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    /**
     * Returns all elements in sorted order (in-order traversal).
     * Used by Module 3 for printing sorted booking logs.
     */
    public CustomLinkedList<T> inOrderTraversal() {
        CustomLinkedList<T> result = new CustomLinkedList<>();
        inOrderHelper(root, result);
        return result;
    }

    private void inOrderHelper(Node node, CustomLinkedList<T> result) {
        if (node != null) {
            inOrderHelper(node.left, result);
            result.add(node.data);
            inOrderHelper(node.right, result);
        }
    }

    public boolean contains(T data) {
        return search(data) != null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    // Inner Node class
    private class Node {
        private T data;
        private Node left;
        private Node right;

        private Node(T data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }
}

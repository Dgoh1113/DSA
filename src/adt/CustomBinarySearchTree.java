package adt;

/**
 * A custom Binary Search Tree (BST) implementation.
 * For Front-Desk fast searching of guests (Module 4).
 * No java.util collections used.
 *
 * @param <T> The type of elements, must implement Comparable.
 */
public class CustomBinarySearchTree<T extends Comparable<T>> {

    private Node root;

    public CustomBinarySearchTree() {
        root = null;
    }

    public void insert(T data) {
        root = insertHelper(root, data);
    }

    private Node insertHelper(Node node, T data) {
        if (node == null) {
            return new Node(data);
        }
        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = insertHelper(node.left, data);
        } else if (cmp > 0) {
            node.right = insertHelper(node.right, data);
        }
        // Duplicate: do nothing (or update)
        return node;
    }

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

    public boolean contains(T data) {
        return search(data) != null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
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

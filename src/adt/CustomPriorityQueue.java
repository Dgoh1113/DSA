package adt;

/**
 * A custom Priority Queue implementation using a Binary Max-Heap.
 * For VIP & Loyalty Tier priority room allocation (Module 2).
 * No java.util collections used.
 *
 * The highest-priority element (max) stays at the root — O(1) inspection.
 * Insertion (sift-up) and extraction (sift-down) operate in O(log n).
 *
 * @param <T> The type of elements, must implement Comparable.
 */
public class CustomPriorityQueue<T extends Comparable<T>> {

    private T[] heap;
    private int size;
    private static final int DEFAULT_CAPACITY = 25;

    public CustomPriorityQueue() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CustomPriorityQueue(int initialCapacity) {
        heap = (T[]) new Comparable[initialCapacity + 1]; // 1-based indexing
        size = 0;
    }

    /**
     * Inserts a new entry into the heap. Sifts up to maintain Max-Heap property.
     * O(log n) time complexity.
     */
    public void enqueue(T newEntry) {
        ensureCapacity();
        size++;
        int newIndex = size;
        // Sift up
        while (newIndex > 1 && newEntry.compareTo(heap[newIndex / 2]) > 0) {
            heap[newIndex] = heap[newIndex / 2];
            newIndex = newIndex / 2;
        }
        heap[newIndex] = newEntry;
    }

    /**
     * Removes and returns the highest-priority element (root of the Max-Heap).
     * O(log n) time complexity.
     */
    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T root = heap[1];
        heap[1] = heap[size];
        heap[size] = null;
        size--;
        if (size > 0) {
            reheapDown(1);
        }
        return root;
    }

    /**
     * Returns the highest-priority element without removing it.
     * O(1) time complexity.
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 1; i <= size; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    /**
     * Returns all heap items as a list (for display purposes).
     * Order is not guaranteed to be priority order.
     */
    public CustomLinkedList<T> toList() {
        CustomLinkedList<T> list = new CustomLinkedList<>();
        for (int i = 1; i <= size; i++) {
            list.add(heap[i]);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void ensureCapacity() {
        if (size >= heap.length - 1) {
            T[] oldHeap = heap;
            heap = (T[]) new Comparable[2 * oldHeap.length];
            System.arraycopy(oldHeap, 1, heap, 1, size);
        }
    }

    private void reheapDown(int rootIndex) {
        boolean done = false;
        T orphan = heap[rootIndex];
        int leftChildIndex = 2 * rootIndex;

        while (!done && leftChildIndex <= size) {
            int largerChildIndex = leftChildIndex;
            int rightChildIndex = leftChildIndex + 1;

            if (rightChildIndex <= size && heap[rightChildIndex].compareTo(heap[leftChildIndex]) > 0) {
                largerChildIndex = rightChildIndex;
            }

            if (orphan.compareTo(heap[largerChildIndex]) < 0) {
                heap[rootIndex] = heap[largerChildIndex];
                rootIndex = largerChildIndex;
                leftChildIndex = 2 * rootIndex;
            } else {
                done = true;
            }
        }
        heap[rootIndex] = orphan;
    }
}

package adt;

/**
 * Static utility class providing sorting algorithms for DoublyLinkedList.
 * Used by Module 4 (Loyalty and Rewards Service) for management reports.
 * No java.util collections used.
 *
 * Provides:
 * - MergeSort: O(n log n) stable sort — ideal for ranking reports
 * - QuickSort: O(n log n) average-case sort — alternative option
 *
 * Uses a custom SortComparator functional interface to avoid java.util.Comparator.
 */
public class SortAlgorithms {

    /**
     * Custom comparator interface to avoid using java.util.Comparator.
     */
    public interface SortComparator<T> {
        /**
         * Compares two elements.
         * Returns negative if a < b, zero if a == b, positive if a > b.
         */
        int compare(T a, T b);
    }

    // ========================================================================
    // MERGE SORT — Stable O(n log n) sort
    // ========================================================================

    /**
     * Sorts a DoublyLinkedList using MergeSort algorithm.
     * Returns a new sorted DoublyLinkedList; the original is not modified.
     *
     * @param list       The list to sort
     * @param comparator The comparison function
     * @param <T>        The element type
     * @return A new sorted DoublyLinkedList
     */
    @SuppressWarnings("unchecked")
    public static <T> DoublyLinkedList<T> mergeSort(DoublyLinkedList<T> list, SortComparator<T> comparator) {
        int n = list.getNumberOfEntries();
        if (n <= 1) {
            // Return a copy
            DoublyLinkedList<T> copy = new DoublyLinkedList<>();
            if (n == 1) {
                copy.add(list.getEntry(1));
            }
            return copy;
        }

        // Copy list elements into an array for efficient indexed access
        Object[] arr = new Object[n];
        for (int i = 0; i < n; i++) {
            arr[i] = list.getEntry(i + 1); // 1-indexed
        }

        // Perform merge sort on the array
        Object[] temp = new Object[n];
        mergeSortArray(arr, temp, 0, n - 1, (SortComparator<Object>) (SortComparator<?>) comparator);

        // Build result list from sorted array
        DoublyLinkedList<T> result = new DoublyLinkedList<>();
        for (int i = 0; i < n; i++) {
            result.add((T) arr[i]);
        }
        return result;
    }

    private static void mergeSortArray(Object[] arr, Object[] temp, int left, int right, SortComparator<Object> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortArray(arr, temp, left, mid, comparator);
            mergeSortArray(arr, temp, mid + 1, right, comparator);
            merge(arr, temp, left, mid, right, comparator);
        }
    }

    private static void merge(Object[] arr, Object[] temp, int left, int mid, int right, SortComparator<Object> comparator) {
        // Copy to temp
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i];
        }

        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            if (comparator.compare(temp[i], temp[j]) <= 0) {
                arr[k++] = temp[i++];
            } else {
                arr[k++] = temp[j++];
            }
        }

        while (i <= mid) {
            arr[k++] = temp[i++];
        }
        // Remaining elements from right half are already in place
    }

    // ========================================================================
    // QUICK SORT — Average O(n log n) sort
    // ========================================================================

    /**
     * Sorts a DoublyLinkedList using QuickSort algorithm.
     * Returns a new sorted DoublyLinkedList; the original is not modified.
     *
     * @param list       The list to sort
     * @param comparator The comparison function
     * @param <T>        The element type
     * @return A new sorted DoublyLinkedList
     */
    @SuppressWarnings("unchecked")
    public static <T> DoublyLinkedList<T> quickSort(DoublyLinkedList<T> list, SortComparator<T> comparator) {
        int n = list.getNumberOfEntries();
        if (n <= 1) {
            DoublyLinkedList<T> copy = new DoublyLinkedList<>();
            if (n == 1) {
                copy.add(list.getEntry(1));
            }
            return copy;
        }

        // Copy list elements into an array
        Object[] arr = new Object[n];
        for (int i = 0; i < n; i++) {
            arr[i] = list.getEntry(i + 1);
        }

        // Perform quick sort on the array
        quickSortArray(arr, 0, n - 1, (SortComparator<Object>) (SortComparator<?>) comparator);

        // Build result list from sorted array
        DoublyLinkedList<T> result = new DoublyLinkedList<>();
        for (int i = 0; i < n; i++) {
            result.add((T) arr[i]);
        }
        return result;
    }

    private static void quickSortArray(Object[] arr, int low, int high, SortComparator<Object> comparator) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high, comparator);
            quickSortArray(arr, low, pivotIndex - 1, comparator);
            quickSortArray(arr, pivotIndex + 1, high, comparator);
        }
    }

    private static int partition(Object[] arr, int low, int high, SortComparator<Object> comparator) {
        // Use median-of-three pivot selection for better average-case performance
        int mid = low + (high - low) / 2;
        if (comparator.compare(arr[mid], arr[low]) < 0) swap(arr, low, mid);
        if (comparator.compare(arr[high], arr[low]) < 0) swap(arr, low, high);
        if (comparator.compare(arr[mid], arr[high]) < 0) swap(arr, mid, high);
        Object pivot = arr[high];

        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(arr[j], pivot) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private static void swap(Object[] arr, int i, int j) {
        Object temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}

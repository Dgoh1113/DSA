package adt;

/**
 * An interface for the ADT Stack (LIFO - Last-In, First-Out).
 * Used for Undo Management operations.
 * No java.util collections allowed.
 *
 * @param <T> The type of elements in the stack.
 */
public interface StackInterface<T> {

    void push(T newEntry);

    T pop();

    T peek();

    boolean isEmpty();

    void clear();

    int size();
}

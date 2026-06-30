package adt;

/**
 * An interface for the ADT Queue.
 * No java.util collections allowed.
 *
 * @param <T> The type of elements in the queue.
 */
public interface QueueInterface<T> {

    void enqueue(T newEntry);

    T dequeue();

    T getFront();

    boolean isEmpty();

    void clear();

    int size();
}

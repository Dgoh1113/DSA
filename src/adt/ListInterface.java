package adt;

/**
 * An interface for the ADT List.
 * No java.util collections allowed.
 *
 * @param <T> The type of elements in the list.
 */
public interface ListInterface<T> {

    void add(T newEntry);

    boolean add(int newPosition, T newEntry);

    T remove(int givenPosition);

    void clear();

    T replace(int givenPosition, T newEntry);

    T getEntry(int givenPosition);

    boolean contains(T anEntry);

    int getNumberOfEntries();

    boolean isEmpty();
}

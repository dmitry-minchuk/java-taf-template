package configuration.core.ui.factory;

import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightWebElement;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Lazy implementation of List<PlaywrightWebElement> for Playwright elements.
 * Performs actual DOM search on each access to ensure elements are always current.
 * 
 * This class implements full List<PlaywrightWebElement> interface but recalculates elements on every access,
 * ensuring that the list always reflects the current DOM state.
 */
public class LazyElementsList implements List<PlaywrightWebElement> {
    
    private final Page page;
    private final PlaywrightWebElement parentElement;
    private final String selector;
    private final String baseName;
    
    public LazyElementsList(
            Page page,
            PlaywrightWebElement parentElement,
            String selector,
            String baseName) {
        this.page = page;
        this.parentElement = parentElement;
        this.selector = selector;
        this.baseName = baseName;
    }
    
    public LazyElementsList(
            Page page,
            PlaywrightWebElement parentElement,
            String selector) {
        this(page, parentElement, selector, null);
    }
    
    /**
     * Core method that performs actual DOM search every time it's called.
     * This ensures the list is always current with DOM state.
     */
    private List<PlaywrightWebElement> getCurrentElements() {
        return ListFactory.createElementsList(page, parentElement, selector, baseName);
    }
    
    // Core List methods that use getCurrentElements()
    
    @Override
    public int size() {
        return getCurrentElements().size();
    }
    
    @Override
    public boolean isEmpty() {
        return getCurrentElements().isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return getCurrentElements().contains(o);
    }
    
    @Override
    public Iterator<PlaywrightWebElement> iterator() {
        return getCurrentElements().iterator();
    }
    
    @Override
    public Object[] toArray() {
        return getCurrentElements().toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return getCurrentElements().toArray(a);
    }
    
    @Override
    public PlaywrightWebElement get(int index) {
        return getCurrentElements().get(index);
    }
    
    @Override
    public int indexOf(Object o) {
        return getCurrentElements().indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return getCurrentElements().lastIndexOf(o);
    }
    
    @Override
    public ListIterator<PlaywrightWebElement> listIterator() {
        return getCurrentElements().listIterator();
    }
    
    @Override
    public ListIterator<PlaywrightWebElement> listIterator(int index) {
        return getCurrentElements().listIterator(index);
    }
    
    @Override
    public List<PlaywrightWebElement> subList(int fromIndex, int toIndex) {
        return getCurrentElements().subList(fromIndex, toIndex);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return getCurrentElements().containsAll(c);
    }
    
    @Override
    public Stream<PlaywrightWebElement> stream() {
        return getCurrentElements().stream();
    }
    
    @Override
    public Stream<PlaywrightWebElement> parallelStream() {
        return getCurrentElements().parallelStream();
    }
    
    @Override
    public void forEach(Consumer<? super PlaywrightWebElement> action) {
        getCurrentElements().forEach(action);
    }
    
    // Modification methods - throw UnsupportedOperationException
    // Since we're dealing with DOM elements, modification operations don't make sense
    
    @Override
    public boolean add(PlaywrightWebElement element) {
        throw new UnsupportedOperationException(
            "Cannot add elements to lazy list. Elements are created from DOM.");
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(
            "Cannot remove elements from lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public boolean addAll(Collection<? extends PlaywrightWebElement> c) {
        throw new UnsupportedOperationException(
            "Cannot add elements to lazy list. Elements are created from DOM.");
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends PlaywrightWebElement> c) {
        throw new UnsupportedOperationException(
            "Cannot add elements to lazy list. Elements are created from DOM.");
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "Cannot remove elements from lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "Cannot retain elements in lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException(
            "Cannot clear lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public PlaywrightWebElement set(int index, PlaywrightWebElement element) {
        throw new UnsupportedOperationException(
            "Cannot set elements in lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public void add(int index, PlaywrightWebElement element) {
        throw new UnsupportedOperationException(
            "Cannot add elements to lazy list. Elements are created from DOM.");
    }
    
    @Override
    public PlaywrightWebElement remove(int index) {
        throw new UnsupportedOperationException(
            "Cannot remove elements from lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public boolean removeIf(Predicate<? super PlaywrightWebElement> filter) {
        throw new UnsupportedOperationException(
            "Cannot remove elements from lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public void replaceAll(UnaryOperator<PlaywrightWebElement> operator) {
        throw new UnsupportedOperationException(
            "Cannot replace elements in lazy list. Use element's own methods to modify DOM.");
    }
    
    @Override
    public void sort(Comparator<? super PlaywrightWebElement> c) {
        throw new UnsupportedOperationException(
            "Cannot sort lazy list. Elements reflect DOM order.");
    }
    
    // Override toString for better debugging
    @Override
    public String toString() {
        return String.format("LazyElementsList[selector='%s', size=%d]",
            selector, size());
    }
    
    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List)) return false;
        return getCurrentElements().equals(o);
    }
    
    @Override
    public int hashCode() {
        return getCurrentElements().hashCode();
    }
}
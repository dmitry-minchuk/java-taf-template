package configuration.core.ui.factory;

import com.microsoft.playwright.Page;
import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Lazy implementation of List<T> for Playwright components.
 * Performs actual DOM search on each access to ensure elements are always current.
 * 
 * This class implements full List<T> interface but recalculates elements on every access,
 * ensuring that the list always reflects the current DOM state.
 */
public class LazyPlaywrightComponentsList<T extends CoreComponent> implements List<T> {
    
    private final Class<T> componentClass;
    private final Page page;
    private final PlaywrightWebElement parentElement;
    private final String selector;
    private final String baseName;
    
    public LazyPlaywrightComponentsList(
            Class<T> componentClass,
            Page page,
            PlaywrightWebElement parentElement,
            String selector,
            String baseName) {
        this.componentClass = componentClass;
        this.page = page;
        this.parentElement = parentElement;
        this.selector = selector;
        this.baseName = baseName;
    }
    
    public LazyPlaywrightComponentsList(
            Class<T> componentClass,
            Page page,
            PlaywrightWebElement parentElement,
            String selector) {
        this(componentClass, page, parentElement, selector, null);
    }
    
    /**
     * Core method that performs actual DOM search every time it's called.
     * This ensures the list is always current with DOM state.
     */
    private List<T> getCurrentComponents() {
        return PlaywrightListFactory.createComponentsList(
            componentClass, page, parentElement, selector, baseName);
    }
    
    // Core List methods that use getCurrentComponents()
    
    @Override
    public int size() {
        return getCurrentComponents().size();
    }
    
    @Override
    public boolean isEmpty() {
        return getCurrentComponents().isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return getCurrentComponents().contains(o);
    }
    
    @Override
    public Iterator<T> iterator() {
        return getCurrentComponents().iterator();
    }
    
    @Override
    public Object[] toArray() {
        return getCurrentComponents().toArray();
    }
    
    @Override
    public <U> U[] toArray(U[] a) {
        return getCurrentComponents().toArray(a);
    }
    
    @Override
    public T get(int index) {
        return getCurrentComponents().get(index);
    }
    
    @Override
    public int indexOf(Object o) {
        return getCurrentComponents().indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return getCurrentComponents().lastIndexOf(o);
    }
    
    @Override
    public ListIterator<T> listIterator() {
        return getCurrentComponents().listIterator();
    }
    
    @Override
    public ListIterator<T> listIterator(int index) {
        return getCurrentComponents().listIterator(index);
    }
    
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return getCurrentComponents().subList(fromIndex, toIndex);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return getCurrentComponents().containsAll(c);
    }
    
    @Override
    public Stream<T> stream() {
        return getCurrentComponents().stream();
    }
    
    @Override
    public Stream<T> parallelStream() {
        return getCurrentComponents().parallelStream();
    }
    
    @Override
    public void forEach(Consumer<? super T> action) {
        getCurrentComponents().forEach(action);
    }
    
    // Modification methods - throw UnsupportedOperationException
    // Since we're dealing with DOM elements, modification operations don't make sense
    
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException(
            "Cannot add components to lazy list. Components are created from DOM elements.");
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(
            "Cannot remove components from lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException(
            "Cannot add components to lazy list. Components are created from DOM elements.");
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException(
            "Cannot add components to lazy list. Components are created from DOM elements.");
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "Cannot remove components from lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(
            "Cannot retain components in lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException(
            "Cannot clear lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException(
            "Cannot set components in lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException(
            "Cannot add components to lazy list. Components are created from DOM elements.");
    }
    
    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException(
            "Cannot remove components from lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException(
            "Cannot remove components from lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException(
            "Cannot replace components in lazy list. Use component's own methods to modify DOM.");
    }
    
    @Override
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException(
            "Cannot sort lazy list. Components reflect DOM order.");
    }
    
    // Override toString for better debugging
    @Override
    public String toString() {
        return String.format("LazyPlaywrightComponentsList[%s, selector='%s', size=%d]", 
            componentClass.getSimpleName(), selector, size());
    }
    
    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List)) return false;
        return getCurrentComponents().equals(o);
    }
    
    @Override
    public int hashCode() {
        return getCurrentComponents().hashCode();
    }
}
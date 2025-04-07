package configuration.core;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import domain.ui.BasePageComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmartElementFactory {

    public static void initElements(WebDriver driver, Object page) {
        Class<?> currentClass = page.getClass();
        List<Class<?>> classHierarchy = new ArrayList<>();

        while (currentClass != null && currentClass != Object.class) {
            classHierarchy.add(0, currentClass);
            currentClass = currentClass.getSuperclass();
        }

        for (Class<?> clazz : classHierarchy) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (!hasSupportedAnnotation(field)) continue;

                try {
                    if (field.getType().equals(SmartWebElement.class)) {
                        By locator = buildBy(field);
                        By root = (page instanceof BasePageComponent) ? ((BasePageComponent) page).getRootLocator() : null;
                        field.set(page, new SmartWebElement(driver, locator, Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)), null, root));
                    } else if (isListOfSmartElements(field)) {
                        By locator = buildBy(field);
                        By root = (page instanceof BasePageComponent) ? ((BasePageComponent) page).getRootLocator() : null;
                        field.set(page, createSmartElementList(driver, locator, root));
                    } else if (BasePageComponent.class.isAssignableFrom(field.getType())) {
                        By componentLocator = buildBy(field);
                        BasePageComponent component = (BasePageComponent) field.getType().getDeclaredConstructor().newInstance();
                        component.init(driver, componentLocator);
                        field.set(page, component);
                        // Recursively initialize elements within the component
                        initElements(driver, component);
                    } else if (isListOfPageComponents(field)) {
                        By listLocator = buildBy(field);
                        if (page instanceof BasePageComponent && ((BasePageComponent) page).getRootLocator() != null) {
                            field.set(page, createPageComponentList(driver, field, new RelativeBy(((BasePageComponent) page).getRootLocator(), listLocator)));
                        } else {
                            field.set(page, createPageComponentList(driver, field, listLocator));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Can't set field: " + field.getName(), e);
                }
            }
        }
    }

    private static boolean hasSupportedAnnotation(Field field) {
        return field.isAnnotationPresent(FindBy.class)
                || field.isAnnotationPresent(FindAll.class)
                || field.isAnnotationPresent(FindBys.class);
    }

    private static boolean isListOfSmartElements(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) return false;
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return false;
        ParameterizedType pt = (ParameterizedType) genericType;
        return pt.getActualTypeArguments()[0].equals(SmartWebElement.class);
    }

    private static boolean isListOfPageComponents(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) return false;
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return false;
        ParameterizedType pt = (ParameterizedType) genericType;
        Type typeArg = pt.getActualTypeArguments()[0];
        return typeArg instanceof Class && BasePageComponent.class.isAssignableFrom((Class<?>) typeArg);
    }

    private static List<SmartWebElement> createSmartElementList(WebDriver driver, By locator, By parentLocator) {
        List<WebElement> elements = driver.findElements(parentLocator != null ? new RelativeBy(parentLocator, locator) : locator);
        List<SmartWebElement> smartList = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            smartList.add(new SmartWebElement(driver, new IndexedBy(locator, i), Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)), null, parentLocator));
        }
        return smartList;
    }

    @SuppressWarnings("unchecked")
    private static <T extends BasePageComponent> List<T> createPageComponentList(WebDriver driver, Field field, By locator) throws Exception {
        List<WebElement> elements = driver.findElements(locator);
        List<T> componentList = new ArrayList<>();

        Type genericType = field.getGenericType();
        ParameterizedType pt = (ParameterizedType) genericType;
        Class<T> componentType = (Class<T>) pt.getActualTypeArguments()[0];

        for (int i = 0; i < elements.size(); i++) {
            T component = componentType.getDeclaredConstructor().newInstance();
            component.init(driver, new IndexedBy(locator, i));
            componentList.add(component);
            // Recursively initialize elements within the component
            initElements(driver, component);
        }

        return componentList;
    }

    private static By buildBy(Field field) {
        if (field.isAnnotationPresent(FindBy.class)) {
            return buildByFromFindBy(field.getAnnotation(FindBy.class));
        } else if (field.isAnnotationPresent(FindBys.class)) {
            FindBy[] findBys = field.getAnnotation(FindBys.class).value();
            return new ByChained(Arrays.stream(findBys).map(SmartElementFactory::buildByFromFindBy).toArray(By[]::new));
        } else if (field.isAnnotationPresent(FindAll.class)) {
            FindBy[] findBys = field.getAnnotation(FindAll.class).value();
            return new ByAll(Arrays.stream(findBys).map(SmartElementFactory::buildByFromFindBy).toArray(By[]::new));
        }
        throw new IllegalArgumentException("No valid locator annotations on field: " + field.getName());
    }

    private static By buildByFromFindBy(FindBy findBy) {
        if (!findBy.id().isEmpty()) return By.id(findBy.id());
        if (!findBy.name().isEmpty()) return By.name(findBy.name());
        if (!findBy.className().isEmpty()) return By.className(findBy.className());
        if (!findBy.css().isEmpty()) return By.cssSelector(findBy.css());
        if (!findBy.tagName().isEmpty()) return By.tagName(findBy.tagName());
        if (!findBy.linkText().isEmpty()) return By.linkText(findBy.linkText());
        if (!findBy.partialLinkText().isEmpty()) return By.partialLinkText(findBy.partialLinkText());
        if (!findBy.xpath().isEmpty()) return By.xpath(findBy.xpath());
        throw new IllegalArgumentException("No valid locator in @FindBy");
    }

    private static class RelativeBy extends By {
        private final By rootLocator;
        private final By childLocator;

        public RelativeBy(By rootLocator, By childLocator) {
            this.rootLocator = rootLocator;
            this.childLocator = childLocator;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            List<WebElement> rootElements = context.findElements(rootLocator);
            if (rootElements.isEmpty()) {
                return Collections.emptyList();
            }
            List<WebElement> foundElements = new ArrayList<>();
            for (WebElement rootElement : rootElements) {
                foundElements.addAll(rootElement.findElements(childLocator));
            }
            return foundElements;
        }

        @Override
        public WebElement findElement(SearchContext context) {
            List<WebElement> rootElements = context.findElements(rootLocator);
            if (rootElements.isEmpty()) {
                throw new NoSuchElementException("Cannot find root element(s) with locator: " + rootLocator);
            }
            return rootElements.get(0).findElement(childLocator);
        }

        @Override
        public String toString() {
            return rootLocator.toString() + " -> " + childLocator.toString();
        }
    }

    private static class IndexedBy extends By {
        private final By originalLocator;
        private final int index;

        public IndexedBy(By originalLocator, int index) {
            this.originalLocator = originalLocator;
            this.index = index;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            List<WebElement> elements = context.findElements(originalLocator);
            if (elements.size() > index) {
                return Collections.singletonList(elements.get(index));
            }
            return Collections.emptyList();
        }

        @Override
        public WebElement findElement(SearchContext context) {
            List<WebElement> elements = context.findElements(originalLocator);
            if (elements.size() > index) {
                return elements.get(index);
            }
            throw new NoSuchElementException("Cannot find element at index " + index + " for locator: " + originalLocator);
        }

        @Override
        public String toString() {
            return originalLocator.toString() + "[" + index + "]";
        }
    }
}

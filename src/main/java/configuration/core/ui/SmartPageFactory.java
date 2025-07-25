package configuration.core.ui;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import helpers.utils.WaitUtil;

// PLAYWRIGHT MIGRATION: Added WaitUtil import for conditional usage

public class SmartPageFactory {

    private final static int timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));

    public static void initElements(WebDriver driver, Object page) {
        Class<?> currentClass = page.getClass();
        List<Class<?>> classHierarchy = new ArrayList<>();

        while (currentClass != null && currentClass != Object.class) {
            classHierarchy.addFirst(currentClass);
            currentClass = currentClass.getSuperclass();
        }

        for (Class<?> clazz : classHierarchy) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (!hasSupportedAnnotation(field)) continue;

                try {
                    if (field.getType().equals(SmartWebElement.class)) {
                        By locator = buildBy(field);
                        By root = (page instanceof BasePageComponent) ? ((BasePageComponent) page).getRootLocatorBy() : null;
                        field.set(page, new SmartWebElement(driver, locator, root));
                    } else if (isListOfSmartElements(field)) {
                        field.set(page, createSmartElementListProxy(driver, field, page));
                    } else if (BasePageComponent.class.isAssignableFrom(field.getType())) {
                        By componentLocator = buildBy(field);
                        BasePageComponent component = (BasePageComponent) field.getType().getDeclaredConstructor().newInstance();
                        component.init(driver, componentLocator);
                        field.set(page, component);
                        initElements(driver, component);
                    } else if (isListOfPageComponents(field)) {
                        field.set(page, createPageComponentListProxy(driver, field, page));
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
        List<WebElement> elements = (parentLocator != null)
                ? WaitUtil.waitForElementsList(driver, parentLocator, timeoutInSeconds).stream().flatMap(parent -> WaitUtil.waitForElementsList(parent, locator, timeoutInSeconds).stream()).toList()
                : WaitUtil.waitForElementsList(driver, locator, timeoutInSeconds);

        List<SmartWebElement> smartList = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            assert parentLocator != null;
            smartList.add(new SmartWebElement(driver, new IndexedBy(locator, i), driver.findElements(parentLocator).getFirst(), null));
        }
        return smartList;
    }

    private static List<SmartWebElement> createSmartElementList(WebDriver driver, By locator) {
        return createSmartElementList(driver, locator, null);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BasePageComponent> List<T> createPageComponentList(WebDriver driver, Field field, By parentLocator, By listLocator) throws Exception {
        List<WebElement> elements = (parentLocator != null)
                ? WaitUtil.waitForElementsList(driver, parentLocator, timeoutInSeconds).stream().flatMap(parent -> WaitUtil.waitForElementsList(parent, listLocator, timeoutInSeconds).stream()).toList()
                : WaitUtil.waitForElementsList(driver, listLocator, timeoutInSeconds);

        List<T> componentList = new ArrayList<>();
        Type genericType = field.getGenericType();
        ParameterizedType pt = (ParameterizedType) genericType;
        Class<T> componentType = (Class<T>) pt.getActualTypeArguments()[0];

        for (int i = 0; i < elements.size(); i++) {
            T component = componentType.getDeclaredConstructor().newInstance();
            component.init(driver, new IndexedBy(listLocator, i));
            componentList.add(component);
            initElements(driver, component);
        }
        return componentList;
    }

    @SuppressWarnings("unchecked")
    private static List<SmartWebElement> createSmartElementListProxy(WebDriver driver, Field field, Object page) {
        return (List<SmartWebElement>) Proxy.newProxyInstance(
                field.getType().getClassLoader(),
                new Class[]{field.getType()},
                new ListProxyHandler(driver, field, page)
        );
    }

    @SuppressWarnings("unchecked")
    private static List<BasePageComponent> createPageComponentListProxy(WebDriver driver, Field field, Object page) {
        return (List<BasePageComponent>) Proxy.newProxyInstance(
                field.getType().getClassLoader(),
                new Class[]{field.getType()},
                new ListProxyHandler(driver, field, page)
        );
    }

    private static class ListProxyHandler implements InvocationHandler {
        private final WebDriver driver;
        private final Field field;
        private final Object page;

        public ListProxyHandler(WebDriver driver, Field field, Object page) {
            this.driver = driver;
            this.field = field;
            this.page = page;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            By locator = buildBy(field);
            By root = (page instanceof BasePageComponent) ? ((BasePageComponent) page).getRootLocatorBy() : null;

            if (isListOfSmartElements(field)) {
                List<SmartWebElement> list = createSmartElementList(driver, locator, root);
                return method.invoke(list, args);
            } else if (isListOfPageComponents(field)) {
                List<BasePageComponent> list = createPageComponentList(driver, field, root, locator);
                return method.invoke(list, args);
            }
            return null;
        }
    }

    private static By buildBy(Field field) {
        if (field.isAnnotationPresent(FindBy.class)) {
            return buildByFromFindBy(field.getAnnotation(FindBy.class));
        } else if (field.isAnnotationPresent(FindBys.class)) {
            FindBy[] findBys = field.getAnnotation(FindBys.class).value();
            return new ByChained(Arrays.stream(findBys).map(SmartPageFactory::buildByFromFindBy).toArray(By[]::new));
        } else if (field.isAnnotationPresent(FindAll.class)) {
            FindBy[] findBys = field.getAnnotation(FindAll.class).value();
            return new ByAll(Arrays.stream(findBys).map(SmartPageFactory::buildByFromFindBy).toArray(By[]::new));
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
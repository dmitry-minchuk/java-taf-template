package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Playwright-based replacement for SmartPageFactory
 * Handles @FindBy annotations and component initialization for Playwright
 */
public class PlaywrightPageFactory {
    
    protected final static Logger LOGGER = LogManager.getLogger(PlaywrightPageFactory.class);
    private final static int timeoutInSeconds = Integer.parseInt(
        ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
    );
    
    public static void initElements(Page page, Object pageObject) {
        Class<?> currentClass = pageObject.getClass();
        List<Class<?>> classHierarchy = new ArrayList<>();
        
        // Build class hierarchy for proper initialization order
        while (currentClass != null && currentClass != Object.class) {
            classHierarchy.addFirst(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        
        // Initialize fields in class hierarchy order
        for (Class<?> clazz : classHierarchy) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                
                if (!hasSupportedAnnotation(field)) continue;
                
                try {
                    if (field.getType().equals(PlaywrightWebElement.class)) {
                        // Initialize single PlaywrightWebElement
                        String selector = buildSelector(field);
                        PlaywrightWebElement element = createPlaywrightElement(page, selector, pageObject);
                        field.set(pageObject, element);
                        
                    } else if (isListOfPlaywrightElements(field)) {
                        // Initialize List<PlaywrightWebElement>
                        field.set(pageObject, createPlaywrightElementListProxy(page, field, pageObject));
                        
                    } else if (BasePageComponent.class.isAssignableFrom(field.getType())) {
                        // Initialize PageComponent
                        String componentSelector = buildSelector(field);
                        BasePageComponent component = (BasePageComponent) field.getType().getDeclaredConstructor().newInstance();
                        component.initPlaywright(page, componentSelector);
                        field.set(pageObject, component);
                        // Recursively initialize component's fields
                        initElements(page, component);
                        
                    } else if (isListOfPageComponents(field)) {
                        // Initialize List<BasePageComponent>
                        field.set(pageObject, createPageComponentListProxy(page, field, pageObject));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Can't set field: " + field.getName() + " in class: " + clazz.getName(), e);
                }
            }
        }
    }
    
    private static PlaywrightWebElement createPlaywrightElement(Page page, String selector, Object parent) {
        if (parent instanceof BasePageComponent) {
            BasePageComponent component = (BasePageComponent) parent;
            if (component.getRootSelector() != null) {
                // Create element relative to component root
                PlaywrightWebElement rootElement = new PlaywrightWebElement(page, component.getRootSelector());
                return new PlaywrightWebElement(rootElement, selector);
            }
        }
        return new PlaywrightWebElement(page, selector);
    }
    
    private static boolean hasSupportedAnnotation(Field field) {
        return field.isAnnotationPresent(FindBy.class)
                || field.isAnnotationPresent(FindAll.class)
                || field.isAnnotationPresent(FindBys.class);
    }
    
    private static boolean isListOfPlaywrightElements(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) return false;
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return false;
        ParameterizedType pt = (ParameterizedType) genericType;
        return pt.getActualTypeArguments()[0].equals(PlaywrightWebElement.class);
    }
    
    private static boolean isListOfPageComponents(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) return false;
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return false;
        ParameterizedType pt = (ParameterizedType) genericType;
        Type typeArg = pt.getActualTypeArguments()[0];
        return typeArg instanceof Class && BasePageComponent.class.isAssignableFrom((Class<?>) typeArg);
    }
    
    @SuppressWarnings("unchecked")
    private static List<PlaywrightWebElement> createPlaywrightElementListProxy(Page page, Field field, Object pageObject) {
        return (List<PlaywrightWebElement>) Proxy.newProxyInstance(
                field.getType().getClassLoader(),
                new Class[]{field.getType()},
                new PlaywrightListProxyHandler(page, field, pageObject)
        );
    }
    
    @SuppressWarnings("unchecked")
    private static List<BasePageComponent> createPageComponentListProxy(Page page, Field field, Object pageObject) {
        return (List<BasePageComponent>) Proxy.newProxyInstance(
                field.getType().getClassLoader(),
                new Class[]{field.getType()},
                new PlaywrightListProxyHandler(page, field, pageObject)
        );
    }
    
    private static class PlaywrightListProxyHandler implements InvocationHandler {
        private final Page page;
        private final Field field;
        private final Object pageObject;
        
        public PlaywrightListProxyHandler(Page page, Field field, Object pageObject) {
            this.page = page;
            this.field = field;
            this.pageObject = pageObject;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String selector = buildSelector(field);
            
            if (isListOfPlaywrightElements(field)) {
                List<PlaywrightWebElement> list = createPlaywrightElementList(page, selector, pageObject);
                return method.invoke(list, args);
            } else if (isListOfPageComponents(field)) {
                List<BasePageComponent> list = createPageComponentList(page, field, selector, pageObject);
                return method.invoke(list, args);
            }
            return null;
        }
    }
    
    private static List<PlaywrightWebElement> createPlaywrightElementList(Page page, String selector, Object parent) {
        List<PlaywrightWebElement> elements = new ArrayList<>();
        
        // Get count of elements matching the selector
        int count = page.locator(selector).count();
        
        // Create PlaywrightWebElement for each match using nth() selector
        for (int i = 0; i < count; i++) {
            String indexedSelector = selector + " >> nth=" + i;
            PlaywrightWebElement element = createPlaywrightElement(page, indexedSelector, parent);
            elements.add(element);
        }
        
        return elements;
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends BasePageComponent> List<T> createPageComponentList(Page page, Field field, String selector, Object parent) throws Exception {
        List<T> components = new ArrayList<>();
        Type genericType = field.getGenericType();
        ParameterizedType pt = (ParameterizedType) genericType;
        Class<T> componentType = (Class<T>) pt.getActualTypeArguments()[0];
        
        // Get count of elements matching the selector
        int count = page.locator(selector).count();
        
        // Create component for each match
        for (int i = 0; i < count; i++) {
            String indexedSelector = selector + " >> nth=" + i;
            T component = componentType.getDeclaredConstructor().newInstance();
            component.initPlaywright(page, indexedSelector);
            components.add(component);
            // Initialize component's fields
            initElements(page, component);
        }
        
        return components;
    }
    
    private static String buildSelector(Field field) {
        if (field.isAnnotationPresent(FindBy.class)) {
            return buildSelectorFromFindBy(field.getAnnotation(FindBy.class));
        } else if (field.isAnnotationPresent(FindBys.class)) {
            // For FindBys, chain the selectors (equivalent to ByChained)
            FindBy[] findBys = field.getAnnotation(FindBys.class).value();
            StringBuilder chained = new StringBuilder();
            for (int i = 0; i < findBys.length; i++) {
                if (i > 0) chained.append(" ");
                chained.append(buildSelectorFromFindBy(findBys[i]));
            }
            return chained.toString();
        } else if (field.isAnnotationPresent(FindAll.class)) {
            // For FindAll, use comma-separated selectors (equivalent to ByAll)
            FindBy[] findBys = field.getAnnotation(FindAll.class).value();
            return Arrays.stream(findBys)
                    .map(PlaywrightPageFactory::buildSelectorFromFindBy)
                    .reduce((a, b) -> a + ", " + b)
                    .orElseThrow(() -> new IllegalArgumentException("No valid locators in @FindAll"));
        }
        throw new IllegalArgumentException("No valid locator annotations on field: " + field.getName());
    }
    
    private static String buildSelectorFromFindBy(FindBy findBy) {
        if (!findBy.id().isEmpty()) return "#" + findBy.id();
        if (!findBy.name().isEmpty()) return "[name='" + findBy.name() + "']";
        if (!findBy.className().isEmpty()) return "." + findBy.className();
        if (!findBy.css().isEmpty()) return findBy.css();
        if (!findBy.tagName().isEmpty()) return findBy.tagName();
        if (!findBy.linkText().isEmpty()) return "text=" + findBy.linkText();
        if (!findBy.partialLinkText().isEmpty()) return "text*=" + findBy.partialLinkText();
        if (!findBy.xpath().isEmpty()) return "xpath=" + findBy.xpath();
        throw new IllegalArgumentException("No valid locator in @FindBy");
    }
}
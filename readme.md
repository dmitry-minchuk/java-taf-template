# UI Automation Framework Rules

* **Pages:**
    * Must have a public constructor accepting a `WebDriver` instance.
    * Elements and Components within Pages are initialized using `@FindBy` annotations within the Page class.

* **Components (`BasePageComponent` descendants):**
    * Must have a public no-argument constructor.
    * Instances are declared as fields and initialized **solely** using `@FindBy` annotations within a Page or another Component.
    * Direct instantiation of Components using `new` within Page or Component classes is **prohibited**.
    * The `init(WebDriver driver, By locator)` method (inherited from `BasePageComponent`) is called by `SmartElementFactory` for initialization.

* **Elements (`SmartWebElement`):**
    * Instances are initialized directly using `new SmartWebElement(driver, locator, root)` within Page or Component classes.
    * Declared as private fields annotated with `@FindBy`, `@FindBys`, or `@FindAll`.

* **Accessing Elements and Components:**
    * Access should ideally be through action-oriented methods (e.g., `clickLoginButton()`, `getHeaderText()`).
    * If direct access is needed, use well-defined methods (e.g., `getLogoElement()`). Avoid direct public field access.

* **Smart Element Factory:**
    * Responsible for initializing elements and components based on annotations.

* **No Automatic Getters:**
    * Getters are not automatically generated for all private fields. Create explicit methods as needed.
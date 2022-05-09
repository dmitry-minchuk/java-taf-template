package tests.junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import tests.junit.methods.SampleTest;

@Suite
@SelectClasses(SampleTest.class)

public class Suite1 {
}

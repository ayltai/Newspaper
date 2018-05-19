package com.github.ayltai.newspaper;

import com.github.ayltai.base.test.UnitTest;

import org.powermock.core.classloader.annotations.PowerMockIgnore;

@PowerMockIgnore({
    "javax.*",
    "com.davemorrissey.*",
    "com.github.piasy.*",
    "com.facebook.*",
    "com.sun.*",
    "org.mockito.*",
    "org.robolectric.*",
    "android.*"
})
public abstract class AppUnitTest extends UnitTest {
}

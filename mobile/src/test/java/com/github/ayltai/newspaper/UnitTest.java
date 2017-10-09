package com.github.ayltai.newspaper;

import android.os.Build;
import android.support.annotation.CallSuper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

@RunWith(RobolectricTestRunner.class)
@Config(
    sdk       = Build.VERSION_CODES.O,
    constants = BuildConfig.class,
    shadows   = {
        ShadowMultiDex.class
    }
)
@PowerMockIgnore({
    "javax.net.ssl.*",
    "org.mockito.*",
    "org.robolectric.*",
    "android.*"
})
public abstract class UnitTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @BeforeClass
    public static void setUpClass() {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setSingleSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @CallSuper
    @Before
    public void setUp() throws Exception {
    }

    @CallSuper
    @After
    public void tearDown() throws Exception {
    }
}

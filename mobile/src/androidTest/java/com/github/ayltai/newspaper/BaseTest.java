package com.github.ayltai.newspaper;

import java.io.File;

import android.Manifest;
import android.support.annotation.CallSuper;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.util.MoreTestUtils;

public abstract class BaseTest {
    @Rule
    public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @CallSuper
    @Before
    public void setUp() {
        final File file = new File("/sdcard/tmp/code-coverage/connected");
        if (!file.exists()) file.mkdirs();

        MoreTestUtils.sleep(MoreTestUtils.DURATION_MEDIUM);
    }

    @CallSuper
    @After
    public void tearDown() {
        MoreTestUtils.sleep(MoreTestUtils.DURATION_MEDIUM);
    }
}

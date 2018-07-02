package com.github.ayltai.newspaper.util;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

public final class ThreadPolicyFactoryTest extends UnitTest {
    @Test
    public void test() {
        Assert.assertEquals("[StrictMode.ThreadPolicy; mask=1376316]", ThreadPolicyFactory.newThreadPolicy().toString());
    }
}

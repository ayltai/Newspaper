package com.github.ayltai.newspaper.debug;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.util.ThreadPolicyFactory;

public final class ThreadPolicyFactoryTest extends UnitTest {
    @Test
    public void test() {
        Assert.assertEquals("[StrictMode.ThreadPolicy; mask=1376316]", ThreadPolicyFactory.newThreadPolicy().toString());
    }
}

package com.github.ayltai.newspaper.util;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

public final class VmPolicyFactoryTest extends UnitTest {
    @Test
    public void test() {
        Assert.assertEquals("[StrictMode.VmPolicy; mask=-2147371776]", VmPolicyFactory.newVmPolicy().toString());
    }
}

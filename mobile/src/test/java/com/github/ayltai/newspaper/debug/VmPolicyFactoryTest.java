package com.github.ayltai.newspaper.debug;

import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.util.VmPolicyFactory;

import junit.framework.Assert;

public final class VmPolicyFactoryTest extends UnitTest {
    @Test
    public void test() {
        Assert.assertEquals("[StrictMode.VmPolicy; mask=-2147109632]", VmPolicyFactory.newVmPolicy().toString());
    }
}

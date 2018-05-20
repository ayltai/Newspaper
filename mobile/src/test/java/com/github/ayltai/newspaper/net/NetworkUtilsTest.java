package com.github.ayltai.newspaper.net;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public final class NetworkUtilsTest extends UnitTest {
    @Test
    public void testShouldRetry() {
        Assert.assertFalse(NetworkUtils.shouldRetry(new IllegalArgumentException()));
        Assert.assertTrue(NetworkUtils.shouldRetry(new HttpException(Response.error(429, ResponseBody.create(null, "")))));
    }
}

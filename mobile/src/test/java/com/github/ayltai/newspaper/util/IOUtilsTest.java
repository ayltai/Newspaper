package com.github.ayltai.newspaper.util;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class IOUtilsTest extends UnitTest {
    @Test
    public void testReadString() {
        ByteArrayInputStream inputStream = null;

        try {
            inputStream = new ByteArrayInputStream("test".getBytes());

            Assert.assertEquals("test", IOUtils.readString(inputStream));
        } catch (final IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Test
    public void testCopy() {
        ByteArrayInputStream  inputStream  = null;
        ByteArrayOutputStream outputStream = null;

        try {
            inputStream  = new ByteArrayInputStream("test".getBytes());
            outputStream = new ByteArrayOutputStream();

            IOUtils.copy(inputStream, outputStream);

            Assert.assertEquals("test", new String(outputStream.toByteArray()));
        } catch (final IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
}

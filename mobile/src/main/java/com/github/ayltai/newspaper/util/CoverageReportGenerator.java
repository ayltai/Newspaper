package com.github.ayltai.newspaper.util;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public final class CoverageReportGenerator {
    private CoverageReportGenerator() {
    }

    public static void generate() {
        final File file = new File(new StringBuilder().append(Environment.getExternalStorageDirectory())
            .append(File.separator)
            .append("tmp")
            .append(File.separator)
            .append("code-coverage")
            .append(File.separator)
            .append("connected")
            .append(File.separator)
            .append("coverage.ec")
            .toString());

        try {
            file.createNewFile();

            Class.forName("com.vladium.emma.rt.RT").getMethod("dumpCoverageData", file.getClass(), boolean.class, boolean.class).invoke(null, file, false, false);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void verifyStoragePermissions(@NonNull final Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
    }
}

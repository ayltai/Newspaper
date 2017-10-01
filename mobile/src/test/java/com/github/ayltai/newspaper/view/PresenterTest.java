package com.github.ayltai.newspaper.view;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.PublishProcessor;
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
    "org.mockito.*",
    "org.robolectric.*",
    "android.*"
})
public abstract class PresenterTest<A extends Activity, P extends Presenter<V>, V extends Presenter.View> {
    protected final PublishProcessor<Boolean>    attachments = PublishProcessor.create();
    protected final PublishProcessor<Irrelevant> detachments = PublishProcessor.create();

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private P presenter;
    private V view;
    private A activity;

    @NonNull
    protected P getPresenter() {
        return this.presenter;
    }

    @NonNull
    protected V getView() {
        return this.view;
    }

    @NonNull
    protected abstract P createPresenter();

    @NonNull
    protected abstract V createView();

    @NonNull
    protected abstract A createActivity();

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
        this.activity  = this.createActivity();
        this.view      = this.createView();
        this.presenter = this.createPresenter();

        Mockito.when(this.view.getContext()).thenReturn(RuntimeEnvironment.application);
        Mockito.when(this.view.getActivity()).thenReturn(this.activity);
        Mockito.when(this.view.attachments()).thenReturn(this.attachments);
        Mockito.when(this.view.detachments()).thenReturn(this.detachments);

        this.disposables.add(this.view.attachments().subscribe(isFirstTimeAttachment -> this.presenter.onViewAttached(this.view, isFirstTimeAttachment)));
        this.disposables.add(this.view.detachments().subscribe(dummy -> this.presenter.onViewDetached()));
    }

    @CallSuper
    @After
    public void tearDown() throws Exception {
        this.detachments.onNext(Irrelevant.INSTANCE);

        this.disposables.dispose();
    }
}

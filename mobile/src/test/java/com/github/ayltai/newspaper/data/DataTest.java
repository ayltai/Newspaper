package com.github.ayltai.newspaper.data;

import android.content.Context;
import android.support.annotation.CallSuper;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.NewsItem;

import edu.emory.mathcs.backport.java.util.Collections;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.internal.RealmCore;

@PrepareForTest({
    Realm.class,
    RealmCore.class,
    RealmConfiguration.class,
    RealmQuery.class,
    RealmResults.class
})
public abstract class DataTest extends UnitTest {
    @SuppressWarnings("unchecked")
    @CallSuper
    @Override
    public void setUp() throws Exception {
        super.setUp();

        PowerMockito.mockStatic(RealmCore.class);
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.mockStatic(RealmConfiguration.class);
        PowerMockito.mockStatic(RealmQuery.class);
        PowerMockito.mockStatic(RealmResults.class);

        Realm.init(RuntimeEnvironment.application);

        PowerMockito.doNothing().when(RealmCore.class);
        RealmCore.loadLibrary(Mockito.any(Context.class));

        PowerMockito.whenNew(RealmConfiguration.class).withAnyArguments().thenReturn(PowerMockito.mock(RealmConfiguration.class));

        final Realm realm = PowerMockito.mock(Realm.class);
        PowerMockito.when(Realm.getDefaultInstance()).thenReturn(realm);
        PowerMockito.when(realm.createObject(NewsItem.class)).thenReturn(new NewsItem());

        final RealmQuery<NewsItem> query = PowerMockito.mock(RealmQuery.class);
        PowerMockito.when(realm.where(NewsItem.class)).thenReturn(query);
        PowerMockito.when(query.in(Mockito.anyString(), Mockito.any(String[].class))).thenReturn(query);
        PowerMockito.when(query.beginGroup()).thenReturn(query);
        PowerMockito.when(query.endGroup()).thenReturn(query);
        PowerMockito.when(query.and()).thenReturn(query);
        PowerMockito.when(query.or()).thenReturn(query);
        PowerMockito.when(query.contains(Mockito.anyString(), Mockito.anyString(), Mockito.any(Case.class))).thenReturn(query);
        PowerMockito.when(query.greaterThan(Mockito.anyString(), Mockito.anyInt())).thenReturn(query);
        PowerMockito.when(query.lessThan(Mockito.anyString(), Mockito.anyLong())).thenReturn(query);
        PowerMockito.when(query.equalTo(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(query);
        PowerMockito.when(query.equalTo(Mockito.anyString(), Mockito.anyString())).thenReturn(query);
        PowerMockito.when(query.equalTo(Mockito.anyString(), Mockito.anyInt())).thenReturn(query);
        PowerMockito.when(query.equalTo(Mockito.anyString(), Mockito.anyLong())).thenReturn(query);
        PowerMockito.when(query.notEqualTo(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(query);

        final RealmResults<NewsItem> results = PowerMockito.mock(RealmResults.class);
        PowerMockito.when(query.sort(Mockito.anyString(), Mockito.any(Sort.class))).thenReturn(query);
        PowerMockito.when(query.findAll()).thenReturn(results);
        PowerMockito.when(results.iterator()).thenReturn(Collections.emptyList().iterator());
        PowerMockito.when(results.deleteAllFromRealm()).thenReturn(true);

        final NewsItem item = new NewsItem();
        PowerMockito.when(results.first()).thenReturn(item);
    }
}

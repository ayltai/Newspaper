package com.github.ayltai.newspaper.app.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import io.realm.RealmList;

public final class FeaturedItem implements Item {
    private static final ThreadLocal<Random> RANDOM = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };

    private final List<Item> items;

    private int index;

    private FeaturedItem(@NonNull final List<Item> items) {
        this.items = items;
    }

    //region Properties

    @Nullable
    @Override
    public String getTitle() {
        return this.items.get(this.index).getTitle();
    }

    @Nullable
    @Override
    public String getDescription() {
        return this.items.get(this.index).getDescription();
    }

    @Override
    public boolean isFullDescription() {
        return this.items.get(this.index).isFullDescription();
    }

    @NonNull
    @Override
    public String getLink() {
        return this.items.get(this.index).getLink();
    }

    @Nullable
    @Override
    public Date getPublishDate() {
        return this.items.get(this.index).getPublishDate();
    }

    @NonNull
    @Override
    public String getSource() {
        return this.items.get(this.index).getSource();
    }

    @NonNull
    @Override
    public String getCategory() {
        return this.items.get(this.index).getCategory();
    }

    @NonNull
    @Override
    public RealmList<Image> getImages() {
        return this.items.get(this.index).getImages();
    }

    @Nullable
    @Override
    public Video getVideo() {
        return this.items.get(this.index).getVideo();
    }

    @Override
    public boolean isBookmarked() {
        return this.items.get(this.index).isBookmarked();
    }

    //endregion

    @NonNull
    public Item getItem() {
        return this.items.get(this.index);
    }

    public void next() {
        this.index = this.index == this.items.size() - 1 ? 0 : this.index + 1;
    }

    @Override
    public int compareTo(@NonNull final Item item) {
        return -1;
    }

    @Nullable
    public static Item create(@NonNull final List<Item> items) {
        final List<Item> originalItems = new ArrayList<>(items);
        final List<Item> featuredItems = new ArrayList<>();

        while (!originalItems.isEmpty()) {
            final Item item = originalItems.remove(FeaturedItem.RANDOM.get().nextInt(originalItems.size()));
            if (FeaturedItem.canBeFeatured(item)) featuredItems.add(item);
        }

        if (featuredItems.isEmpty()) return null;

        return new FeaturedItem(featuredItems);
    }

    private static boolean canBeFeatured(@NonNull final Item item) {
        return !TextUtils.isEmpty(item.getTitle()) && !item.getImages().isEmpty();
    }
}

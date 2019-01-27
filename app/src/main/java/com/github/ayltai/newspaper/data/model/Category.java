package com.github.ayltai.newspaper.data.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Category extends RealmObject implements Comparable<Category> {
    private static final Map<String, Integer> CATEGORIES = new HashMap<>();

    public static final List<String> DEFAULT_CATEGORIES = Arrays.asList("港聞", "兩岸", "國際", "經濟", "地產", "娛樂", "體育", "副刊", "教育", "即時港聞", "即時兩岸", "即時國際", "即時經濟", "即時地產", "即時娛樂", "即時體育", "即時副刊", "即時教育");

    static {
        int i = 0;

        Category.CATEGORIES.put("港聞", i++);
        Category.CATEGORIES.put("兩岸", i++);
        Category.CATEGORIES.put("國際", i++);
        Category.CATEGORIES.put("經濟", i++);
        Category.CATEGORIES.put("地產", i++);
        Category.CATEGORIES.put("娛樂", i++);
        Category.CATEGORIES.put("體育", i++);
        Category.CATEGORIES.put("副刊", i++);
        Category.CATEGORIES.put("教育", i);
    }

    @Getter
    private String url;

    @Getter
    private String name;

    @Nonnull
    @NonNull
    public String getDisplayName() {
        return this.name.length() > 2 ? this.name.substring(2) : this.name;
    }

    @Override
    public int compareTo(final Category category) {
        if (category == null) throw new NullPointerException();

        return Category.CATEGORIES.get(this.getDisplayName()) - Category.CATEGORIES.get(category.getDisplayName());
    }

    @Override
    public boolean equals(final Object category) {
        if (this == category) return true;
        if (category == null || getClass() != category.getClass()) return false;

        return this.getDisplayName().equals(((Category)category).getDisplayName());
    }

    @Override
    public int hashCode() {
        return this.getDisplayName().hashCode();
    }
}

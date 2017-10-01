package com.github.ayltai.newspaper.widget;

import android.widget.Filter;

public abstract class DelegatingFilter extends Filter {
    @Override
    public abstract FilterResults performFiltering(CharSequence charSequence);

    @Override
    public abstract void publishResults(CharSequence charSequence, FilterResults filterResults);
}

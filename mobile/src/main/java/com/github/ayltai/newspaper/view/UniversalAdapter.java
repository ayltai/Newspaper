package com.github.ayltai.newspaper.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.github.ayltai.newspaper.view.binding.Binder;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.view.binding.PartBinderFactory;
import com.github.ayltai.newspaper.view.binding.ViewBinderUtils;

import io.reactivex.disposables.Disposable;

public abstract class UniversalAdapter<M, V, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    public static final int          DEFAULT_ANIMATION_DURATION     = 600;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private final List<FullBinderFactory<M>>                     factories;
    private final List<Pair<PartBinderFactory<M, V>, Binder<V>>> binders = new ArrayList<>();

    private int          lastItemPosition;
    private int          animationDuration     = UniversalAdapter.DEFAULT_ANIMATION_DURATION;
    private Interpolator animationInterpolator = UniversalAdapter.DEFAULT_ANIMATION_INTERPOLATOR;

    protected UniversalAdapter(@NonNull final List<FullBinderFactory<M>> factories) {
        this.factories = factories;
    }

    public int getAnimationDuration() {
        return this.animationDuration;
    }

    public void setAnimationDuration(final int animationDuration) {
        this.animationDuration = animationDuration;
    }

    @Nullable
    public Interpolator getAnimationInterpolator() {
        return this.animationInterpolator;
    }

    public void setAnimationInterpolator(@Nullable final Interpolator animationInterpolator) {
        this.animationInterpolator = animationInterpolator;
    }

    @Override
    public int getItemCount() {
        return this.binders.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return this.binders.get(position).first.getPartType();
    }

    @NonNull
    protected Binder<V> getBinder(final int position) {
        return this.binders.get(position).second;
    }

    @NonNull
    protected Iterable<Animator> getItemAnimators(@NonNull final View view) {
        return Collections.emptyList();
    }

    public void clear() {
        for (final Pair<PartBinderFactory<M, V>, Binder<V>> binder : this.binders) {
            if (binder.second instanceof Disposable) {
                final Disposable disposable = (Disposable)binder.second;
                if (!disposable.isDisposed()) disposable.dispose();
            }
        }

        this.binders.clear();

        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final T holder, final int position) {
        final int adapterPosition = holder.getAdapterPosition();

        if (adapterPosition > this.lastItemPosition) {
            for (final Animator animator : this.getItemAnimators(holder.itemView)) {
                animator.setInterpolator(this.animationInterpolator);
                animator.setDuration(this.animationDuration).start();
            }

            this.lastItemPosition = adapterPosition;
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setScaleY(1f);
            holder.itemView.setScaleX(1f);
            holder.itemView.setTranslationY(0f);
            holder.itemView.setTranslationX(0f);
            holder.itemView.setRotation(0f);
            holder.itemView.setRotationY(0f);
            holder.itemView.setRotationX(0f);
            holder.itemView.setPivotY(holder.itemView.getMeasuredHeight() / 2);
            holder.itemView.setPivotX(holder.itemView.getMeasuredWidth() / 2);
            holder.itemView.animate().setInterpolator(null).setStartDelay(0L);
        }
    }

    /**
     * Calls this method instead of calling {@link #notifyDataSetChanged()} to update its associated {@link Binder}s.
     * @param items The items changed.
     */
    public void onDataSetChanged(@NonNull final Iterable<M> items) {
        this.binders.addAll(ViewBinderUtils.createViewBinders(items, this.factories));

        this.notifyDataSetChanged();
    }

    /**
     * Calls this method instead of calling {@link #notifyItemChanged(int)} to update its associated {@link Binder}s.
     * @param item The item changed.
     * @param position Position of the item that has changed.
     */
    public void onItemChanged(@Nullable final M item, final int position) {
        final Pair<PartBinderFactory<M, V>, Binder<V>> binder = this.binders.get(position);
        if (binder.second instanceof Disposable) {
            final Disposable disposable = (Disposable)binder.second;
            if (!disposable.isDisposed()) disposable.dispose();
        }

        this.binders.set(position, ViewBinderUtils.<M, V>createViewBinders(Collections.singletonList(item), this.factories).iterator().next());

        this.notifyItemChanged(position);
    }

    /**
     * Calls this method instead of calling {@link #notifyItemRangeChanged(int, int)} to update its associated {@link Binder}s.
     * @param items The items changed.
     * @param positionStart Position of the first item that has changed.
     */
    public void onItemRangeChanged(@NonNull final Collection<M> items, final int positionStart) {
        int i = 0;

        for (final Pair<PartBinderFactory<M, V>, Binder<V>> binder : ViewBinderUtils.<M, V>createViewBinders(items, this.factories)) {
            final int position = positionStart + i;

            final Pair<PartBinderFactory<M, V>, Binder<V>> b = this.binders.get(position);
            if (b.second instanceof Disposable) {
                final Disposable disposable = (Disposable)b.second;
                if (!disposable.isDisposed()) disposable.dispose();
            }

            this.binders.set(position, binder);

            i++;
        }

        this.notifyItemRangeChanged(positionStart, items.size());
    }

    /**
     * Calls this method instead of calling {@link #notifyItemInserted(int)} to update its associated {@link Binder}s.
     * @param item The item inserted.
     * @param position Position of the newly inserted item in the data set.
     */
    public void onItemInserted(@Nullable final M item, final int position) {
        this.binders.add(position, ViewBinderUtils.<M, V>createViewBinders(Collections.singletonList(item), this.factories).iterator().next());

        this.notifyItemInserted(position);
    }

    /**
     * Calls this method instead of calling {@link #notifyItemMoved(int, int)} to update its associated {@link Binder}s.
     * @param fromPosition Previous position of the item.
     * @param toPosition New position of the item.
     */
    public void onItemMoved(final int fromPosition, final int toPosition) {
        Collections.swap(this.binders, fromPosition, toPosition);

        this.notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * Calls this method instead of calling {@link #notifyItemRangeInserted(int, int)} to update its associated {@link Binder}s.
     * @param items The items inserted.
     * @param positionStart Position of the first item that was inserted.
     */
    public void onItemRangeInserted(@NonNull final Collection<M> items, final int positionStart) {
        this.binders.addAll(positionStart, ViewBinderUtils.createViewBinders(items, this.factories));

        this.notifyItemRangeInserted(positionStart, items.size());
    }

    /**
     * Calls this method instead of calling {@link #notifyItemRemoved(int)} to update its associated {@link Binder}s.
     * @param position Position of the item that has now been removed.
     */
    public void onItemRemoved(final int position) {
        final Pair<PartBinderFactory<M, V>, Binder<V>> binder = this.binders.get(position);
        if (binder.second instanceof Disposable) {
            final Disposable disposable = (Disposable)binder.second;
            if (!disposable.isDisposed()) disposable.dispose();
        }

        this.binders.remove(position);

        this.notifyItemRemoved(position);
    }

    /**
     * Calls this method instead of calling {@link #notifyItemRangeRemoved(int, int)} to update its associated {@link Binder}s.
     * @param positionStart Previous position of the first item that was removed.
     * @param itemCount Number of items removed from the data set.
     */
    public void onItemRangeRemoved(final int positionStart, final int itemCount) {
        final List<Pair<PartBinderFactory<M, V>, Binder<V>>> subList = this.binders.subList(positionStart, positionStart + itemCount);
        for (final Pair<PartBinderFactory<M, V>, Binder<V>> binder : subList) {
            if (binder.second instanceof Disposable) {
                final Disposable disposable = (Disposable)binder.second;
                if (!disposable.isDisposed()) disposable.dispose();
            }
        }

        subList.clear();

        this.notifyItemRangeRemoved(positionStart, itemCount);
    }
}

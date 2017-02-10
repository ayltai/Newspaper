package com.github.ayltai.newspaper.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.ContextUtils;

final class DummyViewHolder extends RecyclerView.ViewHolder {
    DummyViewHolder(@NonNull final View itemView) {
        super(itemView);

        final int textColor = ContextUtils.getColor(itemView.getContext(), R.attr.textColorHint);

        final TextView title = (TextView)itemView.findViewById(R.id.title);
        title.setTextColor(textColor);
        title.setText("██████████");

        final TextView description = (TextView)itemView.findViewById(R.id.description);
        description.setTextColor(textColor);
        description.setText("████████████████████████████████████████████████████████████████████████████████████████████████████");

        final TextView source = (TextView)itemView.findViewById(R.id.source);
        source.setTextColor(textColor);
        source.setText("█████");

        final TextView publishDate = (TextView)itemView.findViewById(R.id.publishDate);
        publishDate.setTextColor(textColor);
        publishDate.setText("███████");
    }
}

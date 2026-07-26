package com.trishop.app.presentation.cart.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.IntConsumer;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final ColorDrawable background = new ColorDrawable(Color.parseColor("#D53030"));
    private final IntConsumer onSwiped;

    public SwipeToDeleteCallback(@NonNull IntConsumer onSwiped) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.onSwiped = onSwiped;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        onSwiped.accept(viewHolder.getBindingAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        background.setBounds(
                itemView.getLeft(),
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom());
        background.draw(c);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}

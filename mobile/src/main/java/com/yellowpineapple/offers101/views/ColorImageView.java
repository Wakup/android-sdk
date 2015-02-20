package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yellowpineapple.offers101.R;

/**
 * Created by agutierrez on 05/02/15.
 */


public class ColorImageView extends ImageView {


    /* Constructors */
    public ColorImageView(Context context) {
        this(context, null);
    }

    public ColorImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Extract styleable attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorImageView);
            if (a.hasValue(R.styleable.ColorImageView_imageColor)) {
                int color = a.getColor(R.styleable.ColorImageView_imageColor, getResources().getColor(R.color.white));
                if (!isInEditMode()) {
                    setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                } else {
                    setColorFilter(color);
                }
            }
            a.recycle();
        }
    }

}
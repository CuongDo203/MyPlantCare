package com.example.myplantcare.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.example.myplantcare.R;

public class ProfileMenuHelper {
    public static void setupItem(View parent, int includeId, int iconResId, String title) {
        View item = parent.findViewById(includeId);
        if (item != null) {
            ImageView icon = item.findViewById(R.id.ivIcon);
            TextView text = item.findViewById(R.id.tvTitle);
            if (icon != null) icon.setImageResource(iconResId);
            if (text != null) text.setText(title);
        }
    }
}



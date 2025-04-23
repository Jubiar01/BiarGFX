package com.example.facebooklogin.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.example.facebooklogin.databinding.DialogProgressBinding;

public class CustomProgressDialog {
    private Dialog dialog;
    private DialogProgressBinding binding;
    private Context context;

    public CustomProgressDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        // Use ViewBinding for the dialog
        binding = DialogProgressBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        // Set transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set dialog width and gravity
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
    }

    public void show(String status) {
        binding.statusText.setText(status);
        binding.statusDescription.setText("Please wait while we process your request");
        dialog.show();
    }

    public void show(String status, String description) {
        binding.statusText.setText(status);
        binding.statusDescription.setText(description);
        dialog.show();
    }

    public void updateStatus(String status) {
        if (dialog != null && dialog.isShowing()) {
            binding.statusText.setText(status);
        }
    }

    public void updateStatus(String status, String description) {
        if (dialog != null && dialog.isShowing()) {
            binding.statusText.setText(status);
            binding.statusDescription.setText(description);
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
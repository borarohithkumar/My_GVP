package com.example.mygvp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.LostItemViewHolder> {

    private List<LostItem> itemList;
    private OnItemEditListener editListener;

    public interface OnItemEditListener {
        void onEditClick(LostItem item);
    }

    public LostItemAdapter(List<LostItem> itemList, OnItemEditListener editListener) {
        this.itemList = itemList;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public LostItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_found, parent, false);
        return new LostItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LostItemViewHolder holder, int position) {
        LostItem item = itemList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemStatus.setText(item.getStatus());
        holder.tvUploaderName.setText("by " + item.getUploaderName() + " (" + item.getUploaderRoll() + ")");

        CharSequence timeAgo = android.text.format.DateUtils.getRelativeTimeSpanString(
                item.getTimestamp(), System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS);
        holder.tvTimestamp.setText("• " + timeAgo);

        holder.tvItemMessage.setText(item.getMessage());

        Glide.with(context).load(item.getImageUrl()).placeholder(android.R.drawable.ic_menu_camera).into(holder.ivItemPhoto);

        holder.ivItemPhoto.setOnClickListener(v -> showImageModal(context, item.getImageUrl()));

        SharedPreferences prefs = context.getSharedPreferences("MyGVP_UserPrefs", Context.MODE_PRIVATE);
        String myRoll = prefs.getString("LOGGED_IN_ROLL_NO", "");

        if (item.getStatus().equals("RESOLVED") || item.getStatus().equals("CLAIMED")) {
            holder.tvItemStatus.setTextColor(Color.parseColor("#1B5E20"));
            holder.tvItemStatus.setBackgroundColor(Color.parseColor("#C8E6C9"));
            holder.btnAction.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE); // Hide edit if resolved
        } else {
            if(item.getStatus().equals("LOST")) {
                holder.tvItemStatus.setTextColor(Color.parseColor("#B71C1C"));
                holder.tvItemStatus.setBackgroundColor(Color.parseColor("#FFCDD2"));
            } else {
                holder.tvItemStatus.setTextColor(Color.parseColor("#E65100"));
                holder.tvItemStatus.setBackgroundColor(Color.parseColor("#FFE0B2"));
            }
            holder.btnAction.setVisibility(View.VISIBLE);

            // OP?
            if (myRoll.equals(item.getUploaderRoll())) {
                holder.btnEdit.setVisibility(View.VISIBLE); // Show Edit!
                holder.btnAction.setText("Resolve");

                holder.btnEdit.setOnClickListener(v -> editListener.onEditClick(item));

                holder.btnAction.setOnClickListener(v -> {
                    String newStatus = item.getStatus().equals("LOST") ? "RESOLVED" : "CLAIMED";
                    FirebaseDatabase.getInstance().getReference("LostAndFound").child(item.getId())
                            .child("status").setValue(newStatus)
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Resolved!", Toast.LENGTH_SHORT).show());
                });
            } else {
                holder.btnEdit.setVisibility(View.GONE); // Hide Edit!
                holder.btnAction.setText("Contact");
                holder.btnAction.setOnClickListener(v -> showContactDialog(context, item.getUploaderRoll(), item.getTitle()));
            }
        }
    }

    private void showImageModal(Context context, String imageUrl) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_image_zoom);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView ivZoomedImage = dialog.findViewById(R.id.ivZoomedImage);
        ImageButton btnCloseZoom = dialog.findViewById(R.id.btnCloseZoom);

        // to natively round the corners of the image using Glide!
        Glide.with(context)
                .load(imageUrl)
                .transform(new com.bumptech.glide.load.resource.bitmap.RoundedCorners(48)) // 48 is the curve amount
                .into(ivZoomedImage);

        btnCloseZoom.setOnClickListener(v -> dialog.dismiss());
        ivZoomedImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showContactDialog(Context context, String uploaderRoll, String itemName) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_contact_student);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView tvContactName = dialog.findViewById(R.id.tvContactName);
        TextView tvContactEmail = dialog.findViewById(R.id.tvContactEmail);
        Button btnSendEmail = dialog.findViewById(R.id.btnSendEmail);

        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students").child(uploaderRoll);
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbName = snapshot.child("name").getValue(String.class);
                    String dbEmail = snapshot.child("email").getValue(String.class);
                    if (dbName == null || dbName.isEmpty()) dbName = "Student";

                    tvContactName.setText(dbName + " (" + uploaderRoll + ")");
                    tvContactEmail.setText(dbEmail != null ? dbEmail : "No email found");

                    String finalEmail = dbEmail;
                    btnSendEmail.setOnClickListener(v -> {
                        if (finalEmail != null) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse("mailto:"));
                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{finalEmail});
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MYGVPT App: Regarding your post about '" + itemName + "'");
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email..."));
                            dialog.dismiss();
                        } else {
                            Toast.makeText(context, "User has no email registered.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    tvContactName.setText("User not found in database.");
                    btnSendEmail.setEnabled(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class LostItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemTitle, tvItemStatus, tvUploaderName, tvItemMessage, tvTimestamp;
        ImageView ivItemPhoto;
        Button btnAction;
        ImageButton btnEdit;

        public LostItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
            tvUploaderName = itemView.findViewById(R.id.tvUploaderName);
            tvItemMessage = itemView.findViewById(R.id.tvItemMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivItemPhoto = itemView.findViewById(R.id.ivItemPhoto);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
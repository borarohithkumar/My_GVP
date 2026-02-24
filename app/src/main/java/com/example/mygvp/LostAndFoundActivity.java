package com.example.mygvp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LostAndFoundActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LostItemAdapter adapter;

    private List<LostItem> masterList;
    private List<LostItem> displayList;

    private Uri selectedImageUri = null;
    private ImageView dialogImageViewPreview;
    private DatabaseReference databaseReference;
    private final String IMGBB_API_KEY = "c1f3c10ea2a0b2c488bc79f65debf52f";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (dialogImageViewPreview != null) {
                        Glide.with(this).load(selectedImageUri).centerCrop().into(dialogImageViewPreview);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_found);

        databaseReference = FirebaseDatabase.getInstance().getReference("LostAndFound");

        recyclerView = findViewById(R.id.recyclerViewLostFound);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        masterList = new ArrayList<>();
        displayList = new ArrayList<>();

        // pass the listener into the adapter for the Edit button!
        adapter = new LostItemAdapter(displayList, item -> showEditItemDialog(item));
        recyclerView.setAdapter(adapter);

        com.google.android.material.button.MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroupFilter);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnFilterAll) filterFeed("ALL");
                else if (checkedId == R.id.btnFilterLost) filterFeed("LOST");
                else if (checkedId == R.id.btnFilterFound) filterFeed("FOUND");
            }
        });

        com.google.android.material.floatingactionbutton.FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        loadFeedFromFirebase();
    }

    private void loadFeedFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                masterList.clear();
                long currentTime = System.currentTimeMillis();
                long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LostItem item = dataSnapshot.getValue(LostItem.class);
                    if (item != null && item.getId() != null) {
                        boolean isResolved = item.getStatus().equals("RESOLVED") || item.getStatus().equals("CLAIMED");
                        boolean isOlderThan3Days = (currentTime - item.getTimestamp()) > threeDaysInMillis;

                        if (isResolved && isOlderThan3Days) continue;
                        masterList.add(item);
                    }
                }
                com.google.android.material.button.MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroupFilter);
                if (toggleGroup.getCheckedButtonId() == R.id.btnFilterAll) filterFeed("ALL");
                else if (toggleGroup.getCheckedButtonId() == R.id.btnFilterLost) filterFeed("LOST");
                else if (toggleGroup.getCheckedButtonId() == R.id.btnFilterFound) filterFeed("FOUND");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LostAndFoundActivity.this, "Failed to load feed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterFeed(String filterType) {
        displayList.clear();
        for (LostItem item : masterList) {
            if (filterType.equals("ALL")) {
                displayList.add(item);
            } else if (filterType.equals("LOST") && (item.getStatus().equals("LOST") || item.getStatus().equals("RESOLVED"))) {
                displayList.add(item);
            } else if (filterType.equals("FOUND") && (item.getStatus().equals("FOUND") || item.getStatus().equals("CLAIMED"))) {
                displayList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddItemDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_item);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        selectedImageUri = null;

        dialogImageViewPreview = dialog.findViewById(R.id.ivSelectedImage);
        Button btnSelectImage = dialog.findViewById(R.id.btnSelectImage);
        Button btnSubmitItem = dialog.findViewById(R.id.btnSubmitItem);
        EditText etItemTitle = dialog.findViewById(R.id.etItemTitle);
        EditText etItemMessage = dialog.findViewById(R.id.etItemMessage);
        RadioButton rbLost = dialog.findViewById(R.id.rbLost);

        btnSelectImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSubmitItem.setOnClickListener(view -> {
            String title = etItemTitle.getText().toString().trim();
            String message = etItemMessage.getText().toString().trim();
            String status = rbLost.isChecked() ? "LOST" : "FOUND";

            if (title.isEmpty() || message.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image!", Toast.LENGTH_SHORT).show();
                return;
            }

            android.content.SharedPreferences prefs = getSharedPreferences("MyGVP_UserPrefs", MODE_PRIVATE);
            String uploaderName = prefs.getString("LOGGED_IN_NAME", "Student");
            String uploaderRoll = prefs.getString("LOGGED_IN_ROLL_NO", "Unknown Roll");

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading to ImgBB...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Thread(() -> {
                String uploadedImageUrl = uploadImageToImgBB(selectedImageUri);
                runOnUiThread(() -> {
                    if (uploadedImageUrl != null) {
                        progressDialog.setMessage("Saving to Database...");
                        saveToFirebase(null, title, status, uploaderName, uploaderRoll, message, uploadedImageUrl, dialog, progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed. Try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });

        dialog.show();
    }

    private void showEditItemDialog(LostItem item) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_item); // Reusing the same XML!
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        selectedImageUri = null; // Reset this so we know if they picked a NEW image

        // Find views
        TextView tvDialogTitle = dialog.findViewById(R.id.tvItemTitle);
        dialogImageViewPreview = dialog.findViewById(R.id.ivSelectedImage);
        Button btnSelectImage = dialog.findViewById(R.id.btnSelectImage);
        Button btnSubmitItem = dialog.findViewById(R.id.btnSubmitItem);
        EditText etItemTitle = dialog.findViewById(R.id.etItemTitle);
        EditText etItemMessage = dialog.findViewById(R.id.etItemMessage);
        RadioButton rbLost = dialog.findViewById(R.id.rbLost);
        RadioButton rbFound = dialog.findViewById(R.id.rbFound);

        // Pre-fill existing data!
        btnSubmitItem.setText("Update Post");
        etItemTitle.setText(item.getTitle());
        etItemMessage.setText(item.getMessage());
        if (item.getStatus().equals("FOUND")) rbFound.setChecked(true);
        Glide.with(this).load(item.getImageUrl()).into(dialogImageViewPreview);

        btnSelectImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSubmitItem.setOnClickListener(view -> {
            String newTitle = etItemTitle.getText().toString().trim();
            String newMessage = etItemMessage.getText().toString().trim();
            String newStatus = rbLost.isChecked() ? "LOST" : "FOUND";

            if (newTitle.isEmpty() || newMessage.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Updating...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // if picked a new image, upload it first
            if (selectedImageUri != null) {
                new Thread(() -> {
                    String uploadedImageUrl = uploadImageToImgBB(selectedImageUri);
                    runOnUiThread(() -> {
                        if (uploadedImageUrl != null) {
                            saveToFirebase(item.getId(), newTitle, newStatus, item.getUploaderName(), item.getUploaderRoll(), newMessage, uploadedImageUrl, dialog, progressDialog);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "New image upload failed.", Toast.LENGTH_LONG).show();
                        }
                    });
                }).start();
            } else {
                // if didn't pick a new image, just update the text and keep the old image URL
                saveToFirebase(item.getId(), newTitle, newStatus, item.getUploaderName(), item.getUploaderRoll(), newMessage, item.getImageUrl(), dialog, progressDialog);
            }
        });

        dialog.show();
    }

    private void saveToFirebase(String existingId, String title, String status, String uploaderName, String uploaderRoll, String message, String imageUrl, Dialog dialog, ProgressDialog progressDialog) {
        // If editing, use the existing ID. If creating new, generate a push ID.
        String pushId = (existingId != null) ? existingId : databaseReference.push().getKey();

        if (pushId != null) {
            long currentTime = System.currentTimeMillis();
            LostItem newItem = new LostItem(pushId, title, status, uploaderName, uploaderRoll, message, imageUrl, currentTime);

            databaseReference.child(pushId).setValue(newItem).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(this, existingId != null ? "Updated successfully!" : "Posted successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to save to database.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String uploadImageToImgBB(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = imageStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            String base64Image = Base64.encodeToString(byteBuffer.toByteArray(), Base64.DEFAULT);
            String urlParameters = "key=" + IMGBB_API_KEY + "&image=" + URLEncoder.encode(base64Image, "UTF-8");

            URL url = new URL("https://api.imgbb.com/1/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(urlParameters.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                JSONObject jsonObject = new JSONObject(response);
                return jsonObject.getJSONObject("data").getString("url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
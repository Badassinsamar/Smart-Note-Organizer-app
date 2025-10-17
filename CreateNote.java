package com.example.smartnoteorganizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartnoteorganizer.Model.Note;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateNote extends AppCompatActivity {

    private EditText etTitle, etDescription, etKeynote, etDate, etSubject;
    private Button btnSave, btnCancel;

    private DatabaseReference notesRef;
    private String editingId = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_note);



        // bind views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etKeynote = findViewById(R.id.etKeynote);
        etDate = findViewById(R.id.etDate);
        etSubject = findViewById(R.id.etSubject);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // init Firebase reference
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        notesRef = db.getReference("notes");

        // Check if we were started in edit mode (extras)
        if (getIntent() != null && getIntent().hasExtra("noteId")) {
            editingId = getIntent().getStringExtra("noteId");
            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etKeynote.setText(getIntent().getStringExtra("keynote"));
            etDate.setText(getIntent().getStringExtra("date"));
            etSubject.setText(getIntent().getStringExtra("subject"));
        }

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String keynote = etKeynote.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please enter a title or description", Toast.LENGTH_SHORT).show();
            return;
        }

        // If editing - update existing node
        if (!TextUtils.isEmpty(editingId)) {
            Note note = new Note(editingId,
                    title,
                    description,
                    keynote.isEmpty() ? null : keynote,
                    date.isEmpty() ? null : date,
                    subject.isEmpty() ? null : subject);

            notesRef.child(editingId).setValue(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateNote.this, "Note updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateNote.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                    });

        } else {
            // create new note using push() to get a stable key
            DatabaseReference newRef = notesRef.push();
            String id = newRef.getKey();

            Note note = new Note(id,
                    title,
                    description,
                    keynote.isEmpty() ? null : keynote,
                    date.isEmpty() ? null : date,
                    subject.isEmpty() ? null : subject);

            newRef.setValue(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateNote.this, "Note saved", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateNote.this, "Failed to save note", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
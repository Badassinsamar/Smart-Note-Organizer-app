package com.example.smartnoteorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartnoteorganizer.Adapter.NotesAdapter;
import com.example.smartnoteorganizer.Model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNotes;
    private NotesAdapter adapter;
    private List<Note> notes = new ArrayList<>();
    private DatabaseReference notesRef;
    private FloatingActionButton fabAdd;


    TextView tvAppTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize Firebase (ensure google-services.json is present)
        FirebaseApp.initializeApp(this);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        notesRef = db.getReference("notes");

        tvAppTitle = findViewById(R.id.tvAppTitle);
        tvAppTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CreateNote.class);
                startActivity(i);
            }
        });

        rvNotes = findViewById(R.id.rvNotes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, notes, new NotesAdapter.OnAction() {
            @Override
            public void onEdit(Note note) {
                // Start CreateNote in edit mode with extras
                Intent i = new Intent(MainActivity.this, CreateNote.class);
                i.putExtra("noteId", note.getNoteId());
                i.putExtra("title", note.getTitle());
                i.putExtra("description", note.getDescription());
                i.putExtra("keynote", note.getKeynote());
                i.putExtra("date", note.getDate());
                i.putExtra("subject", note.getSubject());
                startActivity(i);
            }

            @Override
            public void onDelete(Note note) {
                if (note.getNoteId() == null) return;
                notesRef.child(note.getNoteId()).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
            }
        });
        rvNotes.setAdapter(adapter);

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            // Start CreateNote activity for creating a new note
            startActivity(new Intent(MainActivity.this, CreateNote.class));
        });

        // Listen for realtime changes and update adapter list
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notes.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Note n = child.getValue(Note.class);
                    if (n != null) notes.add(n);
                }
                adapter.notifyDataSetChanged();
                // optionally scroll to top when new notes arrive:
                if (!notes.isEmpty()) rvNotes.scrollToPosition(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

// AssistantActivity.java
package com.example.emsiapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class AssistantActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private ListView listConversations;
    private RecyclerView recyclerMessages;
    private EditText editTextQuestion;
    private Button buttonAsk, buttonMenu, buttonNewConv;
    private ImageButton buttonReset;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;
    private ArrayAdapter<String> conversationAdapter;
    private List<String> conversationTitles = new ArrayList<>();
    private List<String> conversationIds = new ArrayList<>();

    private String currentConversationId;
    private FirebaseFirestore db;
    private String uid;

    private final OkHttpClient client = new OkHttpClient();
    private static final String URL_BACKEND = "http://192.168.11.106:3002/ask";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        drawerLayout = findViewById(R.id.drawerLayout);
        listConversations = findViewById(R.id.listConversations);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        editTextQuestion = findViewById(R.id.editTextQuestion);
        buttonAsk = findViewById(R.id.buttonAsk);
        buttonMenu = findViewById(R.id.buttonMenu);
        buttonReset = findViewById(R.id.buttonReset);
        buttonNewConv = findViewById(R.id.buttonNewConv);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user != null) ? user.getUid() : "demoUser";

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(messageList);
        recyclerMessages.setAdapter(adapter);

        conversationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, conversationTitles);
        listConversations.setAdapter(conversationAdapter);

        loadConversations();

        buttonMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        listConversations.setOnItemClickListener((parent, view, position, id) -> {
            currentConversationId = conversationIds.get(position);
            loadMessages(currentConversationId);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        listConversations.setOnItemLongClickListener((parent, view, position, id) -> {
            String conversationId = conversationIds.get(position);
            String currentTitle = conversationTitles.get(position);

            String[] options = {"🗑️ Supprimer", "✏️ Renommer"};
            new AlertDialog.Builder(this)
                    .setTitle("Options pour : " + currentTitle)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            db.collection("assistant_conversations")
                                    .document(uid)
                                    .collection("conversations")
                                    .document(conversationId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        if (conversationId.equals(currentConversationId)) {
                                            messageList.clear();
                                            adapter.notifyDataSetChanged();
                                            currentConversationId = null;
                                        }
                                        loadConversations();
                                        Toast.makeText(this, "Conversation supprimée", Toast.LENGTH_SHORT).show();
                                    });
                        } else if (which == 1) {
                            AlertDialog.Builder renameDialog = new AlertDialog.Builder(this);
                            renameDialog.setTitle("Renommer la conversation");

                            final EditText input = new EditText(this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setText(currentTitle);
                            renameDialog.setView(input);

                            renameDialog.setPositiveButton("Renommer", (dialog1, which1) -> {
                                String newTitle = input.getText().toString().trim();
                                if (!newTitle.isEmpty()) {
                                    db.collection("assistant_conversations")
                                            .document(uid)
                                            .collection("conversations")
                                            .document(conversationId)
                                            .update("title", newTitle)
                                            .addOnSuccessListener(unused -> loadConversations());
                                }
                            });
                            renameDialog.setNegativeButton("Annuler", null);
                            renameDialog.show();
                        }
                    })
                    .show();
            return true;
        });

        buttonNewConv.setOnClickListener(v -> {
            createNewConversation();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        buttonReset.setOnClickListener(v -> {
            createNewConversation();
        });

        buttonAsk.setOnClickListener(v -> {
            String question = editTextQuestion.getText().toString().trim();
            if (!question.isEmpty()) {
                editTextQuestion.setText("");

                if (currentConversationId == null) {
                    String title = "Conversation du " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("timestamp", new Date());
                    data.put("messages", new ArrayList<>());

                    db.collection("assistant_conversations")
                            .document(uid)
                            .collection("conversations")
                            .add(data)
                            .addOnSuccessListener(docRef -> {
                                currentConversationId = docRef.getId();
                                messageList.clear();
                                adapter.notifyDataSetChanged();
                                loadConversations();
                                addMessage(new Message(question, true));
                                sendToBackend(question);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erreur création conversation", Toast.LENGTH_SHORT).show());
                } else {
                    addMessage(new Message(question, true));
                    sendToBackend(question);
                }
            }
        });
    }

    private void addMessage(Message msg) {
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);
        saveMessagesToFirestore();
    }

    private void createNewConversation() {
        String title = "Conversation du " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("timestamp", new Date());
        data.put("messages", new ArrayList<>());

        db.collection("assistant_conversations")
                .document(uid)
                .collection("conversations")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    currentConversationId = docRef.getId();
                    messageList.clear();
                    adapter.notifyDataSetChanged();
                    loadConversations();
                    Toast.makeText(this, "Nouvelle conversation créée", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadConversations() {
        conversationTitles.clear();
        conversationIds.clear();

        db.collection("assistant_conversations")
                .document(uid)
                .collection("conversations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        conversationTitles.add(doc.getString("title"));
                        conversationIds.add(doc.getId());
                    }
                    conversationAdapter.notifyDataSetChanged();
                });
    }

    private void loadMessages(String conversationId) {
        db.collection("assistant_conversations")
                .document(uid)
                .collection("conversations")
                .document(conversationId)
                .get()
                .addOnSuccessListener(doc -> {
                    messageList.clear();
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) doc.get("messages");
                    if (messages != null) {
                        for (Map<String, Object> msg : messages) {
                            messageList.add(new Message(
                                    Objects.toString(msg.get("content"), ""),
                                    Boolean.TRUE.equals(msg.get("isUser"))
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void saveMessagesToFirestore() {
        List<Map<String, Object>> messages = new ArrayList<>();
        for (Message m : messageList) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", m.content);
            map.put("isUser", m.isUser);
            messages.add(map);
        }

        db.collection("assistant_conversations")
                .document(uid)
                .collection("conversations")
                .document(currentConversationId)
                .update("messages", messages);
    }

    private void sendToBackend(String question) {
        JSONObject json = new JSONObject();
        try {
            json.put("question", question);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(URL_BACKEND)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> addMessage(new Message("Erreur : " + e.getMessage(), false)));
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    final String answer = jsonResponse.getString("answer");
                    runOnUiThread(() -> addMessage(new Message(answer, false)));
                } catch (JSONException e) {
                    runOnUiThread(() -> addMessage(new Message("Erreur de réponse", false)));
                }
            }
        });
    }
}

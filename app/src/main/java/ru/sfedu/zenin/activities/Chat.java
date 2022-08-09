package ru.sfedu.zenin.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.sfedu.zenin.R;
import ru.sfedu.zenin.chat.ChatAdapter;
import ru.sfedu.zenin.chat.ChatObject;

public class Chat extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private String currentUserId, matchId, chatId;
    private String message, createdByUser;

    private EditText mSendEditText;
    private ImageButton mSendButton;

    DatabaseReference mDatabaseUser, mDatabaseChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        matchId = getIntent().getExtras().getString(String.valueOf(R.string.match_id));
        //"matchId"

        // БД для чатов пользователей
        mDatabaseUser = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.users))
                .child(currentUserId)
                .child(getString(R.string.connections))
                .child(getString(R.string.match_db))
                .child(matchId)
                .child(getString(R.string.chat_id));

        // БД всех чатов
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child(getString(R.string.chat));

        getChatId();

        mRecyclerView = findViewById(R.id.recyclerView);
        // Прокрутка
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);

        mChatLayoutManager = new LinearLayoutManager(Chat.this);

        // Помещаем LayoutManager в RecyclerView
        mRecyclerView.setLayoutManager(mChatLayoutManager);

        // Создаем адаптер с совпадениями
        mChatAdapter = new ChatAdapter(getDataSetChat(), Chat.this);
        mRecyclerView.setAdapter(mChatAdapter);

        mSendEditText = findViewById(R.id.messageText);
        mSendButton = findViewById(R.id.buttonSend);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    private void sendMessage(){
        // Нужно получить id чата и id того, кто отправил смс
        // Отправляем сообщение и добавляем его в бд
        String sendMessageText = mSendEditText.getText().toString();
        if(!sendMessageText.isEmpty()){
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put(getString(R.string.created_by_user), currentUserId);
            newMessage.put(getString(R.string.text_message), sendMessageText);

            newMessageDb.setValue(newMessage);
        }
        // Очищаем поле ввода текста
        mSendEditText.setText(null);
    }

    private void getChatId(){
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);

                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatMessages(){
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if(dataSnapshot.exists()){
//                    String message = null;
//                    String createdByUser = null;

                    if(dataSnapshot.child(getString(R.string.text_message)).getValue() != null){
                        message = dataSnapshot.child(getString(R.string.text_message)).getValue().toString();
                    }
                    if(dataSnapshot.child(getString(R.string.created_by_user)).getValue() != null){
                        createdByUser = dataSnapshot.child(getString(R.string.created_by_user)).getValue().toString();
                    }
                    if(message != null && createdByUser != null){
                        // Проверка кем было отправлено сообщение для дальнейшего правильного отображения сообщения
                        Boolean currentUserBoolean = false;
                        if(createdByUser.equals(currentUserId)) {
                            currentUserBoolean = true;
                        }
                        ChatObject newMessage = new ChatObject(message, currentUserBoolean);
                        resultChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private ArrayList<ChatObject> resultChat = new ArrayList<ChatObject>();
    // Получить совпадения наборов данных
    private List<ChatObject> getDataSetChat(){
        return resultChat;
    }
}
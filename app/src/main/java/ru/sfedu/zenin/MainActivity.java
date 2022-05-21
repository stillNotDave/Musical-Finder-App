package ru.sfedu.zenin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> arrayList;
    private ArrayAdapter<String> arrayAdapter;
    private int i;

    private Button signOut;

    //private String userProfileType;
    //private String oppositeUserProfileType;

    private FirebaseAuth mAuth;


    //@InjectView(R.id.frame) SwipeFlingAdapterView flingContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mAuth = FirebaseAuth.getInstance();

        // Проверяем тип профиля пользователя(группа или музыкант)
        checkUserProfileType();

        arrayList = new ArrayList<>();
//        arrayList.add("php");
//        arrayList.add("c");
//        arrayList.add("python");
//        arrayList.add("java");
//        arrayList.add("html");
//        arrayList.add("c++");
//        arrayList.add("css");
//        arrayList.add("javascript");

        // Адаптер для свайпа карточек профилей пользователей
        arrayAdapter = new ArrayAdapter<>(this, R.layout.item, R.id.helloText, arrayList );

        // Добавить данные в адаптер после создания адаптера
        //arrayAdapter.notifyDataSetChanged();

        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // Удаляем объект из адаптера
                Log.d("LIST", "removed object!");
                arrayList.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            // Смахиваем карточки влево и вправо
            @Override
            public void onLeftCardExit(Object dataObject) {
                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            // Проверка опустошается ли адаптер и все ли работает так как я хочу
            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

        // Нажатие на карточку адаптера
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
               Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
            }
        });



        // Выход из учетной записи
        signOut = findViewById(R.id.buttonSignIn);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginAndRegistration.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }

    private String userProfileType;
    private String oppositeUserProfileType;
    // Проверка - пользователь группа или музыкант
    public void checkUserProfileType(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference bandDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Band");
        bandDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userProfileType = "Band";
                    oppositeUserProfileType = "Musician";
                    // Вызываем функцию, получающую пользователей другого типа профиля
                    getOppositeProfileTypeUsers();
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

        DatabaseReference musicianDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Musician");
        musicianDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userProfileType = "Musician";
                    oppositeUserProfileType = "Band";
                    // Вызываем функцию, получающую пользователей другого типа профиля
                    getOppositeProfileTypeUsers();
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


    // Получить пользователей противоположного типа профиля
    //(Если зарегистрирован как музыкант - получить все зарегистрированные группы и наоборот)
    public void getOppositeProfileTypeUsers(){
        DatabaseReference oppositeProfileTypeDb = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(oppositeUserProfileType);
        oppositeProfileTypeDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя
                // Добавляем пользователя в адаптер
                if(dataSnapshot.exists()){
                    arrayList.add(dataSnapshot.child("name").getValue().toString());
                    arrayAdapter.notifyDataSetChanged();
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

}
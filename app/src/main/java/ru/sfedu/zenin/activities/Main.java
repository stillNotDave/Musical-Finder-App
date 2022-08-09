package ru.sfedu.zenin.activities;

import static ru.sfedu.zenin.utils.NavigationUtils.openActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

import ru.sfedu.zenin.cards.Card;
import ru.sfedu.zenin.cards.CardsArrayAdapter;
import ru.sfedu.zenin.R;

public class Main extends AppCompatActivity {

    //private Cards cardsData[];
    private CardsArrayAdapter arrayAdapter;
    private int i;

    private Button signOut, settings, matches;

    private final Context context = this;

    private String userProfileType;
    private String oppositeUserProfileType;
    private String currentUId;
    private DatabaseReference usersDb;
    private FirebaseAuth mAuth;

    ListView listView;
    List<Card> rowItems;

    private static final String TAG = "logger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users));
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        // Проверяем тип профиля пользователя(группа или музыкант)
        checkUserProfileType();

        rowItems = new ArrayList<Card>();
        // Адаптер для свайпа карточек профилей пользователей
        arrayAdapter = new CardsArrayAdapter(this, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // Удаляем объект из адаптера
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            // Смахиваем карточки влево и вправо
            @Override
            public void onLeftCardExit(Object dataObject) {
                // Помещаем всех дизлайкнутых пользователей в одну таблицы
                Card object = (Card) dataObject;
                String userId = object.getUserID();
                usersDb.child(userId)
                        .child(getString(R.string.connections))
                        .child(getString(R.string.dislike))
                        .child(currentUId).setValue(true);
                Toast.makeText(Main.this, R.string.dislike, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                // Помещаем всех лайкнутых пользователей в одну таблицы
                Card object = (Card) dataObject;
                String userId = object.getUserID();
                usersDb.child(userId)
                        .child(getString(R.string.connections))
                        .child(getString(R.string.like))
                        .child(currentUId).setValue(true);
                isConnectionMatch(userId);
                Toast.makeText(Main.this, R.string.like, Toast.LENGTH_SHORT).show();
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
               //Toast.makeText(Main.this, R.string.show_profile, Toast.LENGTH_SHORT).show();
            }
        });


//        // Находим элементы нижней панели и работаем с ними
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()){
//                    case R.id.actionMain:
//                        break;
//                    case R.id.actionMatch:
//                        openMatches();
//                        break;
//                    case R.id.actionSettings:
//                        openSettings();
//                        break;
//                }
//                return false;
//            }
//        });


        // Выход из учетной записи
        signOut = findViewById(R.id.buttonSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });

        // Открыть настройки профиля
//        settings = findViewById(R.id.buttonSettings);
//        settings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Toast.makeText(MainActivity.this, "something is wrong", Toast.LENGTH_SHORT).show();
//                openSettings();
//            }
//        });
//
//        matches = findViewById(R.id.buttonMatches);
//        matches.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openMatches();
//            }
//        });

    }

    // регистрируем совпадение лайков у пользователей
    private void isConnectionMatch(String userId){
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId)
                .child(getString(R.string.connections))
                .child(getString(R.string.like))
                .child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast.makeText(Main.this, R.string.match, Toast.LENGTH_LONG).show();

                    // Создаем уникальный ключ для чата 2 пользователей
                    String key = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.chat)).push().getKey();

                    // Фиксируем совпадение у пользователя которого мы лайкнули
                    usersDb.child(dataSnapshot.getKey())
                            .child(getString(R.string.connections))
                            .child(getString(R.string.match_db))
                            .child(currentUId)
                            .child(getString(R.string.chat_id)).setValue(key);

                    // Фиксируем совпадение у нас
                    usersDb.child(currentUId)
                            .child(getString(R.string.connections))
                            .child(getString(R.string.match_db))
                            .child(dataSnapshot.getKey())
                            .child(getString(R.string.chat_id)).setValue(key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Проверка типа профиля пользователя
    public void checkUserProfileType(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child(getString(R.string.user_profile_type)).getValue() != null){
                        userProfileType = dataSnapshot.child(getString(R.string.user_profile_type)).getValue().toString();

                        // Получаем тип профиля пользователя
                        if(userProfileType.equals(getString(R.string.we_are_band))){
                            oppositeUserProfileType = getString(R.string.i_am_musician);
                        }
                        else if(userProfileType.equals(getString(R.string.i_am_musician))){
                            oppositeUserProfileType = getString(R.string.we_are_band);
                        }

                        // Получаем противоположный тип профиля пользователя
                        getOppositeProfileTypeUsers();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // Получить пользователей противоположного типа профиля
    // (Если зарегистрирован как музыкант - получить все зарегистрированные группы и наоборот)
    public void getOppositeProfileTypeUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя и если мы еще не смахивали карточку этого пользователя
                // Добавляем пользователя в адаптер
                if(dataSnapshot.exists()
                        && !dataSnapshot.child(getString(R.string.connections)).child(getString(R.string.dislike)).hasChild(currentUId)
                        && !dataSnapshot.child(getString(R.string.connections)).child(getString(R.string.like)).hasChild(currentUId)
                        && dataSnapshot.child(getString(R.string.user_profile_type)).getValue().toString().equals(oppositeUserProfileType)){

                    // Если у пользователя нет фото, то ему ставится стандартное фото
                    String profileImageUrl = getString(R.string.default_image_map);
                    if(!dataSnapshot.child((getString(R.string.profile_image_url))).getValue().equals(getString(R.string.default_image_map))){
                        profileImageUrl = dataSnapshot.child((getString(R.string.profile_image_url))).getValue().toString();
                    }
                    Card item = new Card(dataSnapshot.getKey(), dataSnapshot
                            .child((getString(R.string.user_name))).getValue().toString(), profileImageUrl);
                    rowItems.add(item);
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



    // Выход из учетной записи пользовтеля
    public void logoutUser(){
        mAuth.signOut();
        openActivity(context, LoginAndRegistration.class);
        finish();
        return;
    }

    public void openSettings(){
        Intent intent = new Intent(Main.this, ProfileSettings.class);
        //intent.putExtra(getString(R.string.user_profile_type), userProfileType);
        startActivity(intent);
        return;
    }

    public void openMatches(){
        Intent intent = new Intent(Main.this, Match.class);
        startActivity(intent);
        return;
    }

}
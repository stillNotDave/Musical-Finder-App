package ru.sfedu.zenin;

import static ru.sfedu.zenin.NavigationUtils.openActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {

    private Cards cardsData[];
    private CardsArrayAdapter arrayAdapter;
    private int i;

    private Button signOut;

    private final Context context = this;

    private String userProfileType;
    private String oppositeUserProfileType;
    private String currentUId;

    private DatabaseReference usersDb;
    private FirebaseAuth mAuth;

    ListView listView;
    List<Cards> rowItems;

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

        rowItems = new ArrayList<Cards>();
        // Адаптер для свайпа карточек профилей пользователей
        arrayAdapter = new CardsArrayAdapter(this, R.layout.item, rowItems);


        // Добавить данные в адаптер после создания адаптера
        //arrayAdapter.notifyDataSetChanged();

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
                // Получаем данные пользователя которого видим на карточке
                Cards object = (Cards) dataObject;
                String userId = object.getUserID();
                // При смахе карточки влево мы как бы говорим "нет" и пропускаем пользователя
                // тк он нам не интересен
                // Создаем в таблице Users в группе/музыканте поля Да/Нет для пользователя с карточки
                usersDb.child(oppositeUserProfileType).child(userId).child(getString(R.string.connections))
                        .child(getString(R.string.nope)).child(currentUId).setValue(true);
                Toast.makeText(MainActivity.this, R.string.dislike, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Cards object = (Cards) dataObject;
                String userId = object.getUserID();
                usersDb.child(oppositeUserProfileType).child(userId).child(getString(R.string.connections))
                        .child(getString(R.string.yep)).child(currentUId).setValue(true);

                isConnectionMatch(userId);

                Toast.makeText(MainActivity.this, R.string.like, Toast.LENGTH_SHORT).show();
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
               Toast.makeText(MainActivity.this, R.string.show_profile, Toast.LENGTH_SHORT).show();
            }
        });



        // Выход из учетной записи
        signOut = findViewById(R.id.buttonSettings);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //logoutUser();
                openSettings();
            }
        });

    }


    // Проверка - пользователь группа или музыкант
    public void checkUserProfileType(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference bandDb = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users))
                .child(getString(R.string.we_are_band));
        bandDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userProfileType = getString(R.string.we_are_band);
                    oppositeUserProfileType = getString(R.string.i_am_musician);
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

        DatabaseReference musicianDb = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users))
                .child(getString(R.string.i_am_musician));
        musicianDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userProfileType = getString(R.string.i_am_musician);
                    oppositeUserProfileType = getString(R.string.we_are_band);
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
        DatabaseReference oppositeProfileTypeDb = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users))
                .child(oppositeUserProfileType);
        oppositeProfileTypeDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Если ключ совпадает в id пользователя и если id пользователя не существует в лайкнутых и пропущенных пользователях,
                // то добавляем пользователя в адаптер
                if(dataSnapshot.exists() && !dataSnapshot.child(getString(R.string.connections)).child(getString(R.string.nope)).hasChild(currentUId) && !dataSnapshot.child(getString(R.string.connections)).child(getString(R.string.yep)).hasChild(currentUId)){
                    // .child("name").getValue().toString());
                    Cards item = new Cards(dataSnapshot.getKey(), dataSnapshot
                            .child((getString(R.string.user_name))).getValue().toString());
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

    // Метод чтобы показать взаимне лайки если они есть
    private void isConnectionMatch(String userId){
        DatabaseReference currentUserMatchDb = usersDb.child(userProfileType).child(currentUId).child(getString(R.string.connections))
                .child(getString(R.string.yep)).child(userId);
        currentUserMatchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d(TAG, "new match");
                    Toast.makeText(MainActivity.this, R.string.match_toast, Toast.LENGTH_LONG).show();

                    // Сохраняем совпадения в БД
                    usersDb.child(oppositeUserProfileType).child(dataSnapshot.getKey()).child(getString(R.string.connections))
                            .child(getString(R.string.match)).child(currentUId).setValue(true);
                    usersDb.child(userProfileType).child(currentUId).child(getString(R.string.connections))
                            .child(getString(R.string.match)).child(dataSnapshot.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Выход из учетной записи пользовтеля
    public void logoutUser(){
        mAuth.signOut();
        openActivity(context, UserSettings.class);
        finish();
        return;
    }

    public void openSettings(){
        openActivity(context, UserSettings.class);
        // без finish()
        return;
    }

}
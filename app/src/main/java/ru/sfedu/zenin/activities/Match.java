package ru.sfedu.zenin.activities;

import static ru.sfedu.zenin.utils.NavigationUtils.openActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ru.sfedu.zenin.matches.MatchesAdapter;
import ru.sfedu.zenin.matches.MatchesObject;
import ru.sfedu.zenin.R;

public class Match extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mMatchesAdapter;
    private RecyclerView.LayoutManager mMatchesLayoutManager;

    private String currentUserId;

    Context context;
    private Button back;

    private String name, profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRecyclerView = findViewById(R.id.recyclerView);
        // Прокрутка
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);

        mMatchesLayoutManager = new LinearLayoutManager(Match.this);

        // Помещаем LayoutManager в RecyclerView
        mRecyclerView.setLayoutManager(mMatchesLayoutManager);

        // Создаем адаптер с совпадениями
        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), Match.this);
        mRecyclerView.setAdapter(mMatchesAdapter);

        // Выход обратно в главную активность
        // Эта кнопка почему-то крашит приложение
        back = findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, Main.class);
                finish();
            }
        });

        getUserMatchId();

        // Создаем 100 тестовых объектов и помещаем в адаптер
//        for(int i=0; i<100;i++){
//            MatchesObject object = new MatchesObject(Integer.toString(i), Integer.toString(i), Integer.toString(i));
//            resultMatches.add(object);
//        }
//        mMatchesAdapter.notifyDataSetChanged();

//        MatchesObject object = new MatchesObject("asd");
//        resultMatches.add(object);
//        mMatchesAdapter.notifyDataSetChanged();

    }

    // Получаем id пользователя с которым есть совпадение
    private void getUserMatchId(){
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.users))
                .child(currentUserId)
                .child(getString(R.string.connections))
                .child(getString(R.string.match_db));

        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        fetchMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Получаем информацию о пользователе с которым есть сопадение
    private void fetchMatchInformation(String key){
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.users))
                .child(key);

        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    //String name = "";
                    //String profileImageUrl = "";

                    if(dataSnapshot.child(getString(R.string.user_name)).getValue() != null){
                        name = dataSnapshot.child(getString(R.string.user_name)).getValue().toString();
                    }
                    if(dataSnapshot.child(getString(R.string.profile_image_url)).getValue() != null){
                        profileImageUrl = dataSnapshot.child(getString(R.string.profile_image_url)).getValue().toString();
                    }

                    MatchesObject object = new MatchesObject(userId, name, profileImageUrl);
                    resultMatches.add(object);
                    mMatchesAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private ArrayList<MatchesObject> resultMatches = new ArrayList<MatchesObject>();
    // Получить совпадения наборов данных
    private List<MatchesObject> getDataSetMatches(){
        return resultMatches;
    }
}
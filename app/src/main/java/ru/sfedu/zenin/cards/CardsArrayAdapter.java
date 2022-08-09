package ru.sfedu.zenin.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.sfedu.zenin.R;

public class CardsArrayAdapter extends android.widget.ArrayAdapter<Card> {

    Context context;

    public CardsArrayAdapter(Context context, int resourceId, List<Card> items){
        super(context, resourceId, items);
    }

    // Получаем данные о пользователе (фото и имя)
    public View getView(int position, View convertView, ViewGroup parent){
        Card cardItem = getItem(position);
        if(convertView == null){
            // LayoutInflater – это класс, который умеет из содержимого layout-файла создать View-элемент.
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        TextView userName = convertView.findViewById(R.id.userName);
        ImageView userPhoto = convertView.findViewById(R.id.userPhoto);

        userName.setText(cardItem.getUserName());

        // Ставим стандартное фото зарегистрировавшемуся пользователю
        switch (cardItem.getProfileImageUrl()){
            case "default":
                Glide.with(convertView.getContext()).load(R.drawable.default_user_profile_image).into(userPhoto);
                break;
            default:
                Glide.clear(userPhoto);
                Glide.with(convertView.getContext()).load(cardItem.getProfileImageUrl()).into(userPhoto);
                break;
        }
        return convertView;
    }

}

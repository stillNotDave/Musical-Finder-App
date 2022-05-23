package ru.sfedu.zenin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CardsArrayAdapter extends android.widget.ArrayAdapter<Cards> {

    Context context;

    public CardsArrayAdapter(Context context, int resourceId, List<Cards> items){
        super(context, resourceId, items);
    }

    // Получаем данные о пользователе (фото и имя)
    public View getView(int position, View convertView, ViewGroup parent){
        Cards cardItem = getItem(position);
        if(convertView == null){
            // LayoutInflater – это класс, который умеет из содержимого layout-файла создать View-элемент.
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        TextView userName = convertView.findViewById(R.id.userName);
        ImageView userPhoto = convertView.findViewById(R.id.userPhoto);

        userName.setText(cardItem.getUserName());
        // Получаем картинку из ресурсов для проверки
        userPhoto.setImageResource(R.mipmap.ic_launcher);

        return convertView;
    }

}

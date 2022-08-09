package ru.sfedu.zenin.matches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.sfedu.zenin.R;

public class MatchesAdapter extends RecyclerView.Adapter<MatchViewHolders>{

    private List<MatchesObject> matchesList;
    private Context context;

    public MatchesAdapter(List<MatchesObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @NonNull
    @Override
    public MatchViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Помещаем в карточку нужный там layout
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);
        MatchViewHolders rcv = new MatchViewHolders((layoutView));

        return rcv;
    }

    // holder - это все что есть в item_matches
    // position - позиция, в которой находится объект recyclerview
    @Override
    public void onBindViewHolder(@NonNull MatchViewHolders holder, int position) {
        holder.mMatchId.setText(matchesList.get(position).getUserID());
        holder.mMatchName.setText(matchesList.get(position).getName());
        if(!matchesList.get(position).getProfileImageUrl().equals(R.string.default_image_map)){
            Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
        }
    }

    @Override
    public int getItemCount() {
        return matchesList.size();
    }
}

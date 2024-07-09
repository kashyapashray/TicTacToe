package androidsamples.java.tictactoe;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.MyViewHolder> {

  ArrayList<GameList> gameLists;
  RecyclerCallBack recyclerCallBack;

  public OpenGamesAdapter(ArrayList<GameList> gameLists) {
    this.gameLists = gameLists;
  }

  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
    return new MyViewHolder(view);
  }

  public void setOpenGame(RecyclerCallBack callBack){
      recyclerCallBack = callBack;
  }

  @SuppressLint("NotifyDataSetChanged")
  void refresh(ArrayList<GameList> gameLists){
    this.gameLists = gameLists;
    notifyDataSetChanged();
  }

  @Override
  public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
      holder.mIdView.setText(String.valueOf(position));
      holder.mContentView.setText(gameLists.get(position).against + "\nWins="+gameLists.get(position).wins+"\nLosses="+gameLists.get(position).losses);
      holder.mView.setOnClickListener(v -> recyclerCallBack.navToChosen(gameLists.get(position).gameId, gameLists.get(position).listId));
  }

  @Override
  public int getItemCount() {
    return gameLists.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public MyViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}
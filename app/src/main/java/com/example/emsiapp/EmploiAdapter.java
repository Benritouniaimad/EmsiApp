package com.example.emsiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmploiAdapter extends RecyclerView.Adapter<EmploiAdapter.EmploiViewHolder> {

    private List<Emploi> emploiList;

    public EmploiAdapter(List<Emploi> emploiList) {
        this.emploiList = emploiList;
    }

    @NonNull
    @Override
    public EmploiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emploi, parent, false);
        return new EmploiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmploiViewHolder holder, int position) {
        Emploi emploi = emploiList.get(position);

        // Format complet dans un seul bloc de texte
        String bloc = emploi.getHeureDebut() + " - " + emploi.getHeureFin()
                + "\n" + emploi.getMatiere()
                + "\nSalle " + emploi.getSalle()
                + "\n" + emploi.getNiveau();

        holder.tvBloc.setText(bloc);
    }

    @Override
    public int getItemCount() {
        return emploiList.size();
    }

    public static class EmploiViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloc;

        public EmploiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloc = itemView.findViewById(R.id.tvBloc); // Voir layout ci-dessous
        }
    }

    public void updateList(List<Emploi> newList) {
        emploiList = newList;
        notifyDataSetChanged();
    }
}

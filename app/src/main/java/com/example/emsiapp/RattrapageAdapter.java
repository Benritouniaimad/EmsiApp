package com.example.emsiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RattrapageAdapter extends RecyclerView.Adapter<RattrapageAdapter.ViewHolder> {

    private List<Rattrapage> rattrapages;

    public RattrapageAdapter(List<Rattrapage> rattrapages) {
        this.rattrapages = rattrapages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInfos;

        public ViewHolder(View view) {
            super(view);
            tvInfos = view.findViewById(R.id.tvInfos);
        }
    }

    @NonNull
    @Override
    public RattrapageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rattrapage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rattrapage r = rattrapages.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        String dateStr = sdf.format(r.date.toDate());
        holder.tvInfos.setText(r.matiere + " - " + r.groupe + "\n" +
                dateStr + " de " + r.heureDebut + " à " + r.heureFin + "\n" +
                r.site + ", " + r.salle);
    }

    @Override
    public int getItemCount() {
        return rattrapages.size();
    }
}
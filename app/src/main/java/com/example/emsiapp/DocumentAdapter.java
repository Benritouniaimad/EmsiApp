package com.example.emsiapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    public interface OnDocumentClickListener {
        void onDocumentClick(Document doc);
    }

    private List<Document> documentList;
    private OnDocumentClickListener listener;

    public DocumentAdapter(List<Document> documentList, OnDocumentClickListener listener) {
        this.documentList = documentList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        Button btnOpen, btnDownload;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDocName);
            tvDate = itemView.findViewById(R.id.tvDocDate);
            btnOpen = itemView.findViewById(R.id.btnOpen);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }

    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentAdapter.ViewHolder holder, int position) {
        Document doc = documentList.get(position);
        holder.tvName.setText(doc.getName());
        holder.tvDate.setText(doc.getDate());

        // Bouton OUVRIR
        holder.btnOpen.setOnClickListener(v -> listener.onDocumentClick(doc));

        // Bouton TÉLÉCHARGER
        holder.btnDownload.setOnClickListener(v -> {
            String url = doc.getUrl();
            if (url != null && (url.startsWith("http") || url.startsWith("https"))) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "Ce document n'est pas téléchargeable.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public void updateList(List<Document> newList) {
        documentList.clear();
        documentList.addAll(newList);
        notifyDataSetChanged();
    }
}

package com.example.emsiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messageList;

    public static final int VIEW_TYPE_USER = 0;
    public static final int VIEW_TYPE_ASSISTANT = 1;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textMessage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_USER)
                ? R.layout.item_message_user
                : R.layout.item_message_assistant;

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(messageList.get(position).content);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
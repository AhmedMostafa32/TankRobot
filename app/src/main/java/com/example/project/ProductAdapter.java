package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<String> productList;
    private List<String> filteredProductList;
    private OnRemoveClickListener onRemoveClick;
    private SendDataToArduinoListener sendDataToArduino;

    public ProductAdapter(List<String> productList, OnRemoveClickListener onRemoveClick, SendDataToArduinoListener sendDataToArduino) {
        this.productList = productList;
        this.filteredProductList = new ArrayList<>(productList);
        this.onRemoveClick = onRemoveClick;
        this.sendDataToArduino = sendDataToArduino;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_model, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        String product = filteredProductList.get(position);
        holder.tvProductData.setText(product);

        // Set click listener for the remove button
        holder.btnRemove.setOnClickListener(view -> {
            onRemoveClick.onRemoveClick(product);
            sendDataToArduino.sendDataToArduino(product + "2"); // Concatenate '2' to the product string
        });
    }

    @Override
    public int getItemCount() {
        return filteredProductList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductData;
        Button btnRemove;

        public ProductViewHolder(View view) {
            super(view);
            tvProductData = view.findViewById(R.id.tvProductData);
            btnRemove = view.findViewById(R.id.remove);
        }
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            filteredProductList = new ArrayList<>(productList);
        } else {
            List<String> filteredList = new ArrayList<>();
            for (String product : productList) {
                if (product.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
            filteredProductList = filteredList;
        }
        notifyDataSetChanged();
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(String product);
    }

    public interface SendDataToArduinoListener {
        void sendDataToArduino(String data);
    }
}

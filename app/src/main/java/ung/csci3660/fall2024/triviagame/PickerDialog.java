package ung.csci3660.fall2024.triviagame;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

public class PickerDialog {

    public interface ItemPickedListener {
        void onItemPicked(String itemText, Integer itemValue);
    }

//    public static PickerDialog launch(String dialogKey, String title, Map<String, Integer> items) {
//        PickerDialog dialog = new PickerDialog();
//        Bundle args = new Bundle();
//        args.putString("dialogKey", dialogKey);
//        Bundle itemsBundle = new Bundle();
//        items.forEach(itemsBundle::putInt);
//        args.putBundle("items", itemsBundle);
//        dialog.setArguments(args);
//        return dialog;
//    }

    private final ItemPickedListener itemPickedListener;
    private final String[] itemKeys;
    private final Map<String, Integer> items;
    private final String title;
    private final int heightDP;

    private Dialog dialog;

    public PickerDialog(int heightDP, String title, Map<String, Integer> items, ItemPickedListener pickedListener) {
        // Required empty public constructor
        this.itemPickedListener = pickedListener;
        this.items = items;
        itemKeys = items.keySet().toArray(new String[0]);
        this.title = title;
        this.heightDP = heightDP;
    }

    public void show(LayoutInflater inflater) {
        dialog = new Dialog(inflater.getContext());

        View view = inflater.inflate(R.layout.dialog_item_picker, null);
        dialog.setContentView(view);

        TextView titleView = view.findViewById(R.id.pickerTitle);
        titleView.setText(title);

        RecyclerView rv = view.findViewById(R.id.item_list);
        rv.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        ItemAdapter adapter = new ItemAdapter();
        rv.setAdapter(adapter);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rv.getLayoutParams();
        params.height = (int) (heightDP*view.getResources().getDisplayMetrics().density);

        rv.setLayoutParams(params);

        dialog.show();
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ItemHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            String key = itemKeys[position];
            Integer value = items.get(key);
            holder.bind(key);
            holder.itemView.setOnClickListener((v) -> {
                dialog.dismiss();
                itemPickedListener.onItemPicked(key, value);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class ItemHolder extends RecyclerView.ViewHolder {

        private final TextView itemTextView;

        public ItemHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.picker_item, parent, false));
            itemTextView = itemView.findViewById(R.id.item_name);
        }

        public void bind(String key) {
            itemTextView.setText(key);
        }
    }

}

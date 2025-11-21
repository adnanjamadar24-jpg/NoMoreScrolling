package com.example.nomorescrolling;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.VH> {

    private List<AppInfo> fullList;
    private List<AppInfo> filteredList;
    private Context ctx;
    private SharedPreferences prefs;
    private Set<String> selectedPkgs = new HashSet<>();

    public AppListAdapter(Context ctx, List<AppInfo> apps){
        this.ctx = ctx;
        this.fullList = new ArrayList<>(apps);
        this.filteredList = new ArrayList<>(apps);
        prefs = ctx.getSharedPreferences("nm_prefs", Context.MODE_PRIVATE);
        loadFromPrefs();
    }

    private void loadFromPrefs(){
        for(AppInfo a: fullList){
            int lim = prefs.getInt("limit_" + a.packageName, a.limitMinutes);
            boolean block = prefs.getBoolean("block_" + a.packageName, a.blockMode);
            a.limitMinutes = lim; a.blockMode = block;
        }
    }

    public void reloadPrefs(){ loadFromPrefs(); notifyDataSetChanged(); }
    public void filter(String q){ filteredList.clear(); if(q==null||q.trim().isEmpty()){ filteredList.addAll(fullList); } else { String s=q.toLowerCase(); for(AppInfo a: fullList) if(a.name.toLowerCase().contains(s)||a.packageName.toLowerCase().contains(s)) filteredList.add(a); } notifyDataSetChanged(); }
    public void selectAllVisible(boolean visibleOnly){ if(visibleOnly){ for(AppInfo a: filteredList) selectedPkgs.add(a.packageName); } else { for(AppInfo a: fullList) selectedPkgs.add(a.packageName); } notifyDataSetChanged(); }
    public void deselectAll(){ selectedPkgs.clear(); notifyDataSetChanged(); }
    public Set<String> getSelectedPackages(){ return new HashSet<>(selectedPkgs); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_app, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        AppInfo a = filteredList.get(position);
        h.tvName.setText(a.name); h.etLimit.setText(String.valueOf(a.limitMinutes)); h.switchBlock.setChecked(a.blockMode); h.cbSelect.setChecked(selectedPkgs.contains(a.packageName));
        if(a.icon!=null) h.ivIcon.setImageDrawable(a.icon); else h.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        h.cbSelect.setOnCheckedChangeListener((btn,checked)->{ if(checked) selectedPkgs.add(a.packageName); else selectedPkgs.remove(a.packageName); });
        h.btnSave.setOnClickListener(v->{ try{ int m=Integer.parseInt(h.etLimit.getText().toString()); a.limitMinutes=m; a.blockMode=h.switchBlock.isChecked(); prefs.edit().putInt("limit_"+a.packageName,m).putBoolean("block_"+a.packageName,a.blockMode).apply(); Toast.makeText(ctx,"Saved for "+a.name,Toast.LENGTH_SHORT).show(); }catch(NumberFormatException e){ Toast.makeText(ctx,"Enter valid number",Toast.LENGTH_SHORT).show(); }});
    }

    @Override public int getItemCount(){ return filteredList.size(); }
    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbSelect; ImageView ivIcon; TextView tvName; EditText etLimit; Switch switchBlock; Button btnSave;
        VH(View v){ super(v); cbSelect=v.findViewById(R.id.cbSelect); ivIcon=v.findViewById(R.id.ivIcon); tvName=v.findViewById(R.id.tvAppName); etLimit=v.findViewById(R.id.etLimit); switchBlock=v.findViewById(R.id.switchBlock); btnSave=v.findViewById(R.id.btnSaveApp); }
    }
}

package com.example.nomorescrolling;

import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private Button btnStartService, btnGrantOverlay, btnSelectAll, btnImportDefaults;
    private TextView tvStatus;
    private EditText etSearch;
    private boolean selectAllState = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnStartService = findViewById(R.id.btnStartService);
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay);
        recyclerView = findViewById(R.id.recyclerApps);
        etSearch = findViewById(R.id.etSearch);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnImportDefaults = findViewById(R.id.btnImportDefaults);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(this, loadInstalledApps());
        recyclerView.setAdapter(adapter);

        btnStartService.setOnClickListener(v -> {
            if (!hasUsageAccess()) {
                startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1001);
                Toast.makeText(this, "Please grant Usage Access permission", Toast.LENGTH_LONG).show();
                return;
            }
            Intent svc = new Intent(this, UsageMonitorService.class);
            startForegroundService(svc);
            Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show();
        });

        btnGrantOverlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show();
            }
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { adapter.filter(s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        btnSelectAll.setOnClickListener(v -> {
            selectAllState = !selectAllState;
            if(selectAllState){ adapter.selectAllVisible(false); btnSelectAll.setText("Deselect All"); }
            else { adapter.deselectAll(); btnSelectAll.setText("Select All"); }
        });

        btnImportDefaults.setOnClickListener(v -> { showImportDefaultsDialog(); });

        updateStatusText();
    }

    private void showImportDefaultsDialog(){
        final String[] presets = new String[] {"Social (30m, warning)", "Focus (20m, block)", "Relax (60m, warning)", "Apply 30m Block to Selected"};
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle("Choose preset to import");
        b.setItems(presets, (dialog, which) -> {
            if(which==1){ applyPresetToSelectedOrAll(20,true); return; }
            if(which==2){ applyPresetToSelectedOrAll(60,false); return; }
            if(which==3){ applyPresetToSelectedOrAll(30,true); return; }
            // social mapping: apply 30m warning to common social packages
            String[] socials = new String[]{"com.instagram.android","com.facebook.katana","com.google.android.youtube","com.zhiliaoapp.musically","com.snapchat.android","com.twitter.android","com.whatsapp"};
            Set<String> selected = adapter.getSelectedPackages();
            if(selected.isEmpty()){
                for(AppInfo a: loadInstalledApps()){
                    for(String p: socials) if(a.packageName.equals(p)) saveLimitForPkg(a.packageName,30,false);
                }
                Toast.makeText(this, "Defaults imported for recognized social apps", Toast.LENGTH_SHORT).show();
            } else {
                for(String pkg: selected) for(String p: socials) if(pkg.equals(p)) saveLimitForPkg(pkg,30,false);
                Toast.makeText(this, "Preset applied to selected apps (where matching)", Toast.LENGTH_SHORT).show();
            }
            adapter.reloadPrefs();
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void applyPresetToSelectedOrAll(int minutes, boolean blockMode){
        Set<String> selected = adapter.getSelectedPackages();
        if(selected.isEmpty()){
            for(AppInfo a: loadInstalledApps()) saveLimitForPkg(a.packageName, minutes, blockMode);
            Toast.makeText(this, "Preset applied to all apps", Toast.LENGTH_SHORT).show();
        } else {
            for(String pkg: selected) saveLimitForPkg(pkg, minutes, blockMode);
            Toast.makeText(this, "Preset applied to selected apps", Toast.LENGTH_SHORT).show();
        }
        adapter.reloadPrefs();
    }

    private void saveLimitForPkg(String pkg, int minutes, boolean block){
        getSharedPreferences("nm_prefs", MODE_PRIVATE).edit().putInt("limit_"+pkg, minutes).putBoolean("block_"+pkg, block).apply();
    }

    private List<AppInfo> loadInstalledApps(){
        PackageManager pm = getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(0);
        List<AppInfo> apps = new ArrayList<>();
        for(PackageInfo p : packs){
            ApplicationInfo ai = p.applicationInfo;
            Intent launchIntent = pm.getLaunchIntentForPackage(p.packageName);
            if(launchIntent==null) continue;
            boolean isSystem = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if(isSystem) continue;
            String name = pm.getApplicationLabel(ai).toString();
            AppInfo a = new AppInfo(name, p.packageName, 30, true);
            try { a.icon = pm.getApplicationIcon(ai); } catch(Exception e){}
            apps.add(a);
        }
        apps.sort(Comparator.comparing(o->o.name.toLowerCase()));
        return apps;
    }

    private boolean hasUsageAccess(){
        try {
            UsageStatsManager m = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            List<android.app.usage.UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000L*60*10, now);
            return stats != null && stats.size() > 0;
        } catch (Exception e){ return false; }
    }

    private void updateStatusText(){
        String s = "Usage Access: " + (hasUsageAccess() ? "GRANTED" : "NOT GRANTED");
        s += "\nOverlay permission: " + (Settings.canDrawOverlays(this) ? "GRANTED" : "NOT GRANTED");
        tvStatus.setText(s);
    }

    @Override protected void onResume(){ super.onResume(); updateStatusText(); adapter.reloadPrefs(); }
}

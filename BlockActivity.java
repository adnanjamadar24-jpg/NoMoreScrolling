package com.example.nomorescrolling;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BlockActivity extends AppCompatActivity {
    private String pkg;
    @Override protected void onCreate(Bundle savedInstanceState){ super.onCreate(savedInstanceState); setContentView(R.layout.activity_block); setFinishOnTouchOutside(false); pkg=getIntent().getStringExtra("pkg"); TextView tv=findViewById(R.id.tvBlockMsg); tv.setText("⛔ Daily Limit Reached for:\n"+friendlyName(pkg)); Button btnHome=findViewById(R.id.btnGoHome); Button btnSettings=findViewById(R.id.btnOpenSettings); Button btnReset=findViewById(R.id.btnResetUsage); btnHome.setOnClickListener(v->{ finishAffinity(); Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME); i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }); btnSettings.setOnClickListener(v->{ Intent i=new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS); i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }); btnReset.setOnClickListener(v->{ String day=todayKey(); getSharedPreferences("nm_prefs",MODE_PRIVATE).edit().putInt("sec_"+pkg+"_"+day,0).putInt("mins_"+pkg+"_"+day,0).putBoolean("reached_"+pkg+"_"+day,false).putBoolean("warned80_"+pkg+"_"+day,false).apply(); finish(); }); }
    private String friendlyName(String pkg){ try{ return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString(); }catch(Exception e){ return pkg; } }
    private String todayKey(){ java.util.Calendar c=java.util.Calendar.getInstance(); return String.format("%04d-%02d-%02d", c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH)+1, c.get(java.util.Calendar.DAY_OF_MONTH)); }
    @Override public void onBackPressed(){ }
}

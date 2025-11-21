package com.example.nomorescrolling;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String name; public String packageName; public int limitMinutes; public boolean blockMode; public Drawable icon;
    public AppInfo(String name, String pkg, int limitMinutes, boolean blockMode){ this.name=name; this.packageName=pkg; this.limitMinutes=limitMinutes; this.blockMode=blockMode;}
}

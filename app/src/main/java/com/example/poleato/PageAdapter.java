package com.example.poleato;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList= new ArrayList<>();
    private List<String> fragmentNameList= new ArrayList<>();


    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String nameFragment){
        fragmentList.add(fragment);
        fragmentNameList.add(nameFragment);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    public Fragment getMyItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return fragmentNameList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}

package com.mad.poleato;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create and handle the fragment page adapter.
 * It contains the list of fragments showed in the tab view, with their names.
 */
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

    @Override
    public CharSequence getPageTitle(int i) {
        return fragmentNameList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragmentList.set(position, fragment);
        return fragment;
    }
}
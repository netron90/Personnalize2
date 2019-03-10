package com.netron90.correction.personnalize;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CHRISTIAN on 14/02/2019.
 */

public class TabAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mListFragment = new ArrayList<>();
    private List<String> mListFragmentTitle = new ArrayList<>();

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mListFragment.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mListFragmentTitle.get(position);
    }

    @Override
    public int getCount() {
        return mListFragment.size();
    }

    public void addFragment(Fragment fragment, String title)
    {
        mListFragment.add(fragment);
        mListFragmentTitle.add(title);
    }
}

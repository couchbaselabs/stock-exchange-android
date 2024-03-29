package com.couchbase.stockexchange;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ListDetailFragmentPagerAdapter extends FragmentPagerAdapter {
    private String tabTitles[] = new String[]{"Stocks", "Users"};
    private final int pageCount;

    public ListDetailFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.pageCount = tabTitles.length;
    }

    public ListDetailFragmentPagerAdapter(FragmentManager fm, int pageCount) {
        super(fm);
        this.pageCount = pageCount;
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new StocksFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public void updateQuery(){

    }
}

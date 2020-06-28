package com.example.chatapplication;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabAccessorAdapter extends FragmentPagerAdapter {
    public TabAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:{
                ChatFrag chatFrag =new ChatFrag();
                return chatFrag;
            }
            case 1:{
                GroupFrag groupFrag=new GroupFrag();
                return groupFrag;
            }
            case 2:{
                ContactsFrag contactsFrag =new ContactsFrag();
                return contactsFrag;
            }
            case 3:{
                RequestFragment requestFragment =new RequestFragment();
                return requestFragment;
            }
            default:{
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: {
                return "Chats";
            }
            case 1: {
                return "Group";
            }
            case 2: {
                return "Contacts";
            }
            case 3: {
                return "Requests";
            }
            default: {
                return null;
            }
        }
    }
}

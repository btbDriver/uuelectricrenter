package it.neokree.materialnavigationdrawer.util;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import it.neokree.materialnavigationdrawer.elements.MaterialSection;

/**
 * Created by neokree on 14/02/15.
 */
public class MaterialToolBarDrawerToggle<Fragment> implements DrawerLayout.DrawerListener {

    private MaterialSection<Fragment> requestedSection;
    private boolean request;

    public MaterialToolBarDrawerToggle() {
        request = false;
    }

    public void addRequest(MaterialSection section) {
        request = true;
        requestedSection = section;
    }

    public void removeRequest() {
        request = false;
        requestedSection = null;
    }

    public boolean hasRequest() {
        return request;
    }

    public MaterialSection getRequestedSection() {
        return requestedSection;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}

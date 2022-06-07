package org.mate.commons.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MockViewGroup {
    ViewGroup vg;
    List<View> children = new ArrayList<>();

    public MockViewGroup() {
        vg = mock(ViewGroup.class);
        updateResponses();
    }

    private void updateResponses() {
        int childCount = children.size();
        when(vg.getChildCount()).thenReturn(childCount);

        for (int i = 0; i < childCount; i++) {
            when(vg.getChildAt(i)).thenReturn(children.get(i));
        }
    }

    public ViewGroup getView() {
        return vg;
    }

    public MockViewGroup withChild(View view) {
        children.add(view);
        updateResponses();
        return this;
    }

    public MockViewGroup withId(int id) {
        when(vg.getId()).thenReturn(id);
        return this;
    }

    public MockViewGroup withTag(String tag) {
        when(vg.getTag()).thenReturn(tag);
        return this;
    }
}

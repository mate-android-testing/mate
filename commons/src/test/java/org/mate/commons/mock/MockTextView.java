package org.mate.commons.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.widget.TextView;

public class MockTextView {
    TextView tv;

    public MockTextView() {
        tv = mock(TextView.class);
    }

    public TextView getView() {
        return tv;
    }

    public MockTextView withId(int id) {
        when(tv.getId()).thenReturn(id);
        return this;
    }

    public MockTextView withContentDescription(String contentDescription) {
        when(tv.getContentDescription()).thenReturn(contentDescription);
        return this;
    }

    public MockTextView withText(String text) {
        when(tv.getText()).thenReturn(text);
        return this;
    }

    public MockTextView withTag(String tag) {
        when(tv.getTag()).thenReturn(tag);
        return this;
    }
}

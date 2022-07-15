package org.mate.commons.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.text.Editable;
import android.widget.EditText;

public class MockEditText {
    EditText et;

    public MockEditText() {
        et = mock(EditText.class);
    }

    public EditText getView() {
        return et;
    }

    public MockEditText withId(int id) {
        when(et.getId()).thenReturn(id);
        return this;
    }

    public MockEditText withContentDescription(String contentDescription) {
        when(et.getContentDescription()).thenReturn(contentDescription);
        return this;
    }

    public MockEditText withText(String text) {
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(text);
        when(et.getText()).thenReturn(editable);
        return this;
    }
}

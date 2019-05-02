package com.mad.poleato;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class LineLimiter implements TextWatcher {
    private boolean _ignore = false; //to avoid infinite loop
    private int changeStart, changeLength;
    private EditText et;
    private int maxlines = 0;


    public void setView(EditText et){
        this.et = et;
    }

    public void setLines(int lines) { this.maxlines = lines; }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if(_ignore)
            return;
        Log.d("matte", "BEFORE :" + this.et.getLineCount());
        Log.d("matte", "MAX :" + this.maxlines);

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(_ignore)
            return;
        Log.d("matte", "AFTER :" + et.getLineCount());
        this.changeStart = start;
        this.changeLength = count;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(_ignore)
            return;

        _ignore = true; //lock

        if(et.getLineCount() > this.maxlines)
        {
            String text = this.et.getText().toString();

            StringBuffer textBuff = new StringBuffer(text);
            textBuff.replace(this.changeStart, this.changeStart+this.changeLength, "");
            String newText = textBuff.toString();

            Log.d("matte", "NEW TEXT :" + newText);
            this.et.setText("");
            this.et.append(newText);
        }

        _ignore = false; //unlock

    }
}

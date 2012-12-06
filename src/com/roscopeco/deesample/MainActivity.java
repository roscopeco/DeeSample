package com.roscopeco.deesample;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.roscopeco.deelang.compiler.Compiler;
import com.roscopeco.deelang.compiler.dex.DexCompilationUnit;
import com.roscopeco.deelang.runtime.dex.CompiledScript;
import com.roscopeco.deelang.runtime.dex.DexBinding;

import dee.lang.Binding;
import dee.lang.Block;
import dee.lang.DeelangInteger;
import dee.lang.DeelangObject;
import dee.lang.DeelangString;

public class MainActivity extends Activity {
  private EditText codeText;
  private TextView activityOutput;
  private Button compileButton;
  private Button runButton;
  
  private Binding binding;

  private Class<? extends CompiledScript> compiledScript;
  
  /**
   * This is a basic Deelang-Java interface class. We'll set
   * this as the 'Activity' variable in our binding, so it
   * can be accessed from Deelang code.
   */
  public final class Activity extends DeelangObject {
    public Activity(Binding binding) {
      super(binding);
    }
    
    public DeelangString aString;

    public void setOutput(DeelangObject str) {
      activityOutput.setText(str.toString());      
    }
    
    public void log(DeelangObject str) {
      Log.i("<deelang>", str.toString());
    }
    
    public void blockTest(Block b, DeelangInteger i) {
      Log.d("ACTIVITY", "Integer is: " + i.getInteger());
      Log.d("ACTIVITY", "Will call block");
      b.invoke();
      Log.d("ACTIVITY", "Back from block");
    }    
  }
  
  public class Self extends DeelangObject {
    public Self(Binding arg0) {
      super(arg0);
    }
    
    public DeelangInteger timesTwo(DeelangInteger i) {
      return new DeelangInteger(getBinding(), i.getInteger() * 2);
    }
  }

  private void setupBinding() {
    DexBinding b = new DexBinding();
    b.setSelf(new Self(b));
    b.setLocal("Activity", new Activity(b));
    binding = b;
  }
  
  private void updateUiStates() {
    compileButton.setEnabled(compiledScript == null);
    runButton.setEnabled(compiledScript != null);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    codeText = (EditText)findViewById(R.id.codeText);
    activityOutput = (TextView)findViewById(R.id.activityOutput);
    compileButton = (Button)findViewById(R.id.compileButton);
    runButton = (Button)findViewById(R.id.runButton);
    
    setupBinding();
    updateUiStates();
    
    compileButton.setOnClickListener(new OnClickListener() {      
      public void onClick(View v) {        
        try {
          Compiler c = new Compiler();
          DexCompilationUnit unit = c.compile(
              new DexCompilationUnit("<EditText.text>", binding), 
              codeText.getText().toString());
          
          compiledScript = unit.getScript();
        } catch (Throwable e) {
          activityOutput.setText("ERROR: " + e.getMessage());
          Log.e("DL", "Exception while compiling", e);
          compiledScript = null;
        } finally {
          updateUiStates();
        }
      }
    });
    
    runButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        try {
          CompiledScript.newInstance(compiledScript, binding).run();
        } catch (Throwable e) {
          activityOutput.setText("ERROR: " + e.getMessage());
          Log.e("DL", "Exception while running", e);
          compiledScript = null;
        } finally {
          updateUiStates();
        }
      }
    });
    
    codeText.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable arg0) { }
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        if (compiledScript != null) {
          compiledScript = null;
          updateUiStates();
        }
      }
    });
  }
}

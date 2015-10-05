package in.vipplaza;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by manish on 10-09-2015.
 */
public class InternetAlertActivity extends Activity {


    TextView txt_DialogTitle,txt_submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_one);

        txt_DialogTitle=(TextView)findViewById(R.id.txt_DialogTitle);
        txt_submit=(TextView)findViewById(R.id.txt_submit);

        txt_DialogTitle.setText(R.string.alert_no_internet);


        txt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}

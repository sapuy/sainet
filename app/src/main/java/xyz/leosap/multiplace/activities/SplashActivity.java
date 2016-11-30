package xyz.leosap.multiplace.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.common.Functions;

public class SplashActivity extends AppCompatActivity {

    TextView tv1,tv2;
    ImageView iv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv1= (TextView) findViewById(R.id.textView);
        tv2= (TextView) findViewById(R.id.textView2);
        iv1= (ImageView) findViewById(R.id.imageView2);
        animacion();
    }

    public void animacion(){
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        anim.setDuration(2000);
        tv1.startAnimation(anim);//aparecemos el primer titulo
        iv1.startAnimation(anim);

        Animation anim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        anim2.setDuration(4000);
        anim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {//Cuando acabe la segunda animación, se abre el activity principal
              Intent intent;
               if(Functions.getPreferences(getApplicationContext()).getBoolean("logued",false))
                intent = new Intent(getApplicationContext(), MainActivity.class);
                else
                   intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        tv2.startAnimation(anim2);//aparecemos el segundo titulo

    }
}

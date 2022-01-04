package com.mxc.jniproject.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mxc.jniproject.R;
import com.mxc.jniproject.ui.callback.DramaBuilder;
import com.mxc.jniproject.ui.callback.DramaObserver;
import com.mxc.jniproject.ui.callback.DramaPredicate;

import java.util.HashMap;
import java.util.Map;

public class CurtainActivity extends AppCompatActivity {

    private Drama<Activity> drama;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Curtain.init(this);
        Curtain.addDramaObserver(new DramaObserver() {
            @Override
            public void didPush(Drama current, Drama previous) {
                Log.e("11111111111111", "didPush current:" + (current != null ? current.url() : "") + "  prev:" + (previous != null ? previous.url() : ""));
            }

            @Override
            public void didPop(Drama current, Drama previous) {
                Log.e("11111111111111", "didPop current:" + (current != null ? current.url() : "") + "  prev:" + (previous != null ? previous.url() : ""));

            }

            @Override
            public void didHide(Drama current) {
                Log.e("11111111111111", "didHide current:" + current.url());

            }

            @Override
            public void didShow(Drama current) {
                Log.e("11111111111111", "didShow current:" + current.url());

            }
        });
        Map<String, DramaBuilder> builders = new HashMap<>();
        builders.put("haha://home/123", new DramaBuilder() {
            @Override
            public DramaContainer.Builder build(String url) {
                DramaContainer.Builder builder = new DramaContainer.Builder();
                builder.root(CurtainActivity.this.findViewById(R.id.drama_root))
                        .view(LayoutInflater.from(CurtainActivity.this).inflate(R.layout.activity_main, null))
                        .url(url);
                return builder;
            }
        });
        Curtain.of(this).registerRouters(builders);
        setContentView(R.layout.acitivity_curtain);
        findViewById(R.id.add_drama).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DramaContainer.Builder builder = new DramaContainer.Builder();
                drama = builder.root(CurtainActivity.this.findViewById(android.R.id.content))
                        .url("11112223")
                        .width(FrameLayout.LayoutParams.MATCH_PARENT)
                        .height(FrameLayout.LayoutParams.WRAP_CONTENT)
                        .inAnim(R.anim.slide_in_top)
                        .outAnim(R.anim.slide_in_bottom)
                        .view(LayoutInflater.from(CurtainActivity.this).inflate(R.layout.activity_media_codec, null))
                        .build();

                Curtain.of(CurtainActivity.this).showAsDropDown(drama,CurtainActivity.this.findViewById(R.id.remove_drama));
            }
        });

        findViewById(R.id.add_drama2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DramaContainer.Builder builder = new DramaContainer.Builder();
                Drama drama = builder.root(CurtainActivity.this.findViewById(android.R.id.content))
                        .view(LayoutInflater.from(CurtainActivity.this).inflate(R.layout.activity_media_codec, null))
                        .url("33333333")
                        .gravity(Gravity.CENTER)
                        .build();

                Curtain.of(CurtainActivity.this).push(drama);
            }
        });

        findViewById(R.id.push_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Curtain.of(CurtainActivity.this).push("haha://home/123?111=222&333=4444");
            }
        });

        findViewById(R.id.hide_drama).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drama != null) {
                    Curtain.of(CurtainActivity.this).hideWithAnim(drama, 0);
                }
            }
        });

        findViewById(R.id.show_drama).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drama != null) {
                    Curtain.of(CurtainActivity.this).showWidthAnim(drama, 0);
                }
            }
        });

        findViewById(R.id.remove_drama).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drama != null) {
                    Curtain.of(CurtainActivity.this).popUtil(new DramaPredicate() {
                        @Override
                        public boolean predicate(Drama drama) {
                            if (drama != null && drama.url() != null) {
                                return drama.url().startsWith("haha://home/123");
                            }
                            return false;
                        }
                    });
                }
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        if (!Curtain.of(this).backPress()) {
//            super.onBackPressed();
//        }
//
//    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.e("1111111111111","dispatchKeyEvent activity"+event.toString());
        return super.dispatchKeyEvent(event);
    }
}

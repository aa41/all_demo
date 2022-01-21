package com.mxc.jniproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mxc.jniproject.media.MediaCodecActivity;
import com.mxc.jniproject.span.SpannableStringGroup;
import com.mxc.jniproject.ui.CurtainActivity;

import java.util.Map;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_media_codec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MediaCodecActivity.class));
            }
        });

        findViewById(R.id.btn_drama).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CurtainActivity.class));

            }
        });

        TextView tv = findViewById(R.id.text1);
        SpannableString string = new SpannableStringGroup().newItem().text("测试").span(new URLSpan("http://www.baidu.com")).buildItem()
                .newItem().text("测试2").span(new BackgroundColorSpan(Color.RED)).span(new UnderlineSpan())
                .buildItem().newItem().text("测试3").buildItem().newItem().text("1111111").span(new ForegroundColorSpan(Color.BLUE))
                .span(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Toast.makeText(MainActivity.this,"1111111111",Toast.LENGTH_SHORT).show();
                    }
                })
                .buildItem().newItem().image(this, R.mipmap.ic_launcher, ImageSpan.ALIGN_BASELINE).text("111").buildItem()
                .newItem().clickNoUnderLine(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this,"222222",Toast.LENGTH_SHORT).show();
                    }
                }).text("点击2222").color(Color.YELLOW).buildItem()
                .build();
        SpannableStringBuilder ssb = new SpannableStringBuilder("123");
        ssb.setSpan(new ImageSpan(this, R.mipmap.ic_launcher, ImageSpan.ALIGN_BASELINE), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        tv.setMovementMethod(LinkMovementMethod.getInstance());
                String htmlStr2 =
                "<span style='color:#EE30A7;font-size:20px'>Html" +
                        "<font color='#000000' size='40px'>字体变大,色值变化0</font>" +
                        "<font color='#000000' size='100px'>字体变大,色值变化1</font>" +
                        "<font color='#EE2C2C' size='40px'>字体变大,色值变化2</font>" +
                        "<font color='#008500' size='50px'>字体变大,色值变化3</font>" +
                        "<font color='#CD8500' size='260px'>字体变大,色值变化4</font>" +
                        "<font color='#CD8500' size='80px'>字体变大,色值变化5</font>" +
                        "</span>";
        SpannableString build = SpannableStringGroup.create().html(htmlStr2, null, new SpannableStringGroup.ReplaceTagHandler() {
            @Override
            public void replaceTags(Map<String, String> tags) {
                tags.put("font","myfont");
            }

            @Override
            public void handleAttributes(String tagName, String attr, String value, SpannableStringGroup.SpannableStringItem item, int startIndex, int endIndex) {
                switch (attr){
                    case "color":
                        item.color(Color.parseColor(value));
                        break;
                    case "size":
                        item.textSize(Integer.parseInt(value.split("px")[0]));
                        break;
                }
            }
        }).build();
        tv.setText(build);


    }


}
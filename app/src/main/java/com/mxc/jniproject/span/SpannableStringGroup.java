package com.mxc.jniproject.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.annotation.NonNull;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpannableStringGroup {
    private List<SpannableStringItem> items = new ArrayList<>();

    public SpannableStringItem newItem() {
        return new SpannableStringItem(this);
    }

    public static SpannableStringGroup create() {
        return new SpannableStringGroup();
    }

    public SpannableString build() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(items, new Comparator<SpannableStringItem>() {
            @Override
            public int compare(SpannableStringItem o1, SpannableStringItem o2) {
                return o1.start - o2.start;
            }
        });
        int index = 0;
        for (SpannableStringItem item : items) {
            if (!TextUtils.isEmpty(item.text)) {
                item.start = index;
                index = index + item.text.length();
                item.end = index;
                sb.append(item.text);
            }
        }
        SpannableString ss = new SpannableString(sb.toString());


        for (SpannableStringItem item : items) {
            List<Object> spans = item.spans;
            for (Object span : spans) {
                ss.setSpan(span, item.start, item.end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return ss;
    }

    public SpannableStringGroup html(String html) {
        return html(html, null, null);
    }

    public SpannableStringGroup html(String html, UrlClickListener listener, ReplaceTagHandler replaceTagHandler) {

        Collection<String> tagSet = null;
        if (replaceTagHandler != null) {
            Map<String, String> tags = new HashMap<>();
            replaceTagHandler.replaceTags(tags);
            Pattern pattern = Pattern.compile("</?[^>]+/?>");
            Matcher matcher = pattern.matcher(html);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    matcher.appendReplacement(sb, matcher.group().replace(entry.getKey(), entry.getValue()));
                }
            }
            matcher.appendTail(sb);
            html = sb.toString();
            tagSet = tags.values();
        }
        Spanned spanned = Html.fromHtml(html, null, new HtmlTagHandler(tagSet, this) {

            @Override
            public void handleAttributes(String tagName, String attr, String value, SpannableStringItem item, int startIndex, int endIndex) {
                if (replaceTagHandler != null) {
                    replaceTagHandler.handleAttributes(tagName, attr, value, item, startIndex, endIndex);
                }
            }
        });
        SpannableStringBuilder ssb = new SpannableStringBuilder(spanned);
        CharacterStyle[] spans = ssb.getSpans(0, ssb.length(), CharacterStyle.class);
        if (spans != null) {
            for (int i = 0; i < spans.length; i++) {
                Object span = spans[i];
                int start = ssb.getSpanStart(span);
                int end = ssb.getSpanEnd(span);
                int flags = ssb.getSpanFlags(span);
                CharSequence text = ssb.subSequence(start, end);
                SpannableStringItem item = newItem();
                item.text = text;
                item.start = start;
                item.end = end;
                if (span instanceof URLSpan) {
                    String url = ((URLSpan) span).getURL();
                    if (!TextUtils.isEmpty(url)) {
                        ClickableNoUnderLineSpan clickableNoUnderLineSpan = new ClickableNoUnderLineSpan(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.click(v, url);
                                }
                            }
                        });
                        item.span(clickableNoUnderLineSpan);
                    }

                } else {
                    item.span(span);
                }
                item.buildItem();
            }
        }
        return this;
    }


    public static class SpannableStringItem {

        private CharSequence text;
        private List<Object> spans = new ArrayList<>();
        private int start;
        private int end;


        private final SpannableStringGroup parent;

        public SpannableStringItem(SpannableStringGroup parent) {
            this.parent = parent;
        }

        public SpannableStringItem text(String text) {
            this.text = text;
            return this;
        }


        public SpannableStringItem backgroundColor(int color) {
            span(new BackgroundColorSpan(color));
            return this;
        }

        public SpannableStringItem color(int color) {
            span(new ForegroundColorSpan(color));
            return this;
        }

        public SpannableStringItem textSize(int textSize) {
            span(new AbsoluteSizeSpan(textSize, false));
            return this;
        }

        public SpannableStringItem typeface(String typeface) {
            span(new TypefaceSpan(typeface));
            return this;
        }

        public SpannableStringItem style(int style) {
            span(new StyleSpan(style));
            return this;
        }


        public SpannableStringItem strikethrough() {
            span(new StrikethroughSpan());
            return this;
        }


        public SpannableStringItem image(Bitmap b) {
            return image(null, b, DynamicDrawableSpan.ALIGN_BOTTOM);

        }


        public SpannableStringItem image(Bitmap b, int verticalAlignment) {
            return image(null, b, verticalAlignment);
        }


        public SpannableStringItem image(Context context, Bitmap bitmap) {
            return image(context, bitmap, DynamicDrawableSpan.ALIGN_BOTTOM);
        }


        public SpannableStringItem image(Context context, Bitmap bitmap, int verticalAlignment) {
            text = " ";
            span(new ImageSpan(context, bitmap, verticalAlignment));
            return this;
        }


        public SpannableStringItem image(Context context, Uri uri) {
            text = " ";
            return image(context, uri, DynamicDrawableSpan.ALIGN_BOTTOM);
        }

        public SpannableStringItem image(Context context, Uri uri, int verticalAlignment) {
            text = " ";
            span(new ImageSpan(context, uri, verticalAlignment));
            return this;
        }

        public SpannableStringItem image(Context context, int resourceId) {
            return image(context, resourceId, DynamicDrawableSpan.ALIGN_BOTTOM);
        }

        public SpannableStringItem image(Context context, int resourceId,
                                         int verticalAlignment) {
            text = " ";
            span(new ImageSpan(context, resourceId, verticalAlignment));
            return this;
        }

        public SpannableStringItem alignment(Layout.Alignment alignment) {
            span(new AlignmentSpan.Standard(alignment));
            return this;
        }

        public SpannableStringItem underLine() {
            span(new UnderlineSpan());
            return this;
        }

        public SpannableStringItem click(View.OnClickListener listener) {
            span(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (listener != null) {
                        listener.onClick(widget);
                    }
                }
            });
            return this;
        }

        public SpannableStringItem clickNoUnderLine(View.OnClickListener listener) {
            span(new ClickableNoUnderLineSpan(listener));
            return this;
        }


        public SpannableStringItem span(Object span) {
            this.spans.add(span);
            return this;
        }

        public SpannableStringGroup buildItem() {
            if (!parent.items.contains(this)) {
                parent.items.add(this);
            }
            return parent;
        }

        private void removeSelf() {
            parent.items.remove(this);
        }

    }


    private static class ClickableNoUnderLineSpan extends ClickableSpan {
        private final View.OnClickListener clickListener;
        private int color;

        private ClickableNoUnderLineSpan(int color, View.OnClickListener clickListener) {
            this.color = color;
            this.clickListener = clickListener;
        }

        private ClickableNoUnderLineSpan(View.OnClickListener clickListener) {
            this(-1, clickListener);
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (clickListener != null) {
                clickListener.onClick(widget);
            }
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            if (color < 0) {
                ds.setColor(ds.linkColor);
            } else {
                ds.setColor(color);
            }
            ds.clearShadowLayer();
            ds.setUnderlineText(false);
            ds.bgColor = Color.TRANSPARENT;
        }
    }

    public interface UrlClickListener {
        void click(View v, String url);
    }

    private abstract static class HtmlTagHandler implements Html.TagHandler {

        private final Collection<String> tagNames;
        private final SpannableStringGroup parent;

        private Stack<Integer> stack = new Stack<>();
        private Stack<Map<String, String>> attrsStack = new Stack<>();

        private HtmlTagHandler(Collection<String> tagNames, SpannableStringGroup parent) {
            this.tagNames = tagNames;
            this.parent = parent;
        }

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tagNames != null) {
                for (String tagName : tagNames) {
                    if (tag.equalsIgnoreCase(tagName)) {
                        if (opening) {
                            stack.push(output.length());
                            attrsStack.push(processAttributes(xmlReader));
                        } else {
                            if (!stack.isEmpty()) {
                                Integer startIndex = stack.pop();
                                int endIndex = output.length();
                                Map<String, String> attributes = attrsStack.pop();
                                SpannableStringItem item = parent.newItem();
                                item.start = startIndex;
                                item.end = endIndex;
                                item.text = output.subSequence(startIndex, endIndex);
                                if (!attributes.isEmpty()) {
                                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                                        String attr = entry.getKey();
                                        String value = entry.getValue();
                                        handleAttributes(tagName, attr, value, item, startIndex, endIndex);
                                        item.buildItem();
                                    }
                                }
                            }

                        }
                    }
                }
            }

        }

        public abstract void handleAttributes(String tagName, String attr, String value, SpannableStringItem item, int startIndex, int endIndex);


        private Map<String, String> processAttributes(final XMLReader xmlReader) {
            Map<String, String> attributes = new HashMap<>();
            try {
                Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
                elementField.setAccessible(true);
                Object element = elementField.get(xmlReader);
                Field attsField = element.getClass().getDeclaredField("theAtts");
                attsField.setAccessible(true);
                Object atts = attsField.get(element);
                Field dataField = atts.getClass().getDeclaredField("data");
                dataField.setAccessible(true);
                String[] data = (String[]) dataField.get(atts);
                Field lengthField = atts.getClass().getDeclaredField("length");
                lengthField.setAccessible(true);
                int len = (Integer) lengthField.get(atts);

                for (int i = 0; i < len; i++)
                    attributes.put(data[i * 5 + 1], data[i * 5 + 4]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return attributes;
        }
    }


    public interface ReplaceTagHandler {

        void replaceTags(Map<String, String> tags);

        void handleAttributes(String tagName, String attr, String value, SpannableStringItem item, int startIndex, int endIndex);


    }

}



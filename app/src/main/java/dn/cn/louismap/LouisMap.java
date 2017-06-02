package dn.cn.louismap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class LouisMap extends View {
    private static final int PARSE_END = 1;
    private Context context;
    private ArrayList<ProvinceItem> itemList = null;
    private int[] colors = new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
    private Paint paint;
    //放大倍数
    private float SCALE_FATOR = 1.3f;
    //被选中的省份
    private ProvinceItem selectedItem = null;

    private GestureDetectorCompat gestureDetectorCompat;
    private int minHeight;
    private int minWidth;
    private int viewWidth;
    private int viewHeight;
    private Rect rect;

    public LouisMap(Context context) {
        this(context, null);
    }

    public LouisMap(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LouisMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        //通过这种方法可以把dp转成px，很方便
        minHeight = context.getResources().getDimensionPixelSize(R.dimen.min_height);
        minWidth = context.getResources().getDimensionPixelSize(R.dimen.min_width);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        loadThread.start();
        gestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                handlerTouch(e.getX(), e.getY());
                return super.onDown(e);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        viewWidth = width;
        viewHeight = height;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                viewWidth = width > minWidth ? width : minWidth;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                viewWidth = minWidth;
                break;
        }
        //这里要计算宽度的比例,等比例拉伸，达到更好的显示效果
        int computeHeight = minHeight * viewWidth / minWidth;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                viewHeight = height;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                viewHeight = minHeight > computeHeight ? minHeight : computeHeight;
                break;
        }
        Log.d("LouisMap", "height:" + height);
        Log.d("LouisMap", "computeHeight:" + computeHeight);
        Log.d("LouisMap", "SCALE_FATOR:" + SCALE_FATOR);
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    /**
     * 处理触摸事件的方法
     * @param x
     * @param y
     */
    private void handlerTouch(float x, float y) {
        if (itemList != null) {
            ProvinceItem temp = null;
            for (ProvinceItem item : itemList) {
                //要除以一个放大系数
                if (item.isTouch((int) (x / SCALE_FATOR), (int) (y / SCALE_FATOR))) {
                    temp = item;
                    break;
                }
            }
            if (temp != null) {
                selectedItem = temp;
                Toast.makeText(context, "You click me OVO", Toast.LENGTH_SHORT).show();
                postInvalidate();
            }
        }
    }

    Thread loadThread = new Thread(new Runnable() {
        @Override
        public void run() {
            parseXMLWithPull();
//            Log.d("LouisMap", "itemList.size():" + itemList.size());
        }
    });

    private void parseXMLWithPull() {
        InputStream inputStream = null;
        try {
            inputStream = context.getResources().openRawResource(R.raw.taiwanhigh);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(inputStream, "UTF-8");
            int eventType = xmlPullParser.getEventType();
            ProvinceItem item = null;
            float left = -1;
            float top = -1;
            float right = -1;
            float bottom = -1;
            RectF rectF = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    //开始解析某个节点
                    case XmlPullParser.START_DOCUMENT:
                        itemList = new ArrayList<>();
                        rectF = new RectF();
                        break;
                    case XmlPullParser.START_TAG:
                        if ("path".equals(nodeName)) {
                            String pathData = xmlPullParser.getAttributeValue("http://schemas.android.com/apk/res/android", "pathData");
                            Path path = PathParser.createPathFromPathData(pathData);
                            assert path != null;
                            path.computeBounds(rectF, true);
                            assert rectF != null;
                            left = left == -1 ? rectF.left : Math.min(rectF.left, left);
                            top = top == -1 ? rectF.top : Math.min(rectF.top, top);
                            right = right == -1 ? rectF.right : Math.max(rectF.right, right);
                            bottom = bottom == -1 ? rectF.bottom : Math.max(rectF.bottom, bottom);

                            item = new ProvinceItem(path);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (nodeName.equalsIgnoreCase("path") && item != null) {
                            assert itemList != null;
                            itemList.add(item);
                            rect = new Rect((int) left, (int)top,(int)right, (int)bottom);
                            item = null;
                        }
                        break;
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            handler.sendEmptyMessage(PARSE_END);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (itemList != null) {
                SCALE_FATOR = (float) viewWidth / rect.width() > (float) viewHeight / rect.height()
                        ? (float) viewHeight / rect.height()
                        :(float) viewWidth / rect.width();
                int totalNum = itemList.size();
                for (int i = 0; i < totalNum; i++) {
                    int flag = i % 4;
                    int color;
                    switch (flag) {
                        case 0:
                            color = colors[0];
                            break;
                        case 1:
                            color = colors[1];
                            break;
                        case 2:
                            color = colors[2];
                            break;
                        default:
                            color = Color.GRAY;
                            break;
                    }
                    //给省份提供颜色
                    itemList.get(i).setDrawColor(color);
                }
                postInvalidate();
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (itemList != null) {
            canvas.save();
            canvas.scale(SCALE_FATOR, SCALE_FATOR);
            for (ProvinceItem item : itemList) {
                //绘制未被选中
                if (item != selectedItem) {
                    item.draw(canvas, paint, false);
                }
            }
            //绘制选择的
            if (selectedItem != null) {
                selectedItem.draw(canvas, paint, true);
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将事件转交
        return gestureDetectorCompat.onTouchEvent(event);
    }
}

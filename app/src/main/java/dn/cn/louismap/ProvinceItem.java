package dn.cn.louismap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * Created by LouisShark on 2017/6/1.
 * this is on dn.cn.louismap.
 * 利用xml解析SVG，封装成JavaBean，一个省
 */

public class ProvinceItem {
    /*
    路径
     */
    private Path path;
    /*
    绘制颜色
     */
    private int drawColor;

    public ProvinceItem(Path path) {
        this.path = path;
    }

    /**
     * 自绘
     * @param canvas
     * @param paint
     * @param isSelect
     */
    public void draw(Canvas canvas, Paint paint, boolean isSelect) {
        if (isSelect) {
            //绘制背景
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(8, 0, 0, 0xffffffff);
            canvas.drawPath(path, paint);

            //绘制省份
            paint.clearShadowLayer();
            paint.setColor(drawColor);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);
            canvas.drawPath(path, paint);
        } else {
            //没有被选择的时候绘制内容
            paint.clearShadowLayer();
            paint.setColor(drawColor);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
            canvas.drawPath(path, paint);

            //选择的时候绘制边界线
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0XFFEEEEEE);
            canvas.drawPath(path, paint);
        }
    }
    /*
    判断是否点击了省份
     */
    public boolean isTouch(int x, int y) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Region region = new Region();
        //setPath 就是用path在region里面剪切出一个区域
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return region.contains(x, y);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }
}

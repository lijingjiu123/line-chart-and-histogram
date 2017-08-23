package cn.lijingjiu.chartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by lijingjiu123 on 2017/4/24.
 */

public class LineChartView extends View {
    public static final int SHOW_TYPE_WEEK = 2, SHOW_TYPE_MONTH = 3, SHOW_TYPE_YEAR = 1;
    private String[] months = {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};
    private String[] week = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private Paint linePaint, curvePaint, bubblePaint, bitmapPaint, pointPaint;
    private TextPaint textPaiant;
    private float textSizeL, textSizeM, textSizeS, textSizeSymbol;
    private float bubbleGap, text2LineGap, lineWidth, curveWidth, pointRadius, symbolRadius;
    private int curveColor, bubbleColor, textColor, backgroundColor;
    private ArrayList<DatePoint> points = null;
    private ArrayList<DatePoint> mPoints = null;
    private PointF touch;
    private boolean isTouch = true;
    private float borderWidth, imgWidth;
    private int showType;
    private int imageId;
    private int unit;
    private PathEffect pe;
    private Bitmap bp;
    private BitmapShader mBitmapShader;
    private Matrix matrix;
    private GregorianCalendar calendar;
    private Rect r;
    private Path path;
    private Path pathDec;
    private Path pathBubble;
    private int width;
    private int height;
    private float w;
    private float h;
    private CornerPathEffect cornerPathEffect;
    private String s1 = "", s2 = "";
    private ArrayList<Point> midPoints = new ArrayList<Point>();
    private ArrayList<Point> midMidPoints = new ArrayList<Point>();
    private ArrayList<Point> controlPoints = new ArrayList<Point>();
    private int spPos = -1;//横坐标不在周或者月的线上，但是是第一个有值的位置，也要显示;
    private int sumPts = 0;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LineChartView);
        curveColor = ta.getColor(R.styleable.LineChartView_lcv_curve_color, Color.WHITE);
        bubbleColor = ta.getColor(R.styleable.LineChartView_lcv_bubble_color, Color.WHITE);
        backgroundColor = ta.getColor(R.styleable.LineChartView_lcv_background_color, Color.TRANSPARENT);
        textSizeL = ta.getDimension(R.styleable.LineChartView_lcv_text_size_l, 10);
        textSizeM = ta.getDimension(R.styleable.LineChartView_lcv_text_size_m, 10);
        textSizeS = ta.getDimension(R.styleable.LineChartView_lcv_text_size_s, 10);
        textSizeSymbol = ta.getDimension(R.styleable.LineChartView_lcv_text_size_symbol, 10);
        bubbleGap = ta.getDimension(R.styleable.LineChartView_lcv_bubble_gap, 10);
        text2LineGap = ta.getDimension(R.styleable.LineChartView_lcv_text_to_line_gap, 10);
        lineWidth = ta.getDimension(R.styleable.LineChartView_lcv_line_width, 2);
        curveWidth = ta.getDimension(R.styleable.LineChartView_lcv_curve_width, 4);
        pointRadius = ta.getDimension(R.styleable.LineChartView_lcv_point_radius, 10);
        symbolRadius = ta.getDimension(R.styleable.LineChartView_lcv_symbol_radius, 10);
        s1 = ta.getString(R.styleable.LineChartView_lcv_text_content1);
        s2 = ta.getString(R.styleable.LineChartView_lcv_text_content2);
        //
        showType = ta.getInteger(R.styleable.LineChartView_lcv_show_type, SHOW_TYPE_WEEK);
        borderWidth = ta.getDimension(R.styleable.LineChartView_lcv_image_border_width, 5);
        imgWidth = ta.getDimension(R.styleable.LineChartView_lcv_image_width, 20);
        imageId = ta.getResourceId(R.styleable.LineChartView_lcv_src, -1);
        ta.recycle();
        init();
    }


    private void init() {
        touch = new PointF(-1, -1);
        linePaint = new Paint();
        curvePaint = new Paint();
        bubblePaint = new Paint();
        textPaiant = new TextPaint();
        //
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //
        curvePaint.setColor(curveColor);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(curveWidth);
        curvePaint.setAntiAlias(true);
        cornerPathEffect = new CornerPathEffect(30);

        //
        bubblePaint.setColor(bubbleColor);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setAntiAlias(true);
        //
        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
        //
        textPaiant.setColor(Color.WHITE);
        textPaiant.setAntiAlias(true);
        //
        pe = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        textColor = Color.CYAN;
        //初始化标准日历
        calendar = new GregorianCalendar();
        r = new Rect();
        path = new Path();
        pathDec = new Path();
        pathBubble = new Path();
        //加载头像图片
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        matrix = new Matrix();
        if (imageId != -1) {
            bp = BitmapFactory.decodeResource(getResources(), imageId).copy(Bitmap.Config.ARGB_8888, true);
            setBitmapShader();
        }
    }


    public void setTypeface(Typeface tf) {
        textPaiant.setTypeface(tf);
        postInvalidate();
    }

    public void setViewColor(int colorId, int textColor, int backColor) {
        this.curveColor = getResources().getColor(colorId);
        this.textColor = getResources().getColor(textColor);
        this.backgroundColor = getResources().getColor(backColor);
        postInvalidate();
    }

    public void setBitmap(Bitmap b) {
        bp = b;
        setBitmapShader();
        postInvalidate();
    }

    public void setData(ArrayList<DatePoint> points) {
        sumPts = 0;
        this.points = null;
        if (points == null || points.size() == 0) return;
        this.points = points;
        int max = getMax(points);
        unit = 10;
        while (unit * 6 < max) {
            unit += 10;
        }

        if (mPoints == null) {
            mPoints = new ArrayList<DatePoint>();
        } else {
            mPoints.clear();
        }
        int sum = 0;
        spPos = -1;
        for (int i = 0; i < points.size(); i++) {
            if (sum == 0) spPos = i;
            int x, y = 0;
            if (i == 0) {
                if (points.get(i).y > -1) {
                    y = points.get(i).y;
                } else if (points.get(i).y == -1) {
                    y = 0;
                } else if (points.get(i).y < -1) {
                    y = Math.abs(points.get(i).y);
                }
                x = i;
                if (y > 0) sum = y;
            } else if (i == points.size() - 1) {
                if (points.get(i).y == -1) {
                    y = sum;
                } else {
                    y = points.get(i).y;
                }
                x = i;
            } else {
                if (points.get(i).y == -1 && sum != 0) {
                    continue;
                } else if (points.get(i).y == -1 && sum == 0) {
                    y = 0;
                    x = i;
                } else {
                    x = i;
                    y = points.get(i).y;
                    sum = y;
                }
            }
            mPoints.add(new DatePoint(x, y, points.get(i).t, points.get(i).s));
            sumPts += y;
        }

//        switch (showType) {
//            case SHOW_TYPE_WEEK:
//
//                setControlPoint(mPoints);
//                break;
//            case SHOW_TYPE_MONTH:
//                break;
//            case SHOW_TYPE_YEAR:
//                break;
//        }
        postInvalidate();
    }

    public void setShowType(int type, ArrayList<DatePoint> points) {
        showType = type;

        setData(points);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calendar.setTime(new Date());//重置画坐标用的日期
        boolean isFind = false;
        boolean changeColor = false;
        width = getWidth();
        height = getHeight();
        w = width / 17F;
        h = height / 8F;
        //画坐标及线
        linePaint.setColor(Color.WHITE);
        linePaint.setAlpha(255);
        canvas.drawLine(3 * w, h * 2 / 3, 3 * w, height - h, linePaint);
        canvas.drawLine(3 * w, height - h, width - 2 * w, height - h, linePaint);
        linePaint.setAlpha(128);
        for (int i = 0; i < 12 / showType; i++) {
            canvas.drawLine(3 * w + (i + 1) * showType * w, h * 2 / 3, 3 * w + (i + 1) * showType * w, height - h, linePaint);
        }
        //画纵坐标数字
        textPaiant.setColor(Color.WHITE);
        textPaiant.setTextSize(textSizeS);
        textPaiant.setAlpha(255);
        int d = unit;
        for (int i = 0; i < 7; i++) {
            String s = String.valueOf(i * d);
            textPaiant.getTextBounds(s, 0, s.length(), r);
            float textX = 3 * w - text2LineGap - r.width();
            float textY = height - (i + 1) * h;
            canvas.drawText(s, textX, textY, textPaiant);
        }
        //画横坐标
        textPaiant.setTextSize(textSizeM);
        drawCoordinate(w, h, width, height, canvas);
        //获取当前显示模式处理好的list


        //画曲线数据
        if (points != null && sumPts > 0) {
            drawCurve(w, h, width, height, canvas);

            if (showType != SHOW_TYPE_YEAR) {

                textPaiant.setTextSize(textSizeM);
                DatePoint tegatP = null;
                for (int i = 0; i < this.points.size(); i++) {
                    DatePoint p = this.points.get(i);
                    pointPaint.setColor(Color.WHITE);
                    //画普通白点
                    boolean type = true;
                    switch (showType) {
                        case SHOW_TYPE_WEEK:
                            type = true;
                            break;
                        case SHOW_TYPE_MONTH:
                            type = i == 0 || i == 7 || i == 14 || i == 21 || i == 28 || (spPos != -1 && i == spPos);
                            break;

                    }

                    if (p.y >= 0 && type) {
                        canvas.drawCircle(getX(p.x), getY(p.y), pointRadius, pointPaint);
                        if (isTouch) {//点击改变颜色
                            if (getX(p.x) - pointRadius * 2 < touch.x && getX(p.x) + pointRadius * 2 > touch.x) {
                                if (touch.y < getY(p.y) + pointRadius * 2 && touch.y > getY(p.y) - pointRadius * 2 && !isFind && p.y >= 0 && i != points.size() - 1) {

                                    drawLineAndValue(w, h, width, height, canvas, p);
                                    canvas.drawCircle(getX(p.x), getY(p.y), pointRadius * 2, pointPaint);
                                    pointPaint.setColor(bubbleColor);
                                    canvas.drawCircle(getX(p.x), getY(p.y), pointRadius, pointPaint);
                                    tegatP = p;
                                    isFind = true;
                                } else if (i == points.size() - 1 && touch.y < getY(mPoints.get(mPoints.size() - 1).y) + imgWidth / 2 && touch.y > getY(mPoints.get(mPoints.size() - 1).y) - imgWidth / 2) {
                                    drawBubble(canvas, w, h, width, height, imgWidth / 2, mPoints.get(mPoints.size() - 1));
                                    changeColor = true;
                                } else {
                                    isFind = false;
                                }
                            }
                        }
                    }
                }
                if (isTouch && isFind)
                    drawBubble(canvas, w, h, width, height, pointRadius * 2, tegatP);
            }
            if (!isFind)
                drawLineAndValue(w, h, width, height, canvas, mPoints.get(mPoints.size() - 1));

            float yStart = getY(mPoints.get(mPoints.size() - 1).y);
            float xEnd = getX(mPoints.get(mPoints.size() - 1).x);
            //画头像
            if (mBitmapShader != null) {
                if (changeColor) {
                    linePaint.setColor(bubbleColor);
                    linePaint.setAlpha(255);
                }
                float radius = imgWidth / 2 - borderWidth;
                canvas.save();
                canvas.translate(xEnd - radius, yStart - radius);
                canvas.drawCircle(radius, radius, radius + borderWidth, linePaint);
                canvas.drawCircle(radius, radius, radius, bitmapPaint);
                canvas.restore();
            }

            isTouch = false;
        }
    }

    private void drawLineAndValue(float w, float h, int width, int height, Canvas canvas, DatePoint p) {
        //画数值、虚线
        textPaiant.setTextSize(textSizeM);
        float yStart = height - h - p.y * h / unit;
        float xStart = 3 * w;
        float xEnd = getX(p.x);
        linePaint.setPathEffect(pe);
        path.reset();
        path.moveTo(xStart, yStart);
        path.lineTo(xEnd, yStart);
        canvas.drawPath(path, linePaint);
        linePaint.setPathEffect(null);
        //数值和气泡
        path.reset();
        bubblePaint.setColor(Color.WHITE);
        path.moveTo(xStart + bubbleGap / 2, yStart);
        path.lineTo(xStart - 1, yStart - bubbleGap / 2);
        path.lineTo(xStart - 1, yStart - h / 4);
        path.lineTo(xStart - 2 * w, yStart - h / 4);
        path.lineTo(xStart - 2 * w, yStart + h / 4);
        path.lineTo(xStart - 1, yStart + h / 4);
        path.lineTo(xStart - 1, yStart + bubbleGap / 2);
        path.lineTo(xStart + bubbleGap / 2, yStart);
        canvas.drawPath(path, bubblePaint);
        //
        String num = String.valueOf(p.y);
        textPaiant.getTextBounds(num, 0, num.length(), r);

        textPaiant.setColor(textColor);
        canvas.drawText(num, xStart - w - r.width() / 2f, yStart + r.height() / 2f, textPaiant);
    }

    private void drawCurve(float w, float h, int width, int height, Canvas canvas) {


        path.reset();
        pathDec.reset();

        path.moveTo(getX(mPoints.get(0).x), getY(mPoints.get(0).y));
        for (int i = 1; i < mPoints.size(); i++) {
            path.lineTo(getX(mPoints.get(i).x), getY(mPoints.get(i).y));
            if (mPoints.get(i).y < mPoints.get(i - 1).y) {
                pathDec.moveTo(getX(mPoints.get(i - 1).x), getY(mPoints.get(i - 1).y));
                pathDec.lineTo(getX(mPoints.get(i).x), getY(mPoints.get(i).y));
                float xStart = (getX(mPoints.get(i).x) + getX(mPoints.get(i - 1).x)) / 2;
                float yStart = getY(mPoints.get(i - 1).y);
                drawSymbolAndBubble(w, h, width, height, xStart, yStart, canvas);
            }

        }
        curvePaint.setStrokeWidth(curveWidth);
        curvePaint.setColor(curveColor);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setPathEffect(cornerPathEffect);
        canvas.drawPath(path, curvePaint);
        //画下降蓝线
        curvePaint.setColor(bubbleColor);
        canvas.drawPath(pathDec, curvePaint);
        //画背景
        path.lineTo(getX(mPoints.get(mPoints.size() - 1).x), height - h);
        path.lineTo(getX(mPoints.get(0).x), height - h);
        path.lineTo(getX(mPoints.get(0).x), getY(mPoints.get(0).y));
        curvePaint.setColor(backgroundColor);
        curvePaint.setStyle(Paint.Style.FILL);
        curvePaint.setPathEffect(null);
        canvas.drawPath(path, curvePaint);

    }

    private void drawSymbolAndBubble(float w, float h, int width, int height, float xStart, float yStart, Canvas canvas) {
        curvePaint.setStrokeWidth(curveWidth / 2);
        curvePaint.setColor(bubbleColor);
        curvePaint.setStyle(Paint.Style.STROKE);
        float yCenter = yStart - bubbleGap - symbolRadius;
        canvas.drawCircle(xStart, yCenter, symbolRadius, curvePaint);
        String s = "!";
        textPaiant.setTextSize(textSizeSymbol);
        textPaiant.setColor(bubbleColor);
        textPaiant.getTextBounds(s, 0, s.length(), r);
        canvas.drawText(s, xStart - r.width(), yCenter + r.height() / 2f, textPaiant);
        if (isTouch && touch.y > yCenter - symbolRadius && touch.y < yCenter + symbolRadius) {
            if (touch.x > xStart - symbolRadius && touch.x < xStart + symbolRadius) {
                pathBubble.reset();
                pathBubble.moveTo(xStart - symbolRadius, yCenter);
                pathBubble.lineTo(xStart - symbolRadius - bubbleGap - 8 * w, yCenter);
                pathBubble.lineTo(xStart - symbolRadius - bubbleGap - 8 * w, yStart - 2 * h);
                pathBubble.lineTo(xStart - symbolRadius - bubbleGap, yStart - 2 * h);
                pathBubble.lineTo(xStart - symbolRadius - bubbleGap, yCenter - bubbleGap);
                pathBubble.lineTo(xStart - symbolRadius, yCenter);
                bubblePaint.setColor(bubbleColor);
                canvas.drawPath(pathBubble, bubblePaint);
                textPaiant.setTextSize(textSizeL);
                textPaiant.setColor(Color.WHITE);
                textPaiant.getTextBounds(s1, 0, s1.length(), r);
                canvas.drawText(s1, xStart - symbolRadius - 8 * w, yStart - 2 * h + bubbleGap + r.height(), textPaiant);
                textPaiant.setTextSize(textSizeM);
                textPaiant.setAlpha(192);
                String[] str = s2.split("\n");
                textPaiant.getTextBounds(str[0], 0, str[0].length(), r);
                canvas.drawText(str[0], xStart - symbolRadius - 8 * w, yCenter - bubbleGap * 3 / 2f - r.height(), textPaiant);
                if (str.length == 2)canvas.drawText(str[1], xStart - symbolRadius - 8 * w, yCenter - bubbleGap, textPaiant);
                textPaiant.setAlpha(255);

            }
        }
    }

    /**
     * 画三种显示模式的横坐标
     *
     * @param w
     * @param h
     * @param width
     * @param height
     * @param canvas
     */
    private void drawCoordinate(float w, float h, int width, int height, Canvas canvas) {
        int year = -1;
        if (showType == SHOW_TYPE_YEAR) calendar.add(Calendar.MONTH, 1);
        for (int i = 12 / showType; i >= 0; i--) {
            switch (showType) {
                case SHOW_TYPE_WEEK:
                    int we = calendar.get(Calendar.DAY_OF_WEEK);
                    textPaiant.getTextBounds(week[we - 1], 0, week[we - 1].length(), r);
                    canvas.drawText(week[we - 1], 3 * w + w * i * showType - r.width() / 2f, height - h + r.height() + text2LineGap, textPaiant);
                    calendar.add(Calendar.DATE, -1);
                    break;
                case SHOW_TYPE_MONTH:
                    String d = calendar.get(Calendar.DAY_OF_MONTH) + "." + String.valueOf(calendar.get(Calendar.MONTH) + 1) + ".";
                    textPaiant.getTextBounds(d, 0, d.length(), r);
                    canvas.drawText(d, 3 * w + w * i * showType - r.width() / 2f, height - h + r.height() + text2LineGap, textPaiant);
                    calendar.add(Calendar.DATE, -7);
                    break;
                case SHOW_TYPE_YEAR:
                    int m = calendar.get(Calendar.MONTH);
                    int y = calendar.get(Calendar.YEAR);
                    textPaiant.getTextBounds(months[m], 0, months[m].length(), r);
                    canvas.drawText(months[m], 7 * w / 2 + w * i * showType - r.width() / 2f, height - h + r.height() + text2LineGap, textPaiant);
                    calendar.add(Calendar.MONTH, -1);
                    if (y < year && year != -1) {
                        canvas.drawLine(4 * w + w * i * showType, height - h, 4 * w + w * i * showType, height - h + r.height() + text2LineGap, linePaint);
                    }
                    year = y;
                    break;
            }
        }
    }

    private void drawBubble(Canvas canvas, float w, float h, int width, int height, float gap, DatePoint p) {
        path.reset();
        float xStart = getX(p.x);
        float yStart = getY(p.y) - gap;
        float textX = xStart - w * 5f / 2f;
        float textY = yStart - bubbleGap - h * 5 / 8;

        if (xStart <= 5 * w + 1) {
            path.moveTo(xStart, yStart);
            path.lineTo(xStart, yStart - bubbleGap - h * 5 / 4);
            path.lineTo(xStart + 5 * w + 1, yStart - bubbleGap - h * 5 / 4);
            path.lineTo(xStart + 5 * w + 1, yStart - bubbleGap);
            path.lineTo(xStart + bubbleGap, yStart - bubbleGap);
            path.lineTo(xStart, yStart);
            //字坐标
            textX = xStart + w * 5f / 2f;
        } else {
            path.moveTo(xStart, yStart);
            path.lineTo(xStart, yStart - bubbleGap - h * 5 / 4);
            path.lineTo(xStart - 5 * w + 1, yStart - bubbleGap - h * 5 / 4);
            path.lineTo(xStart - 5 * w + 1, yStart - bubbleGap);
            path.lineTo(xStart - bubbleGap, yStart - bubbleGap);
            path.lineTo(xStart, yStart);
            //字坐标
            textX = xStart - w * 5f / 2f;
        }
        bubblePaint.setColor(bubbleColor);
        canvas.drawPath(path, bubblePaint);
        String s = null;
        if (p.s > 0) {
            s = "+" + String.valueOf(p.s) + "pts";
        } else {
            s = String.valueOf(p.s) + "pts";
        }
        textPaiant.getTextBounds(s, 0, s.length(), r);
        textPaiant.setColor(Color.WHITE);
        textPaiant.setTextSize(textSizeL);
        canvas.drawText(s, textX - r.width() * 3f / 4, textY - bubbleGap / 2, textPaiant);
        textPaiant.setAlpha(192);
        textPaiant.setTextSize(textSizeM);
        String time = null;
        if (p.x != points.size() - 1) {
            /*time = d + "." + m + "." + y;*/
            time = TimeFormatUtils.getPtsTime(p.t);
        } else {
            time = "today";
        }
        if (time != null) textPaiant.getTextBounds(time, 0, time.length(), r);
        canvas.drawText(time, textX - r.width() / 2f, textY + bubbleGap / 2 + r.height(), textPaiant);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (MotionEvent.ACTION_DOWN == action) {
            if (!isTouch) {
                isTouch = true;
                touch.set(event.getX(), event.getY());
                postInvalidate();
            }
        }
        return super.onTouchEvent(event);
    }

    private void setBitmapShader() {
        if (bp == null) return;
        mBitmapShader = new BitmapShader(bp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = 1.0f;
        // 拿到bitmap宽或高的小值
        int bSize = Math.min(bp.getWidth(), bp.getHeight());
        scale = (imgWidth - borderWidth * 2) * 1.0f / bSize;

        // shader的变换矩阵，我们这里主要用于放大或者缩小
        matrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(matrix);
        // 设置shader
        bitmapPaint.setShader(mBitmapShader);

    }


    private int getMax(ArrayList<DatePoint> points) {
        int max = Math.abs(points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        return max;
    }

    private int getLastPosition(ArrayList<Point> points) {
        int pos = points.size() - 1;
        while (pos >= 0 && points.get(pos).y == 0) {
            pos--;
        }
        return pos;
    }

    private float getX(float x) {
        float value = 0;
        switch (showType) {
            case SHOW_TYPE_WEEK:
                value = 3 * w + showType * w * x;
                break;
            case SHOW_TYPE_MONTH:
                value = 3 * w + showType * w * 4 * x / 28;
                break;
            case SHOW_TYPE_YEAR:
                value = 3 * w + showType * w * 12 * x / 364;
                break;
        }
        return value;
    }

    private float getY(float y) {
        return height - h - y * h / unit;
    }

    private void setControlPoint(ArrayList<DatePoint> list) {

        //算中点
        midPoints.clear();
        loop1:
        for (int i = 0; i < list.size(); i++) {
            Point midPoint = null;
            if (i == list.size() - 1) {
                break loop1;
            } else {
                midPoint = new Point((list.get(i).x + list.get(i + 1).x) / 2, (list.get(i).y + list.get(i + 1).y) / 2);
            }
            midPoints.add(midPoint);
        }
        //算中点的中点
        midMidPoints.clear();
        loop2:
        for (int i = 0; i < midPoints.size(); i++) {
            Point midMidPoint = null;
            if (i == midPoints.size() - 1) {
                break loop2;
            } else {
                midMidPoint = new Point((midPoints.get(i).x + midPoints.get(i + 1).x) / 2, (midPoints.get(i).y + midPoints.get(i + 1).y) / 2);
            }
            midMidPoints.add(midMidPoint);
        }
        //算控制点
        controlPoints.clear();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0 || i == list.size() - 1) {
                continue;
            } else {
                Point before = new Point();
                Point after = new Point();
                before.x = list.get(i).x - midMidPoints.get(i - 1).x + midPoints.get(i - 1).x;
                before.y = list.get(i).y - midMidPoints.get(i - 1).y + midPoints.get(i - 1).y;
                after.x = list.get(i).x - midMidPoints.get(i - 1).x + midPoints.get(i).x;
                after.y = list.get(i).y - midMidPoints.get(i - 1).y + midPoints.get(i).y;
                controlPoints.add(before);
                controlPoints.add(after);
            }
        }
    }

}

package cn.lijingjiu.chartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by lijingjiu123 on 2017/4/18.
 */

public class BarGraphView extends View {
    private String[] months = {"J","F","M","A","M","J","J","A","S","O","N","D"};
    private String[] monthsF = {"January ","February","March","April","May","June","July","August","September","October","November","December"};
    private  String[] days = {"1st","2nd","3rd","4th","5th","6th","7th","8th","9th","10th","11th","12th","13h","14th","15th","16th","17th","18th","19th","20th","21st","22nd","23rd","24th","25th","26th","27th","28th","29th","30th","31st"};
    private Paint linePaint,barPaint,bubblePaint,bitmapPaint;
    private TextPaint textPaiant;
    private float textSizeL = 10,textSizeM = 10,textSizeS = 10;
    private float bubbleGap,text2LineGap,lineWidth;
    private int barColor,bubbleColor,textColor;
    private ArrayList<Point> points = null;
    private PointF touch;
    private boolean isTouch = false, hideImage = true;
    private float borderWidth,imgWidth;
    private int imageId;
    private int unit;
    private PathEffect pe;
    private Bitmap bp;
    private BitmapShader mBitmapShader;
    private Matrix matrix;
    private Rect r;
    private Path path;

    public BarGraphView(Context context) {
        this(context,null);
    }

    public BarGraphView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BarGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BarGraphView);
        barColor = ta.getColor(R.styleable.BarGraphView_bgv_bar_color, Color.WHITE);
        bubbleColor = ta.getColor(R.styleable.BarGraphView_bgv_bubble_color, Color.WHITE);
        textSizeL = ta.getDimension(R.styleable.BarGraphView_bgv_text_size_l,10);
        textSizeM = ta.getDimension(R.styleable.BarGraphView_bgv_text_size_m,10);
        textSizeS = ta.getDimension(R.styleable.BarGraphView_bgv_text_size_s,10);
        bubbleGap = ta.getDimension(R.styleable.BarGraphView_bgv_bubble_gap,10);
        text2LineGap = ta.getDimension(R.styleable.BarGraphView_bgv_text_to_line_gap,10);
        lineWidth = ta.getDimension(R.styleable.BarGraphView_bgv_line_width,2);
        //
        hideImage = ta.getBoolean(R.styleable.BarGraphView_bgv_hide_image,true);
        borderWidth = ta.getDimension(R.styleable.BarGraphView_bgv_image_border_width,5);
        imgWidth = ta.getDimension(R.styleable.BarGraphView_bgv_image_width,20);
        imageId = ta.getResourceId(R.styleable.BarGraphView_bgv_src,-1);
        ta.recycle();
         init();
    }

    private void init() {
        touch = new PointF(-1,-1);
        linePaint = new Paint();
        barPaint = new Paint();
        bubblePaint = new Paint();
        textPaiant = new TextPaint();
        //
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //
        barPaint.setColor(barColor);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setAntiAlias(true);
        //
        bubblePaint.setColor(bubbleColor);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setAntiAlias(true);
        //
        textPaiant.setColor(Color.WHITE);
        textPaiant.setAntiAlias(true);
        //
        pe = new DashPathEffect(new float[]{5,5,5,5},1);
        textColor =Color.CYAN;
        //
        r = new Rect();
        path = new Path();
        //加载头像图片 Head picture
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        matrix = new Matrix();
        if (imageId != -1){
            bp = BitmapFactory.decodeResource(getResources(),imageId).copy(Bitmap.Config.ARGB_8888, true);
            setBitmapShader();
        }
    }



    public void  setTypeface(Typeface tf){
        textPaiant.setTypeface(tf);
        postInvalidate();
    }
    public  void setBarColor(int colorId){
        barColor = getResources().getColor(colorId);
        postInvalidate();
    }

    public  void setBarColor(int colorId,int textColor){
        barColor = getResources().getColor(colorId);
        this.textColor = getResources().getColor(textColor);
        postInvalidate();
    }
    public void setBitmap(Bitmap b){
        bp = b;
        setBitmapShader();
        postInvalidate();
    }
    public void setData(ArrayList<Point> points){
        this.points = points;
        if (!hideImage){//根据最大体重设置横坐标
            int max = getMax(points);
            unit = 10;
            while (unit*6 < max){
                unit += 10;
            }
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth(); int height = getHeight();
        float w = width / 17F; float h = height / 8F;
        //画坐标及线 Draw coordinates and lines
        linePaint.setAlpha(255);
        canvas.drawLine( 3*w, h*2/3, 3*w, height-h, linePaint);
        canvas.drawLine( 3*w, height-h, width-2*w, height-h, linePaint);
        linePaint.setAlpha(128);
        for (int i = 0; i < 12; i++){
            canvas.drawLine( (4+i)*w, h*2/3, (4+i)*w, height-h, linePaint);
        }
        //画纵坐标数字 Draw the ordinate number

        textPaiant.setColor(Color.WHITE);
        textPaiant.setTextSize(textSizeS);
        textPaiant.setAlpha(255);
        int d = 20;
        if (!hideImage){//自定义间隔大小 Customize the size of the interval
            d = unit;
        }
        for (int i = 0; i < 7; i++){
            String s = String.valueOf(i*d);
            textPaiant.getTextBounds(s,0,s.length(),r);
            float textX = 3*w - text2LineGap - r.width();
            float textY = height - (i+1)*h;
            canvas.drawText(s,textX,textY,textPaiant);

        }
        //画横坐标及数据 Draw the abscissa and data
        if(points != null){
            textPaiant.setTextSize(textSizeM);
            int year = -1;
            boolean isFind = false;//是否找到点击的条 Whether to find the click item
            for (int i= 0; i < points.size(); i++){
                Point p = points.get(i);
                int y = p.x/10000;//年 year
                int m = p.x%10000/100;//月 month
                //画横坐标月份 Draw the abscissa month
                textPaiant.getTextBounds(months[m-1],0,months[m-1].length(),r);
                canvas.drawText(months[m-1],w*(7f/2+i)-r.width()/2f, height-h+ r.height()+text2LineGap, textPaiant);
                //画年分隔线 Draw the dividing line between year and year
                if (y > year && year != -1){
                    canvas.drawLine( (3+i)*w, height-h, (3+i)*w,  height-h+ r.height()+text2LineGap, linePaint);
                }
                year = y;
                //画柱状图 Draw a histogram
                if (p.y != 0){
                    barPaint.setColor(barColor);
                    if (hideImage){
                        canvas.drawRect((3+i)*w+1,(140-p.y)/20f*h ,(4+i)*w-1, height-h-1 ,barPaint);
                    }else {
                        canvas.drawRect((3+i)*w+1,height-h-p.y*h/unit ,(4+i)*w-1, height-h-1 ,barPaint);
                    }
                    if (isTouch){//点击改变颜色 Click to change the color

                        if ((4+i)*w > touch.x &&(3+i)*w < touch.x && !isFind){
                            if (touch.y < height-h && touch.y >(140-p.y)/20f*h){
                                barPaint.setColor(Color.WHITE);
                                canvas.drawRect((3+i)*w+1,(140-p.y)/20f*h ,(4+i)*w-1, height-h-1 ,barPaint);
                                drawBubble(canvas,w,h,width,height,i,p);
                                isFind = true;
                            }
                        }

                    }
                }
            }
            isTouch = false;
            //画数值、虚线和头像 Drawing values, dashed lines and image
            if (!hideImage){
                int pos = getLastPosition();
                if (pos < 0) return;//数据全为0，不画 The data is all 0, not painted
                float yStar = height-h-points.get(pos).y*h/unit;
                float xStart = 3*w;
                float xEnd = (4+pos)*w;
                linePaint.setPathEffect(pe);
                path.reset();
                path.moveTo(xStart,yStar);
                path.lineTo(xEnd,yStar);
                canvas.drawPath(path,linePaint);
                linePaint.setPathEffect(null);
                //数值和气泡 Values and bubbles
                path.reset();
                bubblePaint.setColor(Color.WHITE);
                path.moveTo(xStart+bubbleGap/2,yStar);
                path.lineTo(xStart-1,yStar-bubbleGap/2);
                path.lineTo(xStart-1,yStar-h/4);
                path.lineTo(xStart-2*w,yStar-h/4);
                path.lineTo(xStart-2*w,yStar+h/4);
                path.lineTo(xStart-1,yStar+h/4);
                path.lineTo(xStart-1,yStar+bubbleGap/2);
                path.lineTo(xStart+bubbleGap/2,yStar);
                canvas.drawPath(path,bubblePaint);
                //
                String num =String.valueOf(points.get(pos).y);
                textPaiant.getTextBounds(num,0,num.length(),r);
                textPaiant.setColor(textColor);
                canvas.drawText(num,xStart-w-r.width()/2f,yStar+r.height()/2f,textPaiant);
                //画头像 draw image
                if (mBitmapShader != null){

                    float radius = imgWidth/2 - borderWidth;
                    canvas.save();
                    canvas.translate(xEnd-w/2-radius, yStar-radius);
                    canvas.drawCircle(radius,radius,radius+borderWidth,linePaint);
                    canvas.drawCircle(radius,radius,radius,bitmapPaint);
                    canvas.restore();
                }
            }
        }
    }

    private void drawBubble(Canvas canvas, float w, float h, int width, int height,int i,Point p) {
        path.reset();
        float xStart = (4+i)*w-1;
        float yStart = (140-p.y)/20f*h;
        float textX = xStart - w*5f/2f;
        float textY = yStart - bubbleGap - h*2/3;
        int m = p.x%10000/100;//月 month
        int d= p.x%10000%100;//日 day

        if (i == 0){
            path.moveTo(xStart,yStart);
            path.lineTo(xStart,yStart-bubbleGap);
            path.lineTo(xStart+w,yStart-bubbleGap);
            path.lineTo(xStart+w,yStart-bubbleGap-h*4/3);
            path.lineTo(xStart-4*w+1,yStart-bubbleGap-h*4/3);
            path.lineTo(xStart-4*w+1,yStart-bubbleGap);
            path.lineTo(xStart-bubbleGap,yStart-bubbleGap);
            path.lineTo(xStart,yStart);
            //字坐标 The coordinates of the word
            textX = xStart - w*3f/2f;
        }else{
            path.moveTo(xStart,yStart);
            path.lineTo(xStart,yStart-bubbleGap-h*4/3);
            path.lineTo(xStart-5*w+1,yStart-bubbleGap-h*4/3);
            path.lineTo(xStart-5*w+1,yStart-bubbleGap);
            path.lineTo(xStart-bubbleGap,yStart-bubbleGap);
            path.lineTo(xStart,yStart);
            //字坐标 The coordinates of the word
            textX = xStart - w*5f/2f;
        }
        canvas.drawPath(path,bubblePaint);
        String s = String.valueOf(p.y)+"pts";
        textPaiant.getTextBounds(s,0,s.length(),r);
        textPaiant.setTextSize(textSizeL);
        canvas.drawText(s,textX-r.width()*3f/4,textY-bubbleGap/2,textPaiant);
        textPaiant.setAlpha(192);
        textPaiant.setTextSize(textSizeM);
        String time = monthsF[m-1]+" "+days[d-1];
        textPaiant.getTextBounds(time,0,time.length(),r);
        canvas.drawText(time,textX-r.width()/2f,textY+bubbleGap/2+r.height(),textPaiant);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("ljj","event:"+event.toString());
        int action = event.getAction();
        if (MotionEvent.ACTION_DOWN== action && hideImage){
            if (!isTouch){
                isTouch = true;
                touch.set(event.getX(),event.getY());
                postInvalidate();
            }
        }
        return super.onTouchEvent(event);
    }

    private void setBitmapShader() {
        if (bp == null) return;
        mBitmapShader = new BitmapShader(bp, Shader.TileMode.CLAMP ,Shader.TileMode.CLAMP);
        float scale = 1.0f;
        // 拿到bitmap宽或高的小值
        int bSize = Math.min(bp.getWidth(), bp.getHeight());
        scale = (imgWidth-borderWidth*2) * 1.0f / bSize;

        // shader的变换矩阵，我们这里主要用于放大或者缩小
        matrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(matrix);
        // 设置shader
        bitmapPaint.setShader(mBitmapShader);

    }



    private int getMax(ArrayList<Point> points){
        int max= points.get(0).y;
        for(int i=1;i < points.size(); i++){
            if(points.get(i).y > max){
                max=points.get(i).y;
            }
        }
        return max;
    }

    private int getLastPosition(){
        int pos = points.size() - 1;
        while(pos >= 0 && points.get(pos).y == 0){
            pos --;
        }
        return pos;
    }
}

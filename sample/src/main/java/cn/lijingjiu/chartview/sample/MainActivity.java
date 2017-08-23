package cn.lijingjiu.chartview.sample;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Random;

import cn.lijingjiu.chartview.BarGraphView;
import cn.lijingjiu.chartview.DatePoint;
import cn.lijingjiu.chartview.LineChartView;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener{
    private LineChartView lcv;
    private BarGraphView bgv,bgv2;
    private RadioGroup rg;
    private ArrayList<DatePoint> pointsWeek,pointsMonth,pointsYear;
    private ArrayList<Point> pointsBGV = new ArrayList<Point>(12);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setView();
        setData();
    }

    private void setView() {
        lcv = (LineChartView) findViewById(R.id.lcv);
        bgv = (BarGraphView) findViewById(R.id.bgv);
        bgv2 = (BarGraphView) findViewById(R.id.bgv2);
        rg = (RadioGroup) findViewById(R.id.rg);
        rg.setOnCheckedChangeListener(this);
    }

    private void setData() {
        /*
         LineChartView数据格式说明:
            位置一：序号；
            位置二：纵坐标的值，正数显示点且可点击，-1不显示点不可点击，如果第一个数据为负，数值取其绝对值且不显示点不可点击；
            位置三：点击后在需要显示的数据时间；
            位置四：点击后在需要显示的数据值。
         LineChartView Data Format Description:
        Position one: serial number;
     Position two: the ordinate value, positive number of points and can click, -1 does not show the point can not click, if the first data is negative, the value of its absolute value and not show points can not click;
     Position three: click on the need to display the data time;
     Position four: click on the data values that need to be displayed.
         */
        //week data
        pointsWeek = new ArrayList<>(7);
        pointsWeek.add(new DatePoint(0,-20,"20170426",0));
        pointsWeek.add(new DatePoint(1,30,"20170427",10));
        pointsWeek.add(new DatePoint(2,-1,"20170428",0));
        pointsWeek.add(new DatePoint(3,50,"20170429",20));
        pointsWeek.add(new DatePoint(4,80,"20170430",30));
        pointsWeek.add(new DatePoint(5,90,"20170501",20));
        pointsWeek.add(new DatePoint(6,80,"20170502",-20));
        lcv.setShowType(LineChartView.SHOW_TYPE_WEEK,pointsWeek);
        //month data
        pointsMonth = new ArrayList<DatePoint>(29);
        Random random = new Random();
        int sum = 0;
        for (int i = 0; i < 29; i++){
            int s = random.nextInt(5);
            sum += s;
            pointsMonth.add(new DatePoint(i,sum,String.valueOf(20170401+i),s));
        }
        //year data
        pointsYear = new ArrayList<DatePoint>(365);
        sum = 0;
        for (int i = 0; i < 365; i++){
            int s = random.nextInt(2);
            sum += s;
            pointsYear.add(new DatePoint(i,sum,String.valueOf(20170515),s));
        }
        lcv.setShowType(LineChartView.SHOW_TYPE_WEEK,pointsWeek);
        /*
         BarGraphView 数据格式说明：
            位置一：数据时间（横轴）；
            位置二：数据的值（竖轴）。
         BarGraphView Data Format Description:
        Position one: data time (horizontal axis);
        Position two: the value of the data (vertical axis).
         */
        pointsBGV.add(new Point(20160319,80));
        pointsBGV.add(new Point(20160419,20));
        for (int i = 1; i< 9;i++){
            pointsBGV.add(new Point(20160019+(i+4)*100,0));
        }
        pointsBGV.add(new Point(20170119,100));
        pointsBGV.add(new Point(20170219,0));
        //
        bgv.setData(pointsBGV);
        bgv2.setData(pointsBGV);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId){
            case R.id.radioButton1:
                lcv.setShowType(LineChartView.SHOW_TYPE_WEEK,pointsWeek);
                break;
            case R.id.radioButton2:
                lcv.setShowType(LineChartView.SHOW_TYPE_MONTH,pointsMonth);
                break;
            case R.id.radioButton3:
                lcv.setShowType(LineChartView.SHOW_TYPE_YEAR,pointsYear);
                break;
        }
    }
}

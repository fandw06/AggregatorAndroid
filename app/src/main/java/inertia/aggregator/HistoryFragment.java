package inertia.aggregator;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import inertia.http.HttpPackage;
import inertia.http.HttpRequestTask;

/**
 * HistoryFragment is used to implement the function of display
 * history data in a graph view. The static history data graph could be scrolled and
 * zoomed.
 *
 * @author David
 *
 */
public class HistoryFragment extends Fragment implements OnTouchListener{
	
	private AggregatorService mService;
	private TextView tUpload;
	private View vCloud;
	
	private boolean firstPlot;
	
    private XYPlot historyPlot;
    private List<Number> historyData= new LinkedList<Number>();
    private SimpleXYSeries historySeries;

    private final String url = "http://inertia.ece.virginia.edu/assist_web/api/v1/data/";
    private static String TAG = "CloudFragment";

    private PointF minXY;
    private PointF maxXY;
    // Definition of the touch states
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;
    PointF firstFinger;
    float lastScrollingX;
    float lastScrollingY;
    float distBetweenFingers;
    float lastZooming;
    
	/**
	 * Messages used by mLogHandler. There is a copy in AggregatorService.java.
	 * @see inertia.aggregator.AggregatorService
	 */
	private static final int MESSAGE_LOG_ECG                    = 2;
	private static final int MESSAGE_LOG_OZONE                  = 3;
    
    private List<Integer> ecgDataLog = new ArrayList<Integer>();    
    private List<Integer> ozoneDataLog = new ArrayList<Integer>();
    
    @SuppressLint("HandlerLeak")
   	private Handler mLogHandler = new Handler(){
       	@Override  
        public void handleMessage(Message msg) {
           	switch(msg.what){
   			case MESSAGE_LOG_ECG:
                ecgDataLog.clear();
                for(int s : (int [])msg.obj)
   				    ecgDataLog.add(s);
                uploadData();
   				break;
   			case MESSAGE_LOG_OZONE:
   				ozoneDataLog.add(Integer.parseInt((String) msg.obj));;
   			} 
           } 
       };
    
    
	public HistoryFragment(){
		firstPlot = false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		vCloud = inflater.inflate(R.layout.fragment_cloud, container, false);
		for(int i = 0; i<100; i++)
			ecgDataLog.add(i);
		
		return vCloud;
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		initPlot(vCloud);
		initComponents(vCloud);
	}
	
	public void initComponents(View vCloud){
	    /*
		// Initialize components
		bUpload = (Button) vCloud.findViewById(R.id.upload);
		Log.w("MyDebug", bUpload.toString());
		bUpload.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
                uploadData();
			}
							
		});
		
		bDownload = (Button) vCloud.findViewById(R.id.download);
		bDownload.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				tUpload.setText("Wait...Data is downloaded!");


                StringBuffer urln = new StringBuffer(url);
				urln.append(keyName.getText().toString());
				result.setText(urln.toString());
				HttpPackage hp = new HttpPackage(urln.toString(), null, HttpPackage.HTTP_GET);
				HttpRequestTask ht = new HttpRequestTask();
				ht.execute(hp);
				JSONObject jData = null;
				try {
					jData = ht.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				if(jData!=null)
					result.setText(jData.toString());
				else
					result.setText("No result");
]
			}							
		});	
		
		bClear = (Button) vCloud.findViewById(R.id.clear);
		bClear.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				tUpload.setText("Data is cleared!");
				while(historySeries.size()>0)
					historySeries.removeFirst();
				historyPlot.redraw();
			}							
		});	
		
		bStartLog = (Button) vCloud.findViewById(R.id.start_log);
		bStartLog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				mService.startLog();
				tUpload.setText("Start data log!");
			}							
		});	
		
		bStopLog = (Button) vCloud.findViewById(R.id.stop_log);
		bStopLog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				mService.stopLog();
				tUpload.setText("Stop data log!");
			}							
		});	
		
		bClearLog = (Button) vCloud.findViewById(R.id.clear_log);
		bClearLog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				ecgDataLog.clear();
				tUpload.setText("Log data is cleared!");
			}							
		});	
		
		bShowLog = (Button) vCloud.findViewById(R.id.show_log);
		bShowLog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				int i = 0;
				tUpload.setText("Log data is shown!");
				while(historySeries.size()>0)
					historySeries.removeFirst();
				while(ecgDataLog.size()>0){
					tUpload.setText(ecgDataLog.get(0).toString());
					historySeries.addLast(i, ecgDataLog.get(0));
					ecgDataLog.remove(0);
					i++;
				}
				historyPlot.setRangeBoundaries(80, 160, BoundaryMode.FIXED);
				historyPlot.setDomainBoundaries(0, i, BoundaryMode.FIXED);
				historyPlot.redraw();
				historyPlot.calculateMinMaxVals();
			    minXY=new PointF(historyPlot.getCalculatedMinX().floatValue(), historyPlot.getCalculatedMinY().floatValue());
			    maxXY=new PointF(historyPlot.getCalculatedMaxX().floatValue(), historyPlot.getCalculatedMaxY().floatValue());
			}							
		});	
		
		
		tUpload = (TextView) vCloud.findViewById(R.id.text_cloud);
        */
	}

	public void initPlot(View vCloud){
		
        historyPlot = (XYPlot) vCloud.findViewById(R.id.historyPlot);
        historyPlot.setOnTouchListener(this);
        if(firstPlot==false){
        	firstPlot=true;
        	for(int i = 0; i<300; i++){
        		historyData.add(Math.sin((float)i/9f));
        	}
        }

        // Create ecg series
        historySeries = new SimpleXYSeries(
                historyData,            // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,   // Y_VALS_ONLY means use the element index as the x value
                "History");                                   // Set the display title of the series
        // Create a formatter to use for drawing a series using LineAndPointRenderer and configure it from xml:
        LineAndPointFormatter historyFormat = new LineAndPointFormatter(Color.BLUE, Color.TRANSPARENT, Color.TRANSPARENT, null);
        historyFormat.setPointLabelFormatter(new PointLabelFormatter(Color.TRANSPARENT));
        
        historyPlot.addSeries(historySeries, historyFormat);
        historyPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("###.##"));
        historyPlot.getGraphWidget().setDrawMarkersEnabled(true);
        historyPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 20);
        historyPlot.setTicksPerRangeLabel(4);
        historyPlot.getGraphWidget().setDomainLabelOrientation(-30);
        historyPlot.setRangeBoundaries(-2, 2, BoundaryMode.FIXED);
        
        //Set of internal variables for keeping track of the boundaries
        historyPlot.calculateMinMaxVals();
        minXY=new PointF(historyPlot.getCalculatedMinX().floatValue(), historyPlot.getCalculatedMinY().floatValue());
        maxXY=new PointF(historyPlot.getCalculatedMaxX().floatValue(), historyPlot.getCalculatedMaxY().floatValue());
        
	}

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
    	
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: // Start gesture
            firstFinger = new PointF(event.getX(), event.getY());
            mode = ONE_FINGER_DRAG;
            break;
        case MotionEvent.ACTION_UP: 
            //When the gesture ends, a thread is created to give inertia to the scrolling and zoom 

            break;
        case MotionEvent.ACTION_POINTER_UP:
            //When the gesture ends, a thread is created to give inertia to the scrolling and zoom 

            break;
        case MotionEvent.ACTION_POINTER_DOWN: // second finger
            distBetweenFingers = spacing(event);
            // the distance check is done to avoid false alarms
            if (distBetweenFingers > 5f) {
                mode = TWO_FINGERS_DRAG;
            }
            break;
        case MotionEvent.ACTION_MOVE:
    
            if (mode == ONE_FINGER_DRAG) {
             	
                PointF oldFirstFinger=firstFinger;
                firstFinger=new PointF(event.getX(), event.getY());
                lastScrollingX=oldFirstFinger.x-firstFinger.x;
                lastScrollingY=oldFirstFinger.y-firstFinger.y;
                scroll(0.6f*lastScrollingX, 0.6f*lastScrollingY);             
                historyPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED); // change from auto to fixed
                historyPlot.setRangeBoundaries(minXY.y, maxXY.y, BoundaryMode.FIXED); // change from auto to fixed
                historyPlot.redraw();

 
            } else if (mode == TWO_FINGERS_DRAG) {

                float oldDist =distBetweenFingers; 
                distBetweenFingers=spacing(event, oldDist);

                lastZooming=oldDist/distBetweenFingers;
                zoom(lastZooming);
                historyPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED); // change from auto to fixed
                historyPlot.setRangeBoundaries(minXY.y, maxXY.y, BoundaryMode.FIXED); // change from auto to fixed
                historyPlot.redraw();
            }
            break;
        }
        return true;
    }
 
    private void zoom(float ratio) {
        float domainSpanX = maxXY.x - minXY.x;
        float domainSpanY = maxXY.y - minXY.y;
        float domainMidPointX = maxXY.x - domainSpanX / 2.0f;
        float domainMidPointY = maxXY.y - domainSpanY / 2.0f;
        float offsetX = domainSpanX * ratio / 2.0f;
        float offsetY = domainSpanY * ratio / 2.0f;
        minXY.x=(float) (domainMidPointX - offsetX);
        maxXY.x=(float) (domainMidPointX + offsetX);        
        minXY.y=(float) (domainMidPointY - offsetY);
        maxXY.y=(float) (domainMidPointY + offsetY);
    }
 
    private void scroll(float dx, float dy) {
        float domainSpanX = maxXY.x - minXY.x;
        float domainSpanY = maxXY.y - minXY.y;
        Log.w("MyDEBUG", "StepX: "+Float.toString(domainSpanX));
        Log.w("MyDEBUG", "StepY: "+Float.toString(domainSpanY));

        float stepX = domainSpanX / historyPlot.getWidth();
        float stepY = domainSpanY / historyPlot.getHeight();
        Log.w("MyDEBUG", "StepX: "+Float.toString(stepX));
        Log.w("MyDEBUG", "StepY: "+Float.toString(stepY));
        Log.w("MyDEBUG", "Width: "+Float.toString(historyPlot.getWidth()));
        Log.w("MyDEBUG", "Height: "+Float.toString(historyPlot.getHeight()));

        minXY.x+= dx*stepX;
        maxXY.x+= dx*stepX;
        minXY.y-= dy*stepY;
        maxXY.y-= dy*stepY;
    }
 
    private float spacing(MotionEvent event, float oldValue) {
  //  	Log.w("MyDEBUG", "In Spacing: "+ event.getPointerCount());
    	if(event.getPointerCount() >= 2){
    		float x = event.getX(0) - event.getX(1);
    		float y = event.getY(0) - event.getY(1);
    		return FloatMath.sqrt(x*x + y*y);
    	}
    	return oldValue;
    }
    
    private float spacing(MotionEvent event) {
    	  //  	Log.w("MyDEBUG", "In Spacing: "+ event.getPointerCount());
    	if(event.getPointerCount() >= 2){
    		float x = event.getX(0) - event.getX(1);
    		float y = event.getY(0) - event.getY(1);
    	    return FloatMath.sqrt(x*x + y*y);
    	}
    	return 0;
    }

	public AggregatorService getmService() {
		return mService;
	}

	public void setmService(AggregatorService mService) {
		this.mService = mService;
	}
	
	public Handler getmLogHandler() {
		return mLogHandler;
	}

	public void setmLogHandler(Handler mLogHandler) {
		this.mLogHandler = mLogHandler;
	}
	
	public void uploadData(){
        JSONObject jData = new JSONObject();
        try {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            jData.put("name", "ecg"+"_"+timeStamp);
            jData.put("JSON_data", Arrays.toString(ecgDataLog.toArray()));
            Log.w(TAG, Arrays.toString(ecgDataLog.toArray()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpPackage hp = new HttpPackage(url, jData, HttpPackage.HTTP_POST);
        new HttpRequestTask().execute(hp);

        tUpload.setText("Data is uploaded!");
	}
	
	public void downloadData(){
		//stub	
	}
	
}

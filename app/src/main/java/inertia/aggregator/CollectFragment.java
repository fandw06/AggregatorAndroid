package inertia.aggregator;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YValueMarker;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Collect fragment is used to plot figures employing androidplot library.
 * The handler will receive data message sent from ASSISTService and update
 * UI components.
 * 
 * @author David
 */
public class CollectFragment extends Fragment {
	
	private AggregatorService mService;

	private ToggleButton tbStart;
	public TextView tDebug;

    private CheckBox cbPlotECG;
    private CheckBox cbPlotAccel;
    private CheckBox cbPlotOzone;
    private CheckBox cbPlotVoltage;

    private XYPlot ecgPlot;
    private XYPlot ozonePlot;
    private List<Number> ecgData= new LinkedList<Number>();
    private List<Number> ozoneData= new LinkedList<Number>();
    private SimpleXYSeries ecgSeries;
    private SimpleXYSeries ozoneSeries;
    private static String TAG = "PlotFragment";

	/**
	 * Messages used by mGraphHandler. There is a copy in GraphFormFragment.java.
	 * @see inertia.aggregator.AggregatorService
	 */
	private static final int MESSAGE_UPDATE_GRAPH               = 0;
    private static final int MESSAGE_REDRAW                     = 1;

	private Handler mGraphHandler = new Handler(){
    	@Override  
        public void handleMessage(Message msg) {  
        	switch(msg.what) {
                case MESSAGE_UPDATE_GRAPH:
                    Log.w(TAG, "Received data: " + Arrays.toString((int []) msg.obj));
                    int recv[] = (int []) msg.obj;
                    for(int i = 0; i<recv.length; i++){
                        if (ecgSeries.size() > 400) {
                            for(int j = 0; j<400; j++){
                                ecgSeries.removeFirst();
                            }
                        }
                        ecgSeries.addLast(ecgSeries.size()+1, recv[i]);
                        ecgPlot.redraw();
                    }
                    break;
            }
        } 
    };
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View vWaveForm = inflater.inflate(R.layout.fragment_graphform, container, false);		
		
		initPlot(vWaveForm);
		initComponents(vWaveForm);
				
		return vWaveForm;
	}

	/**
	 * Initialize ecg and ozone XYPlots.
	 * 
	 * @param vWaveForm: the parent view to add into.
	 */
	public void initPlot(View vWaveForm){
		
        ecgPlot = (XYPlot) vWaveForm.findViewById(R.id.plot_ecg);
        ozonePlot = (XYPlot) vWaveForm.findViewById(R.id.plot_ozone);
 
 
        // Create ecg series
        ecgSeries = new SimpleXYSeries(
                ecgData,            // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,   // Y_VALS_ONLY means use the element index as the x value
                "ECG");                                   // Set the display title of the series
        // Create a formatter to use for drawing a series using LineAndPointRenderer and configure it from xml:
        LineAndPointFormatter ecgFormat = new LineAndPointFormatter(Color.BLUE, Color.TRANSPARENT, Color.TRANSPARENT, null);
        ecgFormat.setPointLabelFormatter(new PointLabelFormatter(Color.TRANSPARENT));
        
        ecgPlot.addSeries(ecgSeries, ecgFormat);
        ecgPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("###.##"));
        ecgPlot.getGraphWidget().setDrawMarkersEnabled(true);
        ecgPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 50);
        ecgPlot.setTicksPerRangeLabel(4);
        ecgPlot.getGraphWidget().setDomainLabelOrientation(-30);
        ecgPlot.setRangeBoundaries(10, 80, BoundaryMode.FIXED);
        
        // same as above
        ozoneSeries = new SimpleXYSeries(
        		ozoneData, 
        		SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, 
        		"OZONE"); 
        // same as above:
        LineAndPointFormatter ozoneFormat = new LineAndPointFormatter(Color.BLUE, Color.BLUE, Color.TRANSPARENT, null);
        ozoneFormat.setPointLabelFormatter(new PointLabelFormatter(Color.TRANSPARENT));

        ozonePlot.addSeries(ozoneSeries, ozoneFormat);     
        ozonePlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("###.##"));
        ozonePlot.getGraphWidget().setDrawMarkersEnabled(true);
        ozonePlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
        ozonePlot.addMarker(new YValueMarker(30,"Dangerous!"));
        ozonePlot.setTicksPerRangeLabel(4);
        ozonePlot.getGraphWidget().setDomainLabelOrientation(-30);
	}
	
	public void initComponents(View vWaveForm){

        cbPlotECG = (CheckBox) vWaveForm.findViewById(R.id.plot_check_ecg);
        cbPlotECG.setChecked(true);

        cbPlotAccel = (CheckBox) vWaveForm.findViewById(R.id.plot_check_acc);
        cbPlotAccel.setChecked(false);

        cbPlotOzone = (CheckBox) vWaveForm.findViewById(R.id.plot_check_ozone);
        cbPlotOzone. setChecked(true);

        cbPlotVoltage = (CheckBox) vWaveForm.findViewById(R.id.plot_check_voltage);
        cbPlotVoltage.setChecked(false);

		tbStart = (ToggleButton)vWaveForm.findViewById(R.id.toggle_start);
        tbStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mService!=null){
                    if (isChecked){
                        mService.startStreaming();
                        tbStart.setText("Start");
                    }
                    else {
                        mService.stopStreaming();
                        tbStart.setText("Stop");
                    }
                }
                else{
                    tbStart.setClickable(false);
                }
            }
        });
		tDebug = (TextView)vWaveForm.findViewById(R.id.text_debug);
	}

	public void setmService(AggregatorService mService) {
		this.mService = mService;
	}
	
	public Handler getmGraphHandler() {
		return this.mGraphHandler;
	}

	public void setmGraphHandler(Handler mGraphHandler) {
		this.mGraphHandler = mGraphHandler;
	} 
	
}

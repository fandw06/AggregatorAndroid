package inertia.aggregator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Settings fragment is used to manage the control and configuration.
 * 
 * @author Dawei Fan
 *
 */
public class SettingsFragment extends Fragment {
	
	private AggregatorService mService;
    private TextView tUserID;
    private RadioGroup rgPlatform;
    private RadioButton rbHET;
    private RadioButton rbSAP;
    public TextView tStatus;
	private Switch swLED;
    private Switch swCloud;


    private static String TAG = "ControlFragment";

	public SettingsFragment(){
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View vControl = inflater.inflate(R.layout.fragment_control, container, false);		
		initComponents(vControl);				
		return vControl;
	}
	
	public void initComponents(View vControl){
        /**
         * Initialize buttons and text views and add listeners.
         */
        tUserID = (TextView) vControl.findViewById(R.id.text_id);
        rgPlatform = (RadioGroup) vControl.findViewById(R.id.radio_platform);
        rbHET = (RadioButton) vControl.findViewById(R.id.radio_het);
        rbHET.setChecked(true);
        rbSAP = (RadioButton) vControl.findViewById(R.id.radio_sap);
        rgPlatform.setClickable(false);

        swCloud = (Switch) vControl.findViewById(R.id.switch_cloud);
		swLED = (Switch) vControl.findViewById(R.id.switch_led);
		swLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mService!=null){				
					if(isChecked)
						mService.startLED();
					else
						mService.stopLED();										
				}
			}
			
		});

        tStatus = (TextView) vControl.findViewById(R.id.text_status);
	}

	public AggregatorService getmService() {
		return mService;
	}

	public void setmService(AggregatorService mService) {
		this.mService = mService;
	}
}

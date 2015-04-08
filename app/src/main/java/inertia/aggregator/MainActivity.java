package inertia.aggregator;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;

import inertia.aggregator.AggregatorService.LocalBinder;

/**
 * Main activity is used to start this application which extends fragmentActivity to hold
 * multiple fragments in it.
 * <p>
 * When it starts, it will start an ASSIST service and bind the service to this activity. It will also pass
 * the parameter of this service to its fragment members which demand access to methods 
 * of AggregatorService.
 * 
 * @author Dawei Fan
 *
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener{
		
	private SectionsPagerAdapter mSectionsPagerAdapter;
	
	private ViewPager mViewPager;
	
	private AggregatorService mService;
	/**
	 *  A flag indicates if a mService is connected.
	 */
	private boolean mBound = false;
    private static String TAG = "MAIN";
	private ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			LocalBinder binder = (LocalBinder)service;
			mService = binder.getService();
			MainActivity.this.collectFragment.setmService(mService);
			MainActivity.this.settingsFragment.setmService(mService);
			MainActivity.this.historyFragment.setmService(mService);
			
			mService.setmGraphHandler(MainActivity.this.collectFragment.getmGraphHandler());
			mService.setmLogHandler(MainActivity.this.historyFragment.getmLogHandler());
			Log.w(TAG, "mService has be created!");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
		
	};

	/**
	 * A fragment to deal with history display.
	 */
	private HistoryFragment historyFragment;
	/**
	 * A fragment to deal with settings.
	 */
	private SettingsFragment settingsFragment;
	/**
	 * A fragment to deal with data collection.
	 */
	private CollectFragment collectFragment;
	
	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_ACTION_BAR);

	    //Remove notification bar
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		
		setContentView(R.layout.main);		
		startService(new Intent(this, AggregatorService.class));
		initFragment();	
		
	}
	
	public void initFragment(){
		// 1, Set up ActionBar related, most of which are generated by Eclipse automatically.
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		historyFragment = new HistoryFragment();
		settingsFragment = new SettingsFragment();
		collectFragment = new CollectFragment();
		Log.w("MyDebug", "3 fragments have been created!");
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
		
	@Override
	public void onStart(){
		super.onStart();		
		bindService(new Intent(this, AggregatorService.class), mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		
	}

	public boolean ismBound() {
		return mBound;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if(position == 2){
				fragment = MainActivity.this.settingsFragment;
			}
			
			else if(position == 0){
				fragment = MainActivity.this.collectFragment;
			}
			
			else if(position == 1){
				fragment = MainActivity.this.historyFragment;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override

		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 2:
				return "Settings".toUpperCase(l);
			case 0:
				return "Collect".toUpperCase(l);
			case 1:
				return "History".toUpperCase(l);
			}
			return null;
		}
	}
	
}
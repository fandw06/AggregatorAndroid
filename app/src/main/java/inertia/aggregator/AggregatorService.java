package inertia.aggregator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

/**
 * Assist service extends IOIOService which manages data streaming in the whole system. 
 * While this service is alive, it will attempt to connect to a IOIO board. Blink the LED to verify
 * the connection.
 * A notification will appear on the notification bar, enabling the user to stop the 
 * service.
 * <p>
 * Assist service provides an interface for activities and fragments to access IOIO's 
 * resource: LED, spi, and flash. The SPI Winbond flash is used in current test-bench
 * to emulate a node. 
 * <p>
 * The main activity has an instance of ASSISTService and it bind this service when started.
 * Fragments in that activity also have a member of ASSISTService, but it just accesses its 
 * methods without binding this service. The parameter of service is passed into fragments in 
 * main activity when the service is connected.
 * <p> 
 * Handlers are used to communicate between service and specific fragments such as GraphForm 
 * and Cloud.
 * 
 * @author David
 *
 */
public class AggregatorService extends IOIOService {

    private static final String TAG = "AggregatorService";
    private boolean connected = false;
	/**
	 * User defined LED on IOIO.
	 */
	public DigitalOutput led_;
	/**
	 * SPI interfaces on IOIO.
	 */
	public SpiMaster spi;
	/**
	 * A binder to bind a service to a activity. The only purpose of this binder is to return 
	 * a service.
	 */
	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder{
		public AggregatorService getService(){
			return AggregatorService.this;
		}
	}
	
	/**
	 * A notification to be displayed in a activity.
	 */
	private NotificationManager nm;
	
	/**
	 * Flags.
	 */
	volatile private boolean isStreaming;
	volatile private boolean isLogging;
	
	/**
	 * A list to hold commands sent from other fragments or activities.
	 */
	private List<Integer> mCommandsList = new ArrayList<Integer>();
	
	/**
	 * A list to receive instructions from remote node. Currently we just send commands to a node, so this is not in use.
	 */
	@SuppressWarnings("unused")
	private List<byte []> mInstructionsList = new ArrayList<byte[]>();
	/**
	 *  Command sent from GUI.
	 *  For example, if pressed "Start" button in Control fragment, a method "mService.start()" is invoked
	 *  in that button listener. Then COMMAND_START_LOG is added in CommandsList. In the IOIOlooper, the command will
	 *  be dequeued and executed. 
	 */
	private static final int COMMAND_TURN_ON_LED                = 0;
	private static final int COMMAND_TURN_OFF_LED               = 1;
	private static final int COMMAND_START_STREAMING            = 2;
	private static final int COMMAND_STOP_STREAMING             = 3;
	private static final int COMMAND_START_LOG                  = 4;
	private static final int COMMAND_STOP_LOG                   = 5;
	
	/**
	 * Messages used by mGraphHandler. There is a copy in GraphFormFragment.java.
	 * @see CollectFragment
	 */
	private static final int MESSAGE_UPDATE_GRAPH               = 0;
	/**
	 * Messages used by mLogHandler. There is a copy in CloudFragment.java.
	 * @see HistoryFragment
	 */
	private static final int MESSAGE_LOG_ECG                    = 2;
//	private static final int COMMAND_LOG_OZONE                  = 3;
    private static final byte SPI_READ                          = 3;
	
	/**
	 * Constants describing the packet type, not used now.
	 */
    /*
    private static final byte DATA_PACKET                      = (byte) 0x00;
    private static final byte INQUIRY_COMMAND                  = (byte) 0x01;
    private static final byte INQUIRY_RESPONSE                 = (byte) 0x02;
    private static final byte START_STREAMING_COMMAND          = (byte) 0x03;
    private static final byte STOP_STREAMING_COMMAND           = (byte) 0x04;    

    private static final byte TOGGLE_LED_COMMAND               = (byte) 0x07;
	*/

	/**
	 * Communicates between loop thread and thread in graph fragment.
	 */
	private Handler mGraphHandler;
	/**
	 * Communicates between loop thread and thread in cloud fragment.
	 */
	private Handler mLogHandler;


	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			
			@Override
			protected void setup() throws ConnectionLostException, InterruptedException {
				/* Initialize LED pin. */
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
				/* Initialize SPI pins. */
				spi = ioio_.openSpiMaster(4, 5, 7, 6, SpiMaster.Rate.RATE_1M);
                connected = true;
			}

			@Override
			public void loop() throws ConnectionLostException, InterruptedException {

				while(!mCommandsList.isEmpty()){			
					int command = mCommandsList.get(0);
					mCommandsList.remove(0);
					switch(command){
						case COMMAND_TURN_ON_LED: 
							led_.write(true);
							break;
							
						case COMMAND_TURN_OFF_LED: 
							led_.write(false);
							break;
							
						case COMMAND_START_STREAMING:
							isStreaming = true;
                            new Thread(new Runnable(){
							    @Override
							    public void run(){
                                    while(true){
							            while(!isStreaming);
                                        byte sent[] = {SPI_READ};
				                        /* The maximum length is 64 defined by IOIO. */
                                        byte received[] = new byte[64];

                                        int temp[] = new int[64];
                                        try {
                                            spi.writeRead(0, sent, sent.length, 64, received, received.length);
                                            Log.w(TAG, "received: " + Arrays.toString(received));
                                            for(int ii = 0; ii<received.length; ii++){
                                                if(received[ii]<0)
                                                    temp[ii] = received[ii]+256;
                                                else
                                                    temp[ii] = received[ii];

                                                //just for test
                                                temp[ii] = 80-temp[ii];
                                            }

                                        } catch (ConnectionLostException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
												
							            Message msgUpdate = new Message();
                                        msgUpdate.what = MESSAGE_UPDATE_GRAPH;
                                        msgUpdate.obj = temp;
							            mGraphHandler.sendMessage(msgUpdate);
                                        if(isLogging){
										    Message msgLog = new Message();
                                            msgLog.what = MESSAGE_LOG_ECG ;
                                            msgLog.obj = temp;
										    mLogHandler.sendMessage(msgLog);
									    }

                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
							        }
							    }
						    }).start();
							break;
							
						case COMMAND_STOP_STREAMING:
							isStreaming = false;
							break;
							
						case COMMAND_START_LOG:
							isLogging = true;
							break;
							
						case COMMAND_STOP_LOG:
							isLogging = false;
							break;						
					}
				}
			}

            @Override
            public void disconnected() {

            }
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		isStreaming = false;
		isLogging = false;
	
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			nm.cancel(0);
			stopSelf();
			
		} else {			
			Notification notification = new Notification.Builder(this)
				.setContentTitle("Aggregator IOIO Service")
				.setContentText("Click to stop")
				.setTicker("Assist service running...")
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.sticker56))
				.setSmallIcon(R.drawable.icon22)
				.setWhen(System.currentTimeMillis())
				.setVibrate(new long[]{200, 200})
				.setContentIntent(PendingIntent.getService(
						AggregatorService.this, 
						0, 
						new Intent("stop", null, AggregatorService.this, AggregatorService.this.getClass()), 
						0))
				.getNotification();				
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			nm.notify(0, notification);
		}
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public void onDestroy(){
        connected = false;
		nm.cancel(0);
	}

    public boolean isConnected() {
        return connected;
    }

	public Handler getmGraphHandler() {
		return mGraphHandler;
	}

	public void setmGraphHandler(Handler mGraphHandler) {
		this.mGraphHandler = mGraphHandler;
	}
	
	public Handler getmLogHandler() {
		return mLogHandler;
	}

	public void setmLogHandler(Handler mLogHandler) {
		this.mLogHandler = mLogHandler;
	}
	
	public void startStreaming(){
		mCommandsList.add(COMMAND_START_STREAMING );
	}
	
	public void stopStreaming(){
		mCommandsList.add(COMMAND_STOP_STREAMING );
	}
	
	public void startLED(){
		mCommandsList.add(COMMAND_TURN_ON_LED);
	}
	
	public void stopLED(){
		mCommandsList.add(COMMAND_TURN_OFF_LED);
	}
	
	public void startLog(){
		if(isStreaming = true)
			mCommandsList.add(COMMAND_START_LOG);
	}
	
	public void stopLog(){
		if(isStreaming = false)
			mCommandsList.add(COMMAND_STOP_LOG);
	}
	
}
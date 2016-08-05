package net.bestidear.jettyinput;

import java.util.EnumMap;
import java.util.Map;

import net.bestidear.jettyinput.socket.RCService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends Activity {
	
	  private static final int WHITE = 0xFFFFFFFF;
	  private static final int BLACK = 0xFF000000;
	  

	  final String TAG="MainACtivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {   
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		startService(new Intent(this, RCService.class));
		startJetty();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);		
		return true;
	}

	 
	
	private void startJetty()
	{		
		 try {
			 
			 String str="http://"+HostInterface.getIPv4Address()+":8020/utooir.apk";//utooir.apk
			 Bitmap bitmap=encodeAsBitmap(str) ;
			 ImageView view = (ImageView) findViewById(R.id.image_view);
		      view.setImageBitmap(bitmap);
		     TextView tv= (TextView)findViewById(R.id.text_view_url);
		     tv.setText(getString(R.string.barcode_input_pre)+str);
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	Bitmap encodeAsBitmap(String contentsToEncode) throws WriterException {
        if (contentsToEncode == null) {
          return null;
        }
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
	    Display display = manager.getDefaultDisplay();
	    int mwidth = display.getWidth();
	    int mheight = display.getHeight();
	    int smallerDimension = mwidth < mheight ? mwidth : mheight;
	    smallerDimension = smallerDimension * 7 / 8;
	    Map<EncodeHintType,Object> hints = null;
	    String encoding = guessAppropriateEncoding(contentsToEncode);
	    if (encoding != null) {
	      hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
	      hints.put(EncodeHintType.CHARACTER_SET, encoding);
	    }
        BitMatrix result;
        try {
          result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hints);
        } catch (IllegalArgumentException iae) {
          // Unsupported format
          return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
          int offset = y * width;
          for (int x = 0; x < width; x++) {
            pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
          }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
      }
	
	  private static String guessAppropriateEncoding(CharSequence contents) {
		    // Very crude at the moment
		    for (int i = 0; i < contents.length(); i++) {
		      if (contents.charAt(i) > 0xFF) {
		        return "UTF-8";
		      }
		    }
		    return null;
		  }
	
}

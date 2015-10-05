package in.vipplaza.utills;

// json response class.
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class IOUtils 
{
    public static final String PREFS_FILE ="javaeye.prefs";
    public static String getUrlResponse(String url) {
        try {
            HttpGet get = new HttpGet(url);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            
            
            return convertStreamToString(entity.getContent());
        } catch (Exception e) {
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8*1024);
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }  
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
 
        return sb.toString();
    }
    
    public static Bitmap getBitmapFromUrl(URL url)
    {
         Bitmap bitmap = null;
         InputStream in = null;
         OutputStream out = null;

         try {
             in = new BufferedInputStream(url.openStream(), 4 * 1024);

             final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
             out = new BufferedOutputStream(dataStream, 4 * 1024);
             copy(in, out);
             out.flush();

             final byte[] data = dataStream.toByteArray();
             bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
             return bitmap;
         } catch (IOException e) {
         } finally {
             closeStream(in);
             closeStream(out);
         }
         return null;
    }
    
    public static Drawable getDrawableFromUrl(URL url) {
        try {
            InputStream is = url.openStream();
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return null;
    }
    
    private static void copy(InputStream in, OutputStream out) throws IOException {
       byte[] b = new byte[4 * 1024];
       int read;
       while ((read = in.read(b)) != -1) {
           out.write(b, 0, read);
       }
   }
    
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }
    public static byte[] getbytearrayFromUrl(URL url) {
        
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new BufferedInputStream(url.openStream(), 4 * 1024);

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 4 * 1024);
            copy(in, out);
            out.flush();

            final byte[] data = dataStream.toByteArray();
            
            return data;  
        } catch (IOException e) {
        } finally {
            closeStream(in);
            closeStream(out);
        }
        return null;
   } 
 public static byte[] getbytearrayInputstram(InputStream in) {
        
//        InputStream in = null;
//        in = new BufferedInputStream(url.openStream(), 4 * 1024);
        OutputStream out = null;

        try {
           

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 4 * 1024);
            copy(in, out);
            out.flush();

            final byte[] data = dataStream.toByteArray();
            
            return data;  
        } catch (IOException e) {
        } finally {
            closeStream(in);
            closeStream(out);
        }
        return null;
   } 
 
 public static String DownloadImage(String imageUrl, String ImageName)      
 {
		//path where the data is stored in SD card
		String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/BestOfMaui/"+"Images";
		// String path = "/sdcard/BusinessJournal/"+id+"/ThumbImage";
     InputStream is = null;
     if((imageUrl == null) || (imageUrl.length() == 0) || (imageUrl == " "))
     {
         System.out.println("No need to download images now");
     }
     else
     {
         System.gc();
         URL myFileUrl =null; 
         FileOutputStream outStream = null;
         File file = new File(path);

         if(!file.exists())
         {
             file.mkdirs();  
         }

         File outputFile;
   

             try {   
                 myFileUrl= new URL(imageUrl.trim().replaceAll( " ", "%20" ));

                 HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
                 conn.setRequestMethod("GET");

                 conn.setDoInput(true);
                 conn.setConnectTimeout(20000);
                 conn.connect();

                 is = conn.getInputStream();


         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
                 e.printStackTrace();
             }

              try {
                  outputFile = new File(file, ImageName);

                  if(outputFile.exists())                
                  {
                      System.out.println("No need to download image it already exist");
                    //  outputFile.delete();
                  }
                  
                  else{
                 	 
                 	 System.out.println("downloading image...");
                      outputFile.createNewFile();

                      outStream = new FileOutputStream(outputFile);
                      //bos = new BufferedOutputStream(outStream);
                      BufferedInputStream bis = new BufferedInputStream(is);

                         ByteArrayBuffer baf = new ByteArrayBuffer(50);

                         int current = 0;
                         while ((current = bis.read()) != -1) 
                         {
                             baf.append((byte) current);
                         }

                         outStream.write(baf.toByteArray());
                         outStream.close();

                  }


             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }   
     }
		return path+"/"+ImageName;

 }
 
 
	public static String readSavedData ( ) {
        StringBuffer datax = new StringBuffer("");
        try {
        	
        	 File sdcard = Environment.getExternalStorageDirectory();  
        	 File directory = new File(sdcard.getAbsolutePath()+ "/BestOfMaui");  
             directory.mkdirs();  
             File file = new File(directory,"Data.txt");  
            FileInputStream fIn = new FileInputStream ( file ) ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return datax.toString() ;
    }
	
	
	 public final static boolean isValidEmail(CharSequence target) {
         if (target == null) {
         return false;
         } else {
         return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
         }
     }   

}

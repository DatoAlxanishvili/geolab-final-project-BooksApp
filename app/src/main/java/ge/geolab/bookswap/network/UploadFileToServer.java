package ge.geolab.bookswap.network;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ge.geolab.bookswap.R;
import ge.geolab.bookswap.activities.MainActivity;
import ge.geolab.bookswap.models.Book;

/**
 * Created by dalkh on 30-Dec-15.
 */
public class UploadFileToServer extends AsyncTask<Void, Integer, String> {
    long totalSize=0;
    Book book;
    Context context;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    int id = 1;

    public UploadFileToServer(Context context,Book book) {
        this.book=book;
        this.context=context;
    }
    @Override
    protected void onPreExecute() {
        // setting progress bar to zero
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("ატვირთვა")
                .setContentText("მიმდინარეობს ატვირთვა")
                .setSmallIcon(R.drawable.ic_upload)
                .setProgress(100,0,false);
        mNotifyManager.notify(id, mBuilder.build());
       // this.progressBar.setProgress(0);
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

        mBuilder.setProgress(100, progress[0], false);
        mNotifyManager.notify(id, mBuilder.build());
    }

    @Override
    protected String doInBackground(Void... params) {
        return uploadFile();
    }

    @SuppressWarnings("deprecation")
    private String uploadFile() {
        String responseString = null;
        HttpParams httpParameters = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);
        HttpProtocolParams.setHttpElementCharset(httpParameters, HTTP.UTF_8);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
        httpclient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
        httpParameters.setBooleanParameter("http.protocol.expect-continue", false);
        HttpPost httppost = new HttpPost("http://192.168.1.100/geolabclass/welcome/upload");
        try {
            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });


            ArrayList<File> bookArray=this.book.getPictures();
            // Adding file data to http body
            for (int i = 0; i <bookArray.size() ; i++) {
                entity.addPart("image[]", new FileBody(bookArray.get(i)));
            }
            entity.addPart("user_id",new StringBody(this.book.getId(),ContentType.APPLICATION_JSON));
            entity.addPart("title",new StringBody(this.book.getTitle(),ContentType.create("text/plain", HTTP.UTF_8)));
            entity.addPart("author",new StringBody(this.book.getAuthor(),ContentType.APPLICATION_JSON));
            entity.addPart("ad_type",new StringBody(this.book.getAdType(),ContentType.APPLICATION_JSON));
            entity.addPart("category",new StringBody(this.book.getCategory(),ContentType.APPLICATION_JSON));
            entity.addPart("state",new StringBody(this.book.getCondition(),ContentType.APPLICATION_JSON));
            entity.addPart("location",new StringBody(this.book.getLocation(),ContentType.APPLICATION_JSON));
            entity.addPart("exchange_item",new StringBody(this.book.getExchangeItem(),ContentType.APPLICATION_JSON));
            entity.addPart("email",new StringBody(this.book.geteMail(),ContentType.APPLICATION_JSON));
            entity.addPart("mobile_number",new StringBody(this.book.getMobileNum(),ContentType.APPLICATION_JSON));
            entity.addPart("description",new StringBody(this.book.getDescription(),ContentType.APPLICATION_JSON));



            totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            // Making server call

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
            } else {
                responseString = "Error ! Http Status Code: "
                        + statusCode;
            }

        } catch (ClientProtocolException e) {
            responseString = e.toString();
        } catch (IOException e) {
            responseString = e.toString();
        }

        return responseString;

    }

    @Override
    protected void onPostExecute(String result) {
        Log.e("TAG", "Response from server: " + result);

        // showing the server response in an alert dialog
        try {
            JSONObject jsonResponse=new JSONObject(result);

            mBuilder.setContentText(jsonResponse.getString("message"));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onPostExecute(result);
    }


}
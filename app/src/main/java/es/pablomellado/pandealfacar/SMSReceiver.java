package es.pablomellado.pandealfacar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;
import android.util.Log;

import es.pablomellado.pandealfacar.register.RegisterActivity;

/**
 * Created by Pablo Mellado on 2/5/17.
 */

public class SMSReceiver extends BroadcastReceiver
{
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    static String address, body = null;

    // Retrieve SMS
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action.equals(ACTION_SMS_RECEIVED))
        {
            SmsMessage[] msgs = getMessagesFromIntent(intent);
            if (msgs != null)
            {
                SharedPreferences sharedPref = context.getSharedPreferences(
                        context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                String uuid = sharedPref.getString(context.getString(R.string.random_id), "");
                if (uuid.length()>0) {

                    for (int i = 0; i < msgs.length; i++) {
                        address = msgs[i].getOriginatingAddress();
                        body = msgs[i].getMessageBody();
                        Log.v("", "Originating Address : Sender :"+address);
                        Log.v("Message from sender :", ""+ body);
                        Log.v("Phone from the form : ", ""+ RegisterActivity.mPhoneNumber);
                        //isSame = PhoneNumberUtils.compare(body, RegisterActivity.mPhoneNumber);
                        if (body.indexOf(uuid)!=-1) {
                            Log.v("Comparison :", "UUID found in the message");
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean(context.getString(R.string.phone_is_confirmed), true);
                            editor.commit();
                        }
                    }
                }
            }
        }
    }

    public static SmsMessage[] getMessagesFromIntent(Intent intent)
    {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++)
        {
            pduObjs[i] = (byte[]) messages[i];
        }

        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++)
        {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
}

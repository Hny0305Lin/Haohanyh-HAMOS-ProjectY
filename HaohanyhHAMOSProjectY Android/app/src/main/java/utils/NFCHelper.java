package utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

public class NFCHelper {
    //初始化所需用到的东西
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mIntentFilter;
    private PendingIntent mPendingIntent;
    private String[][] mTechList;
    private AlertDialog alertDialog;

    /*
     * 初始化
     */
    private void init(Context context) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPendingIntent = PendingIntent.getActivity(context, 123, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPendingIntent = PendingIntent.getActivity(context, 123, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_ONE_SHOT);
        }
        //mPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilter = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
                , new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                , new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };
        mTechList = new String[][]{
                new String[]{android.nfc.tech.IsoDep.class.getName()}
                , new String[]{android.nfc.tech.NfcA.class.getName()}
                , new String[]{android.nfc.tech.NfcB.class.getName()}
                , new String[]{android.nfc.tech.NfcF.class.getName()}
                , new String[]{android.nfc.tech.NfcV.class.getName()}
                , new String[]{Ndef.class.getName()}
                , new String[]{NdefFormatable.class.getName()}
                , new String[]{android.nfc.tech.MifareClassic.class.getName()}
                , new String[]{MifareUltralight.class.getName()}
        };
    }

    /*
     * 调用初始化函数的同时重构
     */
    public NFCHelper(Context context) { init(context); }

    /*
     * 判断当前设备是否支持NFC功能
     */
    public boolean SupportNFC() {
        boolean NFCSupport = mNfcAdapter != null;
        Log.v("浩瀚银河:", "是否支持NFC: " + NFCSupport);
        return NFCSupport;
    }

    /*
     * 判断当前设备的NFC功能是否可用
     */
    public boolean EnableNFC() {
        boolean NFCEnable = mNfcAdapter != null && mNfcAdapter.isEnabled();
        Log.v("浩瀚银河:", "是否可用NFC: " + NFCEnable);
        return NFCEnable;
    }

    /*
     * 注册NFC广播监听
     * 浩瀚银河提醒各位开发者：我们在Android 12的灰度测试设备上，发现了NFC注册广播监听的Bug，如果您也遇到了，请按照下面初始化函数修改。
     */
    public void RegisterNFC(Activity activity) {
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(activity, mPendingIntent, null, null);
            Log.v("浩瀚银河:", "是否注册成功NFC: " + "成功");
        }
    }

    /*
     * 注销NFC广播监听
     */
    public void UnRegisterNFC(Activity activity) {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(activity);
            Log.v("浩瀚银河:", "是否注销成功NFC: " + "成功");
        }
    }

    /*
     * 显示NFC设置界面
     * 一般为如果您NFC没有提前开启而进行的函数，当然，如果出现了此页面，去打开了NFC再回来，也是没有问题的。
     */
    public void GoToNFCSetting(final Context context) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Haohanyh-ProjectY-NFC")
                    .setMessage("当前应用需要您开启NFC,是否立即去设置界面开启?")
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @SuppressLint("ObsoleteSdkInt")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                context.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                            } else {
                                context.startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
                            }
                        }
                    }).create();
        }
        alertDialog.show();
    }

    /*
     * 向NFC标签中写入NDEF格式数据
     * 目前暂不确认护花使者我们该写入的是NFC标签还是Tag，正在研究。
     */
    public boolean writeNFC_NDEF(Intent intent, String text) {
        if (intent != null && text != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            return writeTag(tag, new NdefMessage(new NdefRecord[]{createTextRecord(Locale.CHINA.getLanguage(), text)}));
        }
        return false;
    }

    /*
     * 向TAG中写入NDEF格式数据
     * 目前暂不确认护花使者我们该写入的是NFC标签还是Tag，正在研究。
     */
    private boolean writeTag(Tag tag, NdefMessage message) {
        if (tag != null && message != null) {
            Ndef ndef = Ndef.get(tag);
            return ndef == null ? writeNoNdefMessage(tag, message) : writeNdefMessage(ndef, message);
        }
        return false;
    }

    /*
     * nfc标签未格式化,或没有分区,先进行格式化,并将数据写入
     */
    private boolean writeNoNdefMessage(Tag tag, NdefMessage message) {
        NdefFormatable format = NdefFormatable.get(tag);
        if (format != null) {
            try {
                format.connect();
                format.format(message);
                Log.v("浩瀚银河:", "是否数据写入成功NFC: " + "成功" + message);
                return true;
            } catch (IOException | FormatException e) {
                e.printStackTrace();
            } finally {
                try {
                    format.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /*
     * nfc标签支持NDEF数据,向nfc标签中写入NDEF数据
     */
    private boolean writeNdefMessage(Ndef ndef, NdefMessage message) {
        //判断是否可写
        if (ndef.isWritable()) {
            int size = message.toByteArray().length;
            //判断容量是否够用
            if (ndef.getMaxSize() > size) {
                try {
                    ndef.connect();
                    ndef.writeNdefMessage(message);
                    Log.v("浩瀚银河:", "是否数据写入成功NFC: " + "成功" + message);
                    return true;
                } catch (IOException | FormatException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ndef.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }


    /*
     *生成NDEF格式的数据
     */
    private NdefRecord createTextRecord(String languageCode, String text) {
        if (text == null)
            throw new NullPointerException("text is null");

        byte[] textBytes = text.getBytes(Charset.forName("UTF-8"));

        byte[] languageCodeBytes = null;
        if (languageCode != null && !languageCode.isEmpty()) {
            languageCodeBytes = languageCode.getBytes(Charset.forName("US-ASCII"));
        } else {
            languageCodeBytes = Locale.getDefault().getLanguage().getBytes(Charset.forName("US-ASCII"));
        }
        if (languageCodeBytes.length >= 64) {
            throw new IllegalArgumentException("language code is too long, must be <64 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(1 + languageCodeBytes.length + textBytes.length);

        byte status = (byte) (languageCodeBytes.length & 0xFF);
        buffer.put(status);
        buffer.put(languageCodeBytes);
        buffer.put(textBytes);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, buffer.array());
    }

    /*
     * 手机震动提醒
     */
    public void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 100, 50, 100};
            vibrator.vibrate(pattern, -1);
        }
    }

}

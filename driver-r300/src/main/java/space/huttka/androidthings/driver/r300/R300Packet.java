package space.huttka.androidthings.driver.r300;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author leon0399
 */
@SuppressWarnings("WeakerAccess")
public class R300Packet {
    public static final byte FINGERPRINT_STARTCODE_1 = (byte) 0xEF;
    public static final byte FINGERPRINT_STARTCODE_2 = (byte) 0x01;

    public static final byte FINGERPRINT_COMMANDPACKET = 0x1;
    public static final byte FINGERPRINT_DATAPACKET = 0x2;
    public static final byte FINGERPRINT_ACKPACKET = 0x7;
    public static final byte FINGERPRINT_ENDDATAPACKET = 0x8;
    public static final byte FINGERPRINT_OK = 0x00;
    public static final byte FINGERPRINT_PACKETRECIEVEERR = 0x01;
    public static final byte FINGERPRINT_NOFINGER = 0x02;
    public static final byte FINGERPRINT_IMAGEFAIL = 0x03;
    public static final byte FINGERPRINT_IMAGEMESS = 0x06;
    public static final byte FINGERPRINT_FEATUREFAIL = 0x07;
    public static final byte FINGERPRINT_NOMATCH = 0x08;
    public static final byte FINGERPRINT_NOTFOUND = 0x09;
    public static final byte FINGERPRINT_ENROLLMISMATCH = 0x0A;
    public static final byte FINGERPRINT_BADLOCATION = 0x0B;
    public static final byte FINGERPRINT_DBRANGEFAIL = 0x0C;
    public static final byte FINGERPRINT_UPLOADFEATUREFAIL = 0x0D;
    public static final byte FINGERPRINT_PACKETRESPONSEFAIL = 0x0E;
    public static final byte FINGERPRINT_UPLOADFAIL = 0x0F;
    public static final byte FINGERPRINT_DELETEFAIL = 0x10;
    public static final byte FINGERPRINT_DBCLEARFAIL = 0x11;
    public static final byte FINGERPRINT_PASSFAIL = 0x13;
    public static final byte FINGERPRINT_INVALIDIMAGE = 0x15;
    public static final byte FINGERPRINT_FLASHERR = 0x18;
    public static final byte FINGERPRINT_INVALIDREG = 0x1A;
    public static final byte FINGERPRINT_ADDRCODE = 0x20;
    public static final byte FINGERPRINT_PASSVERIFY = 0x21;
    public static final int FINGERPRINT_TIMEOUT = 0xFF;
    public static final byte FINGERPRINT_BADPACKET = (byte) 0xFE;
    public static final byte FINGERPRINT_GETIMAGE = 0x01;
    public static final byte FINGERPRINT_IMAGE2TZ = 0x02;
    public static final byte FINGERPRINT_REGMODEL = 0x05;
    public static final byte FINGERPRINT_STORE = 0x06;
    public static final byte FINGERPRINT_LOAD = 0x07;
    public static final byte FINGERPRINT_UPLOAD = 0x08;
    public static final byte FINGERPRINT_DELETE = 0x0C;
    public static final byte FINGERPRINT_EMPTY = 0x0D;
    public static final byte FINGERPRINT_HISPEEDSEARCH = 0x1B;
    public static final byte FINGERPRINT_TEMPLATECOUNT = 0x1D;

    public static final byte FINGERPRINT_VERIFYPASSWORD = 0x13;
    public static final byte FINGERPRINT_SETPASSWORD = 0x12;
    public static final byte FINGERPRINT_GETRANDOMCODE = 0x14;
    public static final byte FINGERPRINT_SETADDRESS = 0x15;

    private static final String TAG = "R300Packet";

    /**
     * "Header" in Data package format
     */
    public byte[] header = new byte[2];

    /**
     * "Adder" in Data package format
     */
    public byte[] adder = new byte[4];

    /**
     * "Package identifier" in Data package format
     */
    public byte pid;

    /**
     * "Package length" in Data package format
     */
    public byte[] length = new byte[2];

    /**
     * "Package contents" in Data package format
     */
    public byte[] data = new byte[256];

    protected R300Packet() {

    }

    /**
     * @param pid  "Package identifier"
     * @param data "Package contents"
     */
    public R300Packet(byte[] adder, byte pid, byte[] data) {
        this.header = new byte[]{FINGERPRINT_STARTCODE_1, FINGERPRINT_STARTCODE_2};
        this.adder = adder;
        this.pid = pid;

        int packetLength = data.length + 2;
        this.length = new byte[]{(byte) ((packetLength >> 8) & 0xFF), (byte) (packetLength & 0xFF)};

        this.data = data;
    }

    /**
     * The arithmetic sum of package identifier, package length and all package contents.
     * Overflowing bits are omitted. high byte is transferred first.
     *
     * @param pid    "Package identifier"
     * @param length "Package length"
     * @param data   "Package contents"
     * @return Checksum (2 bytes)
     */
    private byte[] calcChecksum(byte pid, byte length[], byte[] data) {
        long ck = 0x0000;

        ck += (pid & 0xff);

        for (byte len : length) {
            ck += (len & 0xff);
        }

        for (byte dat : data) {
            ck += (dat & 0xff);
        }

        Log.d(TAG, "Checksum: " + Long.toHexString(ck));

        return new byte[]{(byte) ((ck >> 8) & 0xFF), (byte) (ck & 0xFF)};
    }

    protected void setHeader(byte[] header) {
        this.header = header;
    }

    protected void setAdder(byte[] adder) {
        this.adder = adder;
    }

    public void write(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        byteArrayOutputStream.write(this.header);
        byteArrayOutputStream.write(this.adder);
        byteArrayOutputStream.write(this.pid);
        byteArrayOutputStream.write(this.length);
        byteArrayOutputStream.write(this.data);
        byteArrayOutputStream.write(calcChecksum(this.pid, this.length, this.data));
    }

}

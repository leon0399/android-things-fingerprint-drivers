package space.huttka.androidthings.driver.r300;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_COMMANDPACKET;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_DBCLEARFAIL;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_EMPTY;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_ENDDATAPACKET;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_GETIMAGE;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_GETRANDOMCODE;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_OK;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_PACKETRECIEVEERR;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_PASSFAIL;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_SETADDRESS;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_SETPASSWORD;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_TEMPLATECOUNT;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_UPLOADFAIL;
import static space.huttka.androidthings.driver.r300.R300Packet.FINGERPRINT_VERIFYPASSWORD;

/**
 * @author leon0399
 */
@SuppressWarnings("WeakerAccess")
public class R300Module implements AutoCloseable {
    public static final int DEFAULT_BAUDRATE = 57600;

    private static final String TAG = "R300Module";

    private UartDevice mDevice;

    private byte[] mAddress;
    private byte[] mPassword;

    /**
     * @param uartPort UART port name where the module is attached. Cannot be null.
     * @param address  Address of module.
     * @param password Password of module.
     * @throws IOException if the hardware board had a problem with its hardware ports
     */
    protected R300Module(@NonNull String uartPort, byte[] address, byte[] password) throws IOException {
        try {
            initializePeripherals(uartPort);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing UART Device", e);
            close();
            throw e;
        }

        this.mAddress = address;
        this.mPassword = password;
    }

    /**
     * Transforms array of bytes to hex-encoded string
     *
     * @param bytes Bytes to be processed
     * @return String of hex-encoded bytes
     */
    public static String byteArrayToHexString(final byte[] bytes) { //todo: delete this crap
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("0x%02x ", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * Performs the initial configuration on hardware ports
     *
     * @throws IOException if the hardware board had a problem with its hardware ports
     */
    private void initializePeripherals(String uartPort) throws IOException {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        this.mDevice = peripheralManagerService.openUartDevice(uartPort);
        this.mDevice.setBaudrate(DEFAULT_BAUDRATE);
        this.mDevice.setDataSize(8);
        this.mDevice.setStopBits(1);
        this.mDevice.setParity(UartDevice.PARITY_NONE);
    }

    @Override
    public void close() throws IOException {
        if (this.mDevice != null) {
            try {
                this.mDevice.close();
            } finally {
                this.mDevice = null;
            }
        }
    }

    /**
     * Verify Module’s handshaking password.
     *
     * @return {@link R300Packet#FINGERPRINT_OK} if password is correct, {@link R300Packet#FINGERPRINT_PASSFAIL} if password is invalid, {@link R300Packet#FINGERPRINT_PACKETRECIEVEERR} otherwise
     */


    /**
     * to delete all the templates in the Flash library
     *
     * @return {@link R300Packet#FINGERPRINT_OK} if empty success, {@link R300Packet#FINGERPRINT_DBCLEARFAIL} if data base clear fail, {@link R300Packet#FINGERPRINT_PACKETRECIEVEERR} otherwise
     */
    public byte Empty() {
        try {
            R300Packet packet = getPacket(FINGERPRINT_EMPTY);

            if (packet.data[0] == FINGERPRINT_OK)
                return FINGERPRINT_OK;
            else if (packet.data[0] == FINGERPRINT_DBCLEARFAIL)
                return FINGERPRINT_DBCLEARFAIL;
            else
                return FINGERPRINT_PACKETRECIEVEERR;
        } catch (IOException e) {
            Log.e(TAG, "Fail to clear finger library: ", e);
            return FINGERPRINT_PACKETRECIEVEERR;
        }
    }

    /**
     * detecting finger and store the detected finger image in ImageBuffer while returning successfull confirmation code;
     */

    public byte VfyPwd() {
        try {
            R300Packet packet = getPacket(FINGERPRINT_VERIFYPASSWORD, this.mPassword);

            if (packet.data[0] == FINGERPRINT_OK)
                return FINGERPRINT_OK;
            else if (packet.data[0] == FINGERPRINT_PASSFAIL)
                return FINGERPRINT_PASSFAIL;
            else
                return FINGERPRINT_PACKETRECIEVEERR;
        } catch (IOException e) {
            Log.e(TAG, "Error when receiving package: ", e);
            return FINGERPRINT_PACKETRECIEVEERR;
        }
    }

    /**
     * <b>Warning!</b> Avoid using! Password will not be saved!
     * <p>
     * Set Module’s handshaking password.
     *
     * @param password Password to be set
     * @return {@link R300Packet#FINGERPRINT_OK} if password setting completed, {@link R300Packet#FINGERPRINT_PACKETRECIEVEERR} otherwise
     */
    public byte SetPwd(byte[] password) {
        try {
            R300Packet packet = getPacket(FINGERPRINT_SETPASSWORD, password);

            if (packet.data[0] == FINGERPRINT_OK) {
                this.mPassword = password;
                return FINGERPRINT_OK;
            } else {
                return FINGERPRINT_PACKETRECIEVEERR;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting password: ", e);
            return FINGERPRINT_PACKETRECIEVEERR;
        }
    }

    /**
     * <b>Warning!</b> Avoid using! Address will not be saved!
     * <p>
     * Set Module address.
     *
     * @param adder New address of module
     * @return {@link R300Packet#FINGERPRINT_OK} if address setting completed, {@link R300Packet#FINGERPRINT_PACKETRECIEVEERR} otherwise
     */
    public byte SetAdder(byte[] adder) {
        try {
            R300Packet packet = getPacket(FINGERPRINT_SETADDRESS, adder);

            if (packet.data[0] == FINGERPRINT_OK) {
                this.mAddress = adder;
                return FINGERPRINT_OK;
            } else {
                return FINGERPRINT_PACKETRECIEVEERR;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting address : ", e);
            return FINGERPRINT_PACKETRECIEVEERR;
        }
    }

    /**
     * todo: javadoc
     *
     * @return
     */
    public byte[] ReadSysPara() {
        try {
            R300Packet packet = getPacket(FINGERPRINT_UPLOADFAIL);

            if (packet.data[0] == FINGERPRINT_OK) {
                return Arrays.copyOfRange(packet.data, 1, 17);
            } else {
                return new byte[16];
            }
        } catch (IOException e) {
            Log.e(TAG, "Error when receiving package: ", e);
            return new byte[16];
        }
    }

    /**
     * Read the current valid template number of the Module
     *
     * @return Template number of the Module
     */
    public byte[] TempleteNum() {
        try {
            R300Packet packet = getPacket(FINGERPRINT_TEMPLATECOUNT);

            if (packet.data[0] == FINGERPRINT_OK) {
                return Arrays.copyOfRange(packet.data, 1, 3);
            } else {
                return new byte[2];
            }
        } catch (IOException e) {
            Log.e(TAG, "Error requesting templates num: ", e);
            return new byte[2];
        }
    }


    /**
     * Sends packet to module, waits for answer an returns it
     *
     * @param instruction Instruction code (identifier of function)
     * @return Response of the module
     * @throws IOException todo: how to describe that?
     */
    public R300Packet getPacket(byte instruction) throws IOException {

        return getPacket(instruction, null);
    }

    /**
     * TODO: Леня разберись
     *
     * @return
     */
    public byte GenImg() {
        try {
            R300Packet packet = getPacket(FINGERPRINT_GETIMAGE);

            if (packet.data[0] == FINGERPRINT_OK) {
                return FINGERPRINT_OK;
            } else if (packet.data[0] == FINGERPRINT_UPLOADFAIL) {
                return FINGERPRINT_UPLOADFAIL;
            } else {
                return FINGERPRINT_PACKETRECIEVEERR;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting address : ", e);
            return FINGERPRINT_PACKETRECIEVEERR;
        }
    }

    /**
     * to upload the character file or template of CharBuffer1/CharBuffer2 to upper computer
     * @return
     */
    public byte UpChar(){
        try {
            R300Packet packet = getPacket(FINGERPRINT_ENDDATAPACKET);

            if (packet.data[0] == FINGERPRINT_OK) {
                collectFingerPrint();
                return FINGERPRINT_OK;
            } else if (packet.data[0] == FINGERPRINT_UPLOADFAIL) {
                return FINGERPRINT_UPLOADFAIL;
            } else {
                return FINGERPRINT_PACKETRECIEVEERR;
            }
        } catch (IOException e) {
            Log.e(TAG, " error when uploading char");
            return FINGERPRINT_PACKETRECIEVEERR;
        }
    }

    public byte[] collectFingerPrint(){
        R300Packet packet = readStructuredPacket();
        return  packet.data;
    }

    /**
     * To command the Module to generate a random number and return it to upper computer
     *
     * @return Random number from the module
     */
    public byte[] GetRandomCode() {
        try {
            R300Packet packet = getPacket(FINGERPRINT_GETRANDOMCODE);

            if (packet.data[0] == FINGERPRINT_OK) {
                return Arrays.copyOfRange(packet.data, 1, 5);
            } else {
                return new byte[4];
            }
        } catch (IOException e) {
            Log.e(TAG, "Error requesting random code: ", e);
            return new byte[4];
        }
    }

    /**
     * Sends packet to module, waits for answer an returns it
     *
     * @param instruction Instruction code (identifier of function)
     * @param data        Data to be written
     * @return Response of the module
     * @throws IOException todo: how to describe that?
     */
    private R300Packet getPacket(byte instruction, byte[] data) throws IOException {
        writePacket(createCommand(instruction, data));
        return readStructuredPacket();
    }

    /**
     * Transforms given parameters to structured packet
     *
     * @param instruction Function's instruction code
     * @param data        Data to be written
     * @return Packet of given parameters
     */
    private R300Packet createCommand(byte instruction, byte[] data) {
        byte[] dataFull = new byte[data.length + 1];
        dataFull[0] = instruction;
        System.arraycopy(data, 0, dataFull, 1, data.length);
        return new R300Packet(this.mAddress, FINGERPRINT_COMMANDPACKET, dataFull);
    }

    /**
     * Writes packet to module
     *
     * @param packet Packet to be written
     * @throws IOException todo: how to describe that?
     */
    private void writePacket(R300Packet packet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        packet.write(byteArrayOutputStream);
        try {
            mDevice.write(byteArrayOutputStream.toByteArray(), byteArrayOutputStream.size());
            Log.v(TAG, String.format("Written to module: %s", byteArrayToHexString(byteArrayOutputStream.toByteArray())));
        } finally {
            byteArrayOutputStream.close();
        }
    }

    /**
     * Retrieves response of module
     *
     * @return Data given by module
     */
    private R300Packet readStructuredPacket() {
        R300Packet packet = new R300Packet();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int index = 0;
        int wireLength = 0;

        try {
            byte[] read = new byte[1];
            byte buf;

            while (true) {
                if (mDevice.read(read, 1) > 0) {
                    buf = read[0];
                    byteArrayOutputStream.write(read);

                    switch (index) {
                        case 0: // Start code 1
                            if (buf != R300Packet.FINGERPRINT_STARTCODE_1)
                                continue;
                            packet.header[index] = buf;
                            break;
                        case 1: // Start code 2
                            if (buf != R300Packet.FINGERPRINT_STARTCODE_2)
                                continue;
                            packet.header[index] = buf;
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            packet.adder[(index - 2)] = buf;
                            break;
                        case 6:
                            packet.pid = buf;
                            break;
                        case 7:
                        case 8:
                            packet.length[(index - 7)] = buf;
                            wireLength = (packet.length[0] << 8) | packet.length[1]; // transform to integer
                            break;
                        default:
                            packet.data[(index - 9)] = buf;
                            if ((index - 8) >= wireLength) {
                                Log.d(TAG, String.format("Read from module: %s", byteArrayToHexString(byteArrayOutputStream.toByteArray()))); // todo: delete after end of development
                                return packet;
                            }
                            break;
                    }

                    index++;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to transfer data over UART", e);
        }

        // Shouldn't get here so...
        return null;
    }
}

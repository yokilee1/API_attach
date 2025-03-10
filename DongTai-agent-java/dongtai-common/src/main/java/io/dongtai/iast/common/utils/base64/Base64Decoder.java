package io.dongtai.iast.common.utils.base64;


import io.dongtai.log.DongTaiLog;

import java.io.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class Base64Decoder extends CharacterDecoder {
    public static Base64Decoder instance;
    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    byte[] decode_buffer = new byte[4];

    public Base64Decoder() {
    }

    public static byte[] decodeBase64FromString(String data) {
        if (null == instance) {
            instance = new Base64Decoder();
        }
        try {
            return instance.decodeBuffer(data);
        } catch (IOException e) {
            DongTaiLog.trace("base64 decode from string failed: {}, {}", e.getMessage(), e.getCause());
        }
        return null;
    }

    @Override
    protected int bytesPerAtom() {
        return 4;
    }

    @Override
    protected int bytesPerLine() {
        return 72;
    }

    @Override
    protected void decodeAtom(PushbackInputStream var1, OutputStream var2, int var3) throws IOException {
        byte var5 = -1;
        byte var6 = -1;
        byte var7 = -1;
        byte var8 = -1;
        if (var3 < 2) {
            DongTaiLog.trace("BASE64Decoder: Not enough bytes for an atom.");
        } else {
            int var4;
            do {
                var4 = var1.read();
                if (var4 == -1) {
                    throw new IOException();
                }
            } while (var4 == 10 || var4 == 13);

            this.decode_buffer[0] = (byte) var4;
            var4 = this.readFully(var1, this.decode_buffer, 1, var3 - 1);
            if (var4 == -1) {
                throw new IOException();
            } else {
                if (var3 > 3 && this.decode_buffer[3] == 61) {
                    var3 = 3;
                }

                if (var3 > 2 && this.decode_buffer[2] == 61) {
                    var3 = 2;
                }

                switch (var3) {
                    case 4:
                        var8 = pem_convert_array[this.decode_buffer[3] & 255];
                    case 3:
                        var7 = pem_convert_array[this.decode_buffer[2] & 255];
                    case 2:
                        var6 = pem_convert_array[this.decode_buffer[1] & 255];
                        var5 = pem_convert_array[this.decode_buffer[0] & 255];
                    default:
                        switch (var3) {
                            case 2:
                                var2.write((byte) (var5 << 2 & 252 | var6 >>> 4 & 3));
                                break;
                            case 3:
                                var2.write((byte) (var5 << 2 & 252 | var6 >>> 4 & 3));
                                var2.write((byte) (var6 << 4 & 240 | var7 >>> 2 & 15));
                                break;
                            case 4:
                                var2.write((byte) (var5 << 2 & 252 | var6 >>> 4 & 3));
                                var2.write((byte) (var6 << 4 & 240 | var7 >>> 2 & 15));
                                var2.write((byte) (var7 << 6 & 192 | var8 & 63));
                        }

                }
            }
        }
    }

    static {
        int var0;
        for (var0 = 0; var0 < 255; ++var0) {
            pem_convert_array[var0] = -1;
        }

        for (var0 = 0; var0 < pem_array.length; ++var0) {
            pem_convert_array[pem_array[var0]] = (byte) var0;
        }

    }
}

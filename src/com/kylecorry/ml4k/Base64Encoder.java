package com.kylecorry.ml4k;

class Base64Encoder {

    /**
     * Encode a string into base64 format.
     * @param s The string to encode.
     * @return The input string, encoded into a base64 string.
     */
    public static String encode(String s){
        return encode(s.getBytes());
    }

    /**
     * Encode a byte array into base64 format.
     * @param bytes The byte array to encode.
     * @return The input byte array, encoded into a base64 string.
     */
    public static String encode(byte[] bytes){

        StringBuilder s = new StringBuilder();

        for (int i = 0; i < bytes.length; i+=3) {
            byte b1 = bytes[i];
            byte b2 = i+1 < bytes.length ? bytes[i+1] : 0;
            byte b3 = i+2 < bytes.length ? bytes[i+2] : 0;

            int current = packBytesIntoInt(byteToUnsignedInt(b1), byteToUnsignedInt(b2), byteToUnsignedInt(b3));
            s.append(lookup(byteToUnsignedInt(read6Bits(0, current))));
            s.append(lookup(byteToUnsignedInt(read6Bits(1, current))));
            if (i + 1 < bytes.length) {
                s.append(lookup(byteToUnsignedInt(read6Bits(2, current))));
            }
            if (i + 2 < bytes.length){
                s.append(lookup(byteToUnsignedInt(read6Bits(3, current))));
            }
        }

        int eqsNeeded = (bytes.length % 3);
        if (eqsNeeded == 2){
            s.append('=');
        } else if (eqsNeeded == 1){
            s.append("==");
        }

        return s.toString();
    }

    /**
     * Convert a byte to an unsigned int.
     * @param b The byte to convert.
     * @return The unsigned int version of the byte.
     */
    static int byteToUnsignedInt(byte b){
        return ((int) b) & 0xFF;
    }

    /**
     * Pack 3 bytes into an integer, from left to right.
     *  Byte 1  Byte 2  Byte 3  Zeros
     * 11111111222222223333333300000000
     * @param byte1 The left most byte
     * @param byte2 The middle byte
     * @param byte3 The right most byte
     * @return An integer containing all three bytes and right padded with zeros.
     */
    static int packBytesIntoInt(int byte1, int byte2, int byte3){
        return ((byte1 << 24) + (byte2 << 16) + (byte3 << 8));
    }

    /**
     * Read 6 bits from an integer.
     * @param index The index to read bits from [0, 1, 2, 3].
     * @param values The integer to get bits from.
     * @return The 6 bits from the values int, left padded with 2 zeros.
     */
    static byte read6Bits(int index, int values){
        if (index >= 4 || index < 0){
            return 0;
        }
        int i = index * 6;
        int mask = 0b111111 << (26 - i);
        return (byte) ((values & mask) >> (26 - i) & 0b00111111);
    }

    /**
     * Lookup a value in a base64 table.
     * @param val The value to lookup [0, 63]
     * @return The character associated with the value in base64.
     */
    private static char lookup(int val){
        if (val <= 25){
            return (char) ('A' + val);
        } else if (val <= 51){
            return (char) ('a' + val - 26);
        } else if (val <= 61){
            return (char) ('0' + val - 52);
        } else if (val == 62){
            return '+';
        } else {
            return '/';
        }
    }

}
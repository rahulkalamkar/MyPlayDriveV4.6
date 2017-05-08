package com.hungama.myplay.activity.data.audiocaching;

import java.io.UnsupportedEncodingException;

import com.hungama.myplay.activity.util.Logger;

public class CMEncryptor {

	private byte[] _key = null;
	private int _keyByteIndex = 0;

	public CMEncryptor(String deviceID) {
		_key = ScrambleKeyGenerator.generateKey(Integer.parseInt(deviceID));
		String str;
		try {
			str = new String(_key, "UTF-8");
			Logger.s("key generated :::::: " + str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void encrypt(byte[] bytes, int off, int len) {
		scramble(bytes, off, len);
	}

	public void decrypt(byte[] bytes, int off, int len) {
		scramble(bytes, off, len);
	}

	
   
    private void scramble(byte[] buffer, int offset, int length) {
        int temp,i, keylength = _key.length;
        for (i = offset; i < offset + length; i++) {
                buffer[i] = (byte) (buffer[i] ^ _key[_keyByteIndex]);
            if (_keyByteIndex + 1 >= keylength)
                _keyByteIndex = 0;
            else
                ++_keyByteIndex;
        }
    }

	// public void reset() {
	// _keyByteIndex = 0;
	// }

	// public void incPosition(long numBytes) {
	// _keyByteIndex = (_keyByteIndex + (int)numBytes) %
	// ScrambleKeyGenerator.getKeySize();
	// }
}

package com.raphfrk.craftproxylib.fields;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;

import com.raphfrk.craftproxylib.CraftProxyLib;
import com.raphfrk.craftproxylib.fields.elements.FieldBoolean;
import com.raphfrk.craftproxylib.fields.elements.FieldByte;
import com.raphfrk.craftproxylib.fields.elements.FieldByteSizedByteArray;
import com.raphfrk.craftproxylib.fields.elements.FieldDouble;
import com.raphfrk.craftproxylib.fields.elements.FieldEntityId;
import com.raphfrk.craftproxylib.fields.elements.FieldFloat;
import com.raphfrk.craftproxylib.fields.elements.FieldIntSizedByteArray;
import com.raphfrk.craftproxylib.fields.elements.FieldIntSizedByteArrayWithInt;
import com.raphfrk.craftproxylib.fields.elements.FieldShortSizedItemArray;
import com.raphfrk.craftproxylib.fields.elements.FieldIntSizedTripleByteArray;
import com.raphfrk.craftproxylib.fields.elements.FieldInteger;
import com.raphfrk.craftproxylib.fields.elements.FieldItem;
import com.raphfrk.craftproxylib.fields.elements.FieldLong;
import com.raphfrk.craftproxylib.fields.elements.FieldMetadata;
import com.raphfrk.craftproxylib.fields.elements.FieldShort;
import com.raphfrk.craftproxylib.fields.elements.FieldShortSizedByteArray;
import com.raphfrk.craftproxylib.fields.elements.FieldString;
import com.raphfrk.craftproxylib.fields.elements.FieldVelocity;
import com.raphfrk.craftproxylib.packet.Packet;

/**
 * Represents a packet field
 */
public abstract class Field {
	
	/**
	 * Expanded version of the field map
	 */
	private static final Field[][] expandedFieldMap = new Field[256][];

	/**
	 * Compressed version of the field map.  Consecutive fixed length fields are combined into single fields.
	 */
	private static final Field[][] compressedFieldMap = new Field[256][];
	
	/**
	 * EntityId information about a packet
	 */
	private static final int[][] entityIdInfo = new int[256][];
	
	/**
	 * Field array for use with ArrayList<Field>.toArray(Field[] fieldArray)
	 */
	private static final Field[] forToArray = new Field[0];

	private final static FieldBoolean fBoolean = new FieldBoolean();	
	private final static FieldByte fByte = new FieldByte(false);
	private final static FieldByte fUByte = new FieldByte(true);
	private final static FieldShort fShort = new FieldShort();
	private final static FieldInteger fInt = new FieldInteger();
	private final static FieldLong fLong = new FieldLong();
	
	private final static FieldFloat fFloat = new FieldFloat();
	private final static FieldDouble fDouble = new FieldDouble();
	
	private final static FieldString fString = new FieldString();
	
	private final static FieldEntityId fEId = new FieldEntityId();

	private final static FieldIntSizedByteArrayWithInt fIntSizedByteArrayWithInt = new FieldIntSizedByteArrayWithInt();
	private final static FieldIntSizedByteArray fIntSizedByteArray = new FieldIntSizedByteArray();
	private final static FieldIntSizedTripleByteArray fIntSizedTripleByteArray = new FieldIntSizedTripleByteArray();

	private final static FieldShortSizedByteArray fShortSizedByteArray = new FieldShortSizedByteArray();
	
	private final static FieldByteSizedByteArray fByteSizedByteArray = new FieldByteSizedByteArray();
	
	private final static FieldItem fItem = new FieldItem();
	private final static FieldShortSizedItemArray fShortSizedItemArray = new FieldShortSizedItemArray();
	private final static FieldVelocity fVelocity = new FieldVelocity();
	private final static FieldMetadata fMetadata = new FieldMetadata();
	
	static {
		
		setupExpandedFieldMap();

		compressFieldMap();
	}
	
	public static void addSpoutPacket195() {
		setPacketFields(0xC3, new Field[] {fInt, fIntSizedByteArray});
	}
	
	public static void setPacketFields(int i, Field[] fields) {
		expandedFieldMap[i] = fields;
		compressFieldMap();
	}
	
	private static void setupExpandedFieldMap() {
		
		Field[][] map = expandedFieldMap;
		
		// Don't include the packet id byte
		map[0x00] = new Field[] {fInt};
		map[0x01] = new Field[] {fInt, fString, fString, fInt, fInt, fByte, fUByte, fUByte};
		map[0x02] = new Field[] {fString};
		map[0x03] = new Field[] {fString};
		map[0x04] = new Field[] {fLong};
		map[0x05] = new Field[] {fEId, fShort, fShort, fShort};
		map[0x06] = new Field[] {fInt, fShort, fShort, fShort};
		map[0x06] = new Field[] {fInt, fInt, fInt};
		map[0x07] = new Field[] {fEId, fEId, fBoolean};
		map[0x08] = new Field[] {fShort, fShort, fFloat};
		map[0x09] = new Field[] {fInt, fByte, fByte, fShort, fString};
		map[0x0A] = new Field[] {fBoolean};
		map[0x0B] = new Field[] {fDouble, fDouble, fDouble, fDouble, fBoolean};
		map[0x0C] = new Field[] {fFloat, fFloat, fBoolean};
		map[0x0D] = new Field[] {fDouble, fDouble, fDouble, fDouble, fFloat, fFloat, fBoolean};
		map[0x0E] = new Field[] {fByte, fInt, fByte, fInt, fByte};
		map[0x0F] = new Field[] {fInt, fByte, fInt, fByte, fItem};
		map[0x10] = new Field[] {fShort};
		map[0x11] = new Field[] {fEId, fByte, fInt, fByte, fInt};
		map[0x12] = new Field[] {fEId, fByte};
		map[0x13] = new Field[] {fEId, fByte};
		map[0x14] = new Field[] {fEId, fString, fInt, fInt, fInt, fByte, fByte, fShort};
		map[0x15] = new Field[] {fEId, fShort, fByte, fShort, fInt, fInt, fInt, fByte, fByte, fByte};
		map[0x16] = new Field[] {fEId, fEId};
		map[0x17] = new Field[] {fEId, fByte, fInt, fInt, fInt, fVelocity};
		map[0x18] = new Field[] {fEId, fByte, fInt, fInt, fInt, fByte, fByte, fByte, fMetadata};
		map[0x19] = new Field[] {fEId, fString, fInt, fInt, fInt, fInt};
		map[0x1A] = new Field[] {fEId, fInt, fInt, fInt, fShort};
		map[0x1C] = new Field[] {fEId, fShort, fShort, fShort};
		map[0x1D] = new Field[] {fEId};
		map[0x1E] = new Field[] {fEId};
		map[0x1F] = new Field[] {fEId, fByte, fByte, fByte};
		map[0x20] = new Field[] {fEId, fByte, fByte};
		map[0x21] = new Field[] {fEId, fByte, fByte, fByte, fByte, fByte};
		map[0x22] = new Field[] {fEId, fInt, fInt, fInt, fByte, fByte};
		map[0x23] = new Field[] {fEId, fByte};
		map[0x26] = new Field[] {fEId, fByte};
		map[0x27] = new Field[] {fEId, fEId};
		map[0x28] = new Field[] {fEId, fMetadata};
		map[0x29] = new Field[] {fEId, fByte, fByte, fShort};
		map[0x2A] = new Field[] {fEId, fByte};
		map[0x2B] = new Field[] {fFloat, fShort, fShort};
		map[0x32] = new Field[] {fInt, fInt, fBoolean};
		map[0x33] = new Field[] {fInt, fInt, fBoolean, fShort, fShort, fIntSizedByteArrayWithInt};
		map[0x34] = new Field[] {fInt, fInt, fShort, fIntSizedByteArray};
		map[0x35] = new Field[] {fInt, fByte, fInt, fByte, fByte};
		map[0x36] = new Field[] {fInt, fShort, fInt, fByte, fByte};
		map[0x3C] = new Field[] {fDouble, fDouble, fDouble, fFloat, fIntSizedTripleByteArray};
		map[0x3D] = new Field[] {fInt, fInt, fByte, fInt, fInt};
		map[0x46] = new Field[] {fByte, fByte};
		map[0x47] = new Field[] {fEId, fBoolean, fInt, fInt, fInt};
		map[0x64] = new Field[] {fByte, fByte, fString, fByte};
		map[0x65] = new Field[] {fByte};
		map[0x66] = new Field[] {fByte, fShort, fByte, fShort, fBoolean, fItem};
		map[0x67] = new Field[] {fByte, fShort, fItem};
		map[0x68] = new Field[] {fByte, fShortSizedItemArray};
		map[0x69] = new Field[] {fByte, fShort, fShort};
		map[0x6A] = new Field[] {fByte, fShort, fBoolean};
		map[0x6B] = new Field[] {fShort, fItem};
		map[0x6C] = new Field[] {fByte, fByte};
		map[0x82] = new Field[] {fInt, fShort, fInt, fString, fString, fString, fString};
		map[0x83] = new Field[] {fShort, fShort, fByteSizedByteArray};
		map[0x84] = new Field[] {fInt, fShort, fInt, fByte, fInt, fInt, fInt};
		map[0xC8] = new Field[] {fInt, fByte};
		map[0xC9] = new Field[] {fString, fBoolean, fShort};
		map[0xCA] = new Field[] {fBoolean, fBoolean, fBoolean, fBoolean};
		map[0xFA] = new Field[] {fString, fShortSizedByteArray};
		map[0xFE] = new Field[] {};
		map[0xFF] = new Field[] {fString};

	}
	
	/**
	 * Compresses the expanded field map
	 */
	private static void compressFieldMap() {
		for (int i = 0; i < 256; i++) {
			Field[] expFields = expandedFieldMap[i];
			int pos = 1;
			if (expFields != null) {
				ArrayList<Field> compFields = new ArrayList<Field>();
				int len = -1;
				for (int f = 0; f < expFields.length; f++) {
					Field field = expFields[f];
					if (field instanceof FieldEntityId) {
						if (pos != -1) {
							entityIdInfo[i] = arrayAppend(entityIdInfo[i], pos);
						} else {
							CraftProxyLib.getLogger().log(Level.SEVERE, "Unable to generate entity id static map");
						}
					}
					int fieldLength = field.getFixedLength();
					if (fieldLength != -1) {
						if (len == -1) {
							len = fieldLength;
						} else {
							len += fieldLength;
						}
						if (pos != -1) {
							pos += fieldLength;
						}
					} else {
						pos = -1;
						if (len != -1) {
							compFields.add(new FieldFixedLength(len));
							len = -1;
						}
						compFields.add(field);
					}
				}
				if (len != -1) {
					compFields.add(new FieldFixedLength(len));
					len = -1;
				}
				compressedFieldMap[i] = compFields.toArray(forToArray);
			}
		}	
		for (int i = 0; i < 256; i++) {
			
		}
	}
	
	public static Field[] getCompressedFields(int id) {
		return compressedFieldMap[id];
	}
	
	public static Field[] getExpandedFields(int id) {
		return expandedFieldMap[id];
	}
	
	public static int[] getEntityIdInfo(int i) {
		return entityIdInfo[i];
	}
	
	/**
	 * Skips over a packet field from an InputStream.<br>
	 * 
	 * @param in the input stream
	 * @param position the initial position in the Packet buffer
	 * @param packet the Packet to place the result
	 * @return the length of the field
	 */
	public abstract int skip(InputStream in) throws IOException;
	
	/**
	 * Reads a packet field from a InputStream.<br>
	 * 
	 * @param in the input stream
	 * @param position the initial position in the Packet buffer
	 * @param packet the Packet to place the result
	 * @return the value of the field
	 */
	public abstract Object read(InputStream in) throws IOException;
	
	/**
	 * Writes a packet field to a byte array.<br>
	 * <br>
	 * NOTE: the method will never attempt to write beyond the end of the array.  In that case, the new position will be greater than the length of the array but there is no guarantee which bytes in the array, if any, will be written to.
	 * 
	 * @param buf the byte array
	 * @param pos the start position
	 * @param value
	 * @return the new position
	 */
	public int write(byte[] buf, int pos, Object value) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	/**
	 * Returns the length of the field for fixed length fields
	 * 
	 * @return the field length or -1 if the field has a variable length
	 */
	public int getFixedLength() {
		return -1;
	}
	
	/**
	 * Returns a new instance of the field
	 * 
	 * @return
	 */
	public abstract Field newInstance();
	
	/**
	 * Reads a packet field from a Packet.
	 * 
	 * @param packet the Packet
	 * @param the field index
	 * @return the value of the field
	 */
	public static Object read(Packet packet, int i) throws IOException {
		int id = packet.getId();
		if (id < 0) {
			return null;
		}
		
		packet.reset(1);
		packet.setInputStream(null);
		
		Field[] fields = expandedFieldMap[id];
		
		for (int j = 0; j < i; j++) {
			fields[j].skip(packet);
		}
		
		try {
			return fields[i].read(packet);
		} finally {
			packet.reset();
		}
	}
	
	/**
	 * Reads the packet fields from a Packet.
	 * 
	 * @param packet the Packet
	 * @return an array containing the values of the fields
	 */
	public static Object[] readAll(Packet packet) throws IOException {
		int id = packet.getId();
		if (id < 0) {
			return null;
		}
		
		packet.reset();
		packet.setInputStream(null);
		
		Field[] fields = expandedFieldMap[id];
		
		Object[] values = new Object[fields.length];
		
		for (int i = 0; i < fields.length; i++) {
			values[i] = fields[i].read(packet);
		}
		
		return values;
	}
	
	/**
	 * Creates a String based on an array of Fields
	 * 
	 * @param fields the field array
	 * @return the String representation
	 */
	public static String fieldArrayToString(Field[] fields) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Field f : fields) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(f.getClass().getSimpleName() + "(" + f.getFixedLength() + ")");
		}
		return sb.toString();
	}
	
	public static int getByte(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
			throw new EOFException("EOF reached");
		} else {
			return b;
		}
	}
	
	private static int[] arrayAppend(int[] oldArray, int b) {
		if (oldArray == null) {
			return new int[] {b};
		} else {
			int[] newArray = new int[oldArray.length + 1];
			for (int i = 0; i < oldArray.length; i++) {
				newArray[i] = oldArray[i];
			}
			newArray[oldArray.length] = b;
			return newArray;
		}
	}
	
}

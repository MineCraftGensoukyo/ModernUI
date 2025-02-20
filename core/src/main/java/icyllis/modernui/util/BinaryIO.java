/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.util;

import icyllis.modernui.annotation.*;
import icyllis.modernui.text.TextUtils;

import javax.annotation.concurrent.GuardedBy;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provides static methods for performing binary I/O on various data objects.
 *
 * @since 3.7
 */
@WorkerThread
public final class BinaryIO {

    /**
     * Value types, version 3.7, do not change.
     */
    private static final byte VAL_NULL = 0;
    private static final byte
            VAL_BYTE = 1,
            VAL_SHORT = 2,
            VAL_INT = 3,
            VAL_LONG = 4,
            VAL_FLOAT = 5,
            VAL_DOUBLE = 6,
            VAL_BOOLEAN = 7,
            VAL_CHAR = 8;
    private static final byte
            VAL_BYTE_ARRAY = 9,
            VAL_SHORT_ARRAY = 10,
            VAL_INT_ARRAY = 11,
            VAL_LONG_ARRAY = 12,
            VAL_FLOAT_ARRAY = 13,
            VAL_DOUBLE_ARRAY = 14,
            VAL_BOOLEAN_ARRAY = 15,
            VAL_CHAR_ARRAY = 16;
    private static final byte
            VAL_STRING = 17,
            VAL_UUID = 19,
            VAL_INSTANT = 20;
    private static final byte
            VAL_DATA_SET = 64,
            VAL_PARCELABLE = 65,
            VAL_CHAR_SEQUENCE = 66,
            VAL_LIST = 68,
            VAL_OBJECT_ARRAY = 118,
            VAL_SERIALIZABLE = 127;

    @GuardedBy("gCreators")
    private static final ConcurrentHashMap<ClassLoader, ConcurrentHashMap<String, Parcelable.Creator<?>>>
            gCreators = new ConcurrentHashMap<>();

    /**
     * Reads a compressed DataSet from a GZIP file.
     * <p>
     * The stream should be a FileInputStream or a ChannelInputStream over FileChannel,
     * and will be closed after the method call.
     *
     * @param stream a FileInputStream or a ChannelInputStream over FileChannel
     * @return the data set
     */
    @NonNull
    public static DataSet inflate(@NonNull InputStream stream,
                                  @Nullable ClassLoader loader) throws IOException {
        try (var in = new DataInputStream(new GZIPInputStream(
                new BufferedInputStream(stream, 4096)))) {
            var res = readDataSet(in, loader);
            if (res == null) {
                throw new IOException("Insufficient data");
            }
            return res;
        }
    }

    /**
     * Writes and compresses a DataSet to a GZIP file.
     * <p>
     * The stream should be a FileOutputStream or a ChannelOutputStream over FileChannel,
     * and will be closed after the method call.
     *
     * @param stream a FileOutputStream or a ChannelOutputStream over FileChannel
     * @param source the data set
     */
    public static void deflate(@NonNull OutputStream stream,
                               @NonNull DataSet source) throws IOException {
        try (var out = new DataOutputStream(new GZIPOutputStream(
                new BufferedOutputStream(stream, 4096)))) {
            writeDataSet(out, source);
        }
    }

    /**
     * Write a value and its type.
     *
     * @param out the data output
     * @param v   the value to write
     * @throws IOException if an IO error occurs
     */
    public static void writeValue(@NonNull DataOutput out, @Nullable Object v) throws IOException {
        if (v == null) {
            out.writeByte(VAL_NULL);
        } else if (v instanceof String) {
            out.writeByte(VAL_STRING);
            writeString(out, (String) v);
        } else if (v instanceof Integer) {
            out.writeByte(VAL_INT);
            out.writeInt((Integer) v);
        } else if (v instanceof Long) {
            out.writeByte(VAL_LONG);
            out.writeLong((Long) v);
        } else if (v instanceof Float) {
            out.writeByte(VAL_FLOAT);
            out.writeFloat((Float) v);
        } else if (v instanceof Double) {
            out.writeByte(VAL_DOUBLE);
            out.writeDouble((Double) v);
        } else if (v instanceof Byte) {
            out.writeByte(VAL_BYTE);
            out.writeByte((Byte) v);
        } else if (v instanceof Short) {
            out.writeByte(VAL_SHORT);
            out.writeShort((Short) v);
        } else if (v instanceof Character) {
            out.writeByte(VAL_CHAR);
            out.writeChar((Character) v);
        } else if (v instanceof Boolean) {
            out.writeByte(VAL_BOOLEAN);
            out.writeBoolean((Boolean) v);
        } else if (v instanceof UUID value) {
            out.writeByte(VAL_UUID);
            out.writeLong(value.getMostSignificantBits());
            out.writeLong(value.getLeastSignificantBits());
        } else if (v instanceof Instant value) {
            out.writeByte(VAL_INSTANT);
            out.writeLong(value.getEpochSecond());
            out.writeInt(value.getNano());
        } else if (v instanceof int[]) {
            out.writeByte(VAL_INT_ARRAY);
            writeIntArray(out, (int[]) v);
        } else if (v instanceof byte[]) {
            out.writeByte(VAL_BYTE_ARRAY);
            writeByteArray(out, (byte[]) v);
        } else if (v instanceof char[]) {
            out.writeByte(VAL_CHAR_ARRAY);
            writeCharArray(out, (char[]) v);
        } else if (v instanceof DataSet) {
            out.writeByte(VAL_DATA_SET);
            writeDataSet(out, (DataSet) v);
        } else if (v instanceof Parcelable value) {
            out.writeByte(VAL_PARCELABLE);
            writeString(out, value.getClass().getName());
            value.write(out);
        } else if (v instanceof CharSequence) {
            out.writeByte(VAL_CHAR_SEQUENCE);
            TextUtils.write(out, (CharSequence) v);
        } else if (v instanceof List) {
            out.writeByte(VAL_LIST);
            writeList(out, (List<?>) v);
        } else if (v instanceof long[]) {
            out.writeByte(VAL_LONG_ARRAY);
            writeLongArray(out, (long[]) v);
        } else if (v instanceof short[]) {
            out.writeByte(VAL_SHORT_ARRAY);
            writeShortArray(out, (short[]) v);
        } else if (v instanceof float[]) {
            out.writeByte(VAL_FLOAT_ARRAY);
            writeFloatArray(out, (float[]) v);
        } else if (v instanceof double[]) {
            out.writeByte(VAL_DOUBLE_ARRAY);
            writeDoubleArray(out, (double[]) v);
        } else if (v instanceof boolean[]) {
            out.writeByte(VAL_BOOLEAN_ARRAY);
            writeBooleanArray(out, (boolean[]) v);
        } else {
            Class<?> clazz = v.getClass();
            if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                // pure Object[]
                out.writeByte(VAL_OBJECT_ARRAY);
                writeArray(out, (Object[]) v);
            } else if (v instanceof Serializable value) {
                out.writeByte(VAL_SERIALIZABLE);
                writeString(out, value.getClass().getName());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(value);
                oos.close();
                writeByteArray(out, baos.toByteArray());
            }
            // others are silently ignored
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T readValue(@NonNull DataInput in, @Nullable ClassLoader loader,
                                  @Nullable Class<T> clazz, @Nullable Class<?> elemType) throws IOException {
        final byte type = in.readByte();
        final Object object = switch (type) {
            case VAL_NULL -> null;
            case VAL_BYTE -> in.readByte();
            case VAL_SHORT -> in.readShort();
            case VAL_INT -> in.readInt();
            case VAL_LONG -> in.readLong();
            case VAL_FLOAT -> in.readFloat();
            case VAL_DOUBLE -> in.readDouble();
            case VAL_BOOLEAN -> in.readBoolean();
            case VAL_CHAR -> in.readChar();
            case VAL_BYTE_ARRAY -> readByteArray(in);
            case VAL_SHORT_ARRAY -> readShortArray(in);
            case VAL_INT_ARRAY -> readIntArray(in);
            case VAL_LONG_ARRAY -> readLongArray(in);
            case VAL_FLOAT_ARRAY -> readFloatArray(in);
            case VAL_DOUBLE_ARRAY -> readDoubleArray(in);
            case VAL_BOOLEAN_ARRAY -> readBooleanArray(in);
            case VAL_CHAR_ARRAY -> readCharArray(in);
            case VAL_STRING -> readString(in);
            case VAL_UUID -> new UUID(in.readLong(), in.readLong());
            case VAL_INSTANT -> Instant.ofEpochSecond(in.readLong(), in.readInt());
            case VAL_DATA_SET -> readDataSet(in, loader);
            case VAL_PARCELABLE -> readParcelable0(in, loader, clazz);
            case VAL_CHAR_SEQUENCE -> TextUtils.read(in);
            case VAL_LIST -> readList(in, loader, elemType);
            case VAL_OBJECT_ARRAY -> {
                if (elemType == null) {
                    elemType = Object.class;
                }
                if (clazz != null) {
                    if (!clazz.isArray()) {
                        throw new IOException("About to read an array but type "
                                + clazz.getCanonicalName()
                                + " required by caller is not an array.");
                    }
                    Class<?> itemArrayType = elemType.arrayType();
                    if (!clazz.isAssignableFrom(itemArrayType)) {
                        throw new IOException("About to read a " + itemArrayType.getCanonicalName()
                                + ", which is not a subtype of type " + clazz.getCanonicalName()
                                + " required by caller.");
                    }
                }
                yield readArray(in, loader, elemType);
            }
            default -> throw new IOException("Unknown value type identifier: " + type);
        };
        if (object != null && clazz != null && !clazz.isInstance(object)) {
            throw new IOException("Deserialized object " + object
                    + " is not an instance of required class " + clazz.getName()
                    + " provided in the parameter");
        }
        return (T) object;
    }

    @Nullable
    public static <T> T readParcelable(@NonNull DataInput in, @Nullable ClassLoader loader,
                                       @NonNull Class<T> clazz) throws IOException {
        return readParcelable0(in, loader, Objects.requireNonNull(clazz));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T readParcelable0(@NonNull DataInput in, @Nullable ClassLoader loader,
                                        @Nullable Class<T> clazz) throws IOException {
        Parcelable.Creator<?> creator = readParcelableCreator0(in, loader, clazz);
        if (creator == null) {
            return null;
        }
        if (creator instanceof Parcelable.ClassLoaderCreator<?>) {
            return (T) ((Parcelable.ClassLoaderCreator<?>) creator).create(in, loader);
        }
        return (T) creator.create(in);
    }

    @Nullable
    public static <T> Parcelable.Creator<T> readParcelableCreator(
            @NonNull DataInput in, @Nullable ClassLoader loader,
            @NonNull Class<T> clazz) throws IOException {
        return readParcelableCreator0(in, loader, Objects.requireNonNull(clazz));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> Parcelable.Creator<T> readParcelableCreator0(
            @NonNull DataInput in, @Nullable ClassLoader loader,
            @Nullable Class<T> clazz) throws IOException {
        final var name = readString(in);
        if (name == null) {
            return null;
        }
        final var map = gCreators.computeIfAbsent(loader, __ -> new ConcurrentHashMap<>());
        var creator = map.get(name);
        if (creator != null) {
            if (clazz != null) {
                var target = creator.getClass().getEnclosingClass();
                if (!clazz.isAssignableFrom(target)) {
                    throw new RuntimeException("Parcelable creator " + name + " is not "
                            + "a subclass of required class " + clazz.getName()
                            + " provided in the parameter");
                }
            }
            return (Parcelable.Creator<T>) creator;
        }

        try {
            var target = (loader == null ? BinaryIO.class.getClassLoader() : loader)
                    .loadClass(name);
            if (!Parcelable.class.isAssignableFrom(target)) {
                throw new RuntimeException("Parcelable protocol requires subclassing "
                        + "from Parcelable on class " + name);
            }
            if (clazz != null) {
                if (!clazz.isAssignableFrom(target)) {
                    throw new RuntimeException("Parcelable creator " + name + " is not "
                            + "a subclass of required class " + clazz.getName()
                            + " provided in the parameter");
                }
            }
            var f = target.getField("CREATOR");
            if ((f.getModifiers() & Modifier.STATIC) == 0) {
                throw new RuntimeException("Parcelable protocol requires "
                        + "the CREATOR object to be static on class " + name);
            }
            if (!Parcelable.Creator.class.isAssignableFrom(f.getType())) {
                throw new RuntimeException("Parcelable protocol requires a "
                        + "Parcelable.Creator object called "
                        + "CREATOR on class " + name);
            }
            creator = (Parcelable.Creator<?>) f.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called "
                    + "CREATOR on class " + name, e);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (creator == null) {
            throw new RuntimeException("Parcelable protocol requires a "
                    + "non-null Parcelable.Creator object called "
                    + "CREATOR on class " + name);
        }

        // Modern UI: just like Android source code, there's always a race
        map.put(name, creator);

        return (Parcelable.Creator<T>) creator;
    }

    /**
     * Write a byte array.
     *
     * @param out the data output
     * @param b   the bytes to write
     * @throws IOException if an IO error occurs
     */
    public static void writeByteArray(@NonNull DataOutput out,
                                      @Nullable byte[] b) throws IOException {
        if (b == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(b.length);
        out.write(b);
    }

    /**
     * Write a byte array.
     *
     * @param out the data output
     * @param b   the bytes to write
     * @throws IOException if an IO error occurs
     */
    public static void writeByteArray(@NonNull DataOutput out,
                                      @Nullable byte[] b, int off, int len) throws IOException {
        if (b == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(len);
        out.write(b, off, len);
    }

    @Nullable
    public static byte[] readByteArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        byte[] b = new byte[n];
        in.readFully(b, 0, n);
        return b;
    }

    /**
     * Write a short array.
     *
     * @param out   the data output
     * @param value the short array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeShortArray(@NonNull DataOutput out,
                                       @Nullable short[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (short e : value)
            out.writeShort(e);
    }

    @Nullable
    public static short[] readShortArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        short[] value = new short[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readShort();
        return value;
    }

    /**
     * Write an int array.
     *
     * @param out   the data output
     * @param value the int array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeIntArray(@NonNull DataOutput out,
                                     @Nullable int[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (int e : value)
            out.writeInt(e);
    }

    @Nullable
    public static int[] readIntArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        int[] value = new int[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readInt();
        return value;
    }

    /**
     * Write a long array.
     *
     * @param out   the data output
     * @param value the long array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeLongArray(@NonNull DataOutput out,
                                      @Nullable long[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (long e : value)
            out.writeLong(e);
    }

    @Nullable
    public static long[] readLongArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        long[] value = new long[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readLong();
        return value;
    }

    /**
     * Write a float array.
     *
     * @param out   the data output
     * @param value the float array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeFloatArray(@NonNull DataOutput out,
                                       @Nullable float[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (float e : value)
            out.writeFloat(e);
    }

    @Nullable
    public static float[] readFloatArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        float[] value = new float[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readFloat();
        return value;
    }

    /**
     * Write a double array.
     *
     * @param out   the data output
     * @param value the double array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeDoubleArray(@NonNull DataOutput out,
                                        @Nullable double[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (double e : value)
            out.writeDouble(e);
    }

    @Nullable
    public static double[] readDoubleArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        double[] value = new double[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readDouble();
        return value;
    }

    /**
     * Write a boolean array.
     *
     * @param out   the data output
     * @param value the boolean array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeBooleanArray(@NonNull DataOutput out,
                                         @Nullable boolean[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (boolean e : value)
            out.writeBoolean(e);
    }

    @Nullable
    public static boolean[] readBooleanArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        boolean[] value = new boolean[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readBoolean();
        return value;
    }

    /**
     * Write a char array.
     *
     * @param out   the data output
     * @param value the char array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeCharArray(@NonNull DataOutput out,
                                      @Nullable char[] value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(value.length);
        for (char e : value)
            out.writeChar(e);
    }

    @Nullable
    public static char[] readCharArray(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        char[] value = new char[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readChar();
        return value;
    }

    /**
     * Write an object array.
     *
     * @param out the data output
     * @param a   the object array to write
     * @throws IOException if an IO error occurs
     */
    public static void writeArray(@NonNull DataOutput out,
                                  @Nullable Object[] a) throws IOException {
        if (a == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(a.length);
        for (var e : a) {
            writeValue(out, e);
        }
    }

    /**
     * Read an object array.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T[] readArray(@NonNull DataInput in, @Nullable ClassLoader loader,
                                    @NonNull Class<T> clazz) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        T[] a = (T[]) (clazz == Object.class ? new Object[n] : Array.newInstance(clazz, n));
        for (int i = 0; i < n; i++) {
            T value = readValue(in, loader, clazz, null);
            a[i] = value;
        }
        return a;
    }

    /**
     * Write a string.
     *
     * @param out the data output
     * @param s   the string to write
     * @throws IOException if an IO error occurs
     */
    public static void writeString(@NonNull DataOutput out, @Nullable String s) throws IOException {
        writeString16(out, s);
    }

    /**
     * Write a string in UTF-8 format.
     *
     * @param out the data output
     * @param s   the string to write
     * @throws IOException if an IO error occurs
     */
    public static void writeString8(@NonNull DataOutput out, @Nullable String s) throws IOException {
        if (s == null) {
            out.writeInt(-1);
        } else {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    /**
     * Write a string in UTF-16 BE format.
     *
     * @param out the data output
     * @param s   the string to write
     * @throws IOException if an IO error occurs
     */
    public static void writeString16(@NonNull DataOutput out, @Nullable String s) throws IOException {
        if (s == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(s.length());
            out.writeChars(s);
        }
    }

    /**
     * Read a string.
     *
     * @param in the data input
     * @throws IOException if an IO error occurs
     */
    @Nullable
    public static String readString(@NonNull DataInput in) throws IOException {
        return readString16(in);
    }

    /**
     * Read a string in UTF-8 format.
     *
     * @param in the data input
     * @throws IOException if an IO error occurs
     */
    @Nullable
    public static String readString8(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        byte[] bytes = new byte[n];
        in.readFully(bytes, 0, n);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read a string in UTF-16 BE format.
     *
     * @param in the data input
     * @throws IOException if an IO error occurs
     */
    @Nullable
    public static String readString16(@NonNull DataInput in) throws IOException {
        int n = in.readInt();
        if (n < 0)
            return null;
        char[] value = new char[n];
        for (int i = 0; i < n; i++)
            value[i] = in.readChar();
        return new String(value);
    }

    /**
     * Write a list.
     *
     * @param out  the data output
     * @param list the list to write
     * @throws IOException if an IO error occurs
     */
    public static void writeList(@NonNull DataOutput out,
                                 @Nullable List<?> list) throws IOException {
        if (list == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(list.size());
        for (var e : list) {
            writeValue(out, e);
        }
    }

    /**
     * Read a list as a value.
     *
     * @param in the data input
     * @return the newly created list
     * @throws IOException if an IO error occurs
     */
    @Nullable
    private static <T> List<T> readList(@NonNull DataInput in, @Nullable ClassLoader loader,
                                        @Nullable Class<? extends T> clazz) throws IOException {
        int n = in.readInt();
        if (n < 0) {
            return null;
        }
        var res = new ArrayList<T>(n);
        while (n-- != 0) {
            res.add(readValue(in, loader, clazz, null));
        }
        return res;
    }

    /**
     * Write a data set.
     *
     * @param out    the data output
     * @param source the data set to write
     * @throws IOException if an IO error occurs
     */
    public static void writeDataSet(@NonNull DataOutput out,
                                    @Nullable DataSet source) throws IOException {
        if (source == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(source.size());
        var it = source.new FastEntryIterator();
        while (it.hasNext()) {
            var e = it.next();
            writeString(out, e.getKey());
            writeValue(out, e.getValue());
        }
    }

    /**
     * Read a data set as a value.
     *
     * @param in     the data input
     * @param loader the class loader for {@link Parcelable} classes
     * @return the newly created data set
     * @throws IOException if an IO error occurs
     */
    @Nullable
    public static DataSet readDataSet(@NonNull DataInput in,
                                      @Nullable ClassLoader loader) throws IOException {
        int n = in.readInt();
        if (n < 0) {
            return null;
        }
        var res = new DataSet(n);
        while (n-- != 0) {
            res.put(readString(in), readValue(in, loader, null, null));
        }
        return res;
    }
}

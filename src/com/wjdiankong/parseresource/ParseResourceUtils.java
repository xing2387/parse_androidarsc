package com.wjdiankong.parseresource;

import com.wjdiankong.parseresource.type.*;

import java.util.ArrayList;

public class ParseResourceUtils {

    private static int resStringPoolChunkOffset;//�ַ����ص�ƫ��ֵ
    private static int packageChunkOffset;//�����ݵ�ƫ��ֵ
    private static int keyStringPoolChunkOffset;//key�ַ����ص�ƫ��ֵ
    private static int typeStringPoolChunkOffset;//�����ַ����ص�ƫ��ֵ

    //������Դ�����͵�ƫ��ֵ
    private static int resTypeOffset;

    private static ArrayList<String> resStringList = new ArrayList<String>();//���е��ַ�����
    private static ArrayList<String> keyStringList = new ArrayList<String>();//���е���Դkey��ֵ�ĳ�
    private static ArrayList<String> typeStringList = new ArrayList<String>();//�������͵�ֵ�ĳ�

    //��Դ����id������id
    private static int packId;
    private static int resTypeId;

    /**
     * ����ͷ����Ϣ
     *
     * @param src
     */
    public static void parseResTableHeaderChunk(byte[] src) {
        ResTableHeader resTableHeader = new ResTableHeader();

        resTableHeader.header = parseResChunkHeader(src, 0);

        resStringPoolChunkOffset = resTableHeader.header.headerSize;

        //����PackageCount����(һ��apk���ܰ������Package��Դ)
        byte[] packageCountByte = Utils.copyByte(src, resTableHeader.header.getHeaderSize(), 4);
        resTableHeader.packageCount = Utils.byte2int(packageCountByte);

    }

    /**
     * ����Resource.arsc�ļ��������ַ�������
     *
     * @param src
     */
    public static void parseResStringPoolChunk(byte[] src) {
        ResStringPoolHeader stringPoolHeader = parseStringPoolChunk(src, resStringList, resStringPoolChunkOffset);
        packageChunkOffset = resStringPoolChunkOffset + stringPoolHeader.header.size;
    }

    /**
     * ����Package��Ϣ
     *
     * @param src
     */
    public static void parsePackage(byte[] src) {
        System.out.println("pchunkoffset:" + Utils.bytesToHexString(Utils.int2Byte(packageChunkOffset)));
        ResTablePackage resTabPackage = new ResTablePackage();
        //����ͷ����Ϣ
        resTabPackage.header = parseResChunkHeader(src, packageChunkOffset);

        System.out.println("package size:" + resTabPackage.header.headerSize);

        int offset = packageChunkOffset + resTabPackage.header.getHeaderSize();

        //����packId
        byte[] idByte = Utils.copyByte(src, offset, 4);
        resTabPackage.id = Utils.byte2int(idByte);
        packId = resTabPackage.id;

        //��������
        System.out.println("package offset:" + Utils.bytesToHexString(Utils.int2Byte(offset + 4)));
        byte[] nameByte = Utils.copyByte(src, offset + 4, 128 * 2);//�����128������ֶεĴ�С�����Բ鿴����˵������char���͵ģ�����Ҫ����2
        String packageName = new String(nameByte);
        packageName = Utils.filterStringNull(packageName);
        System.out.println("pkgName:" + packageName);

        //���������ַ�����ƫ��ֵ
        byte[] typeStringsByte = Utils.copyByte(src, offset + 4 + 128 * 2, 4);
        resTabPackage.typeStrings = Utils.byte2int(typeStringsByte);
        System.out.println("typeString:" + resTabPackage.typeStrings);

        //����lastPublicType�ֶ�
        byte[] lastPublicType = Utils.copyByte(src, offset + 8 + 128 * 2, 4);
        resTabPackage.lastPublicType = Utils.byte2int(lastPublicType);

        //����keyString�ַ�����ƫ��ֵ
        byte[] keyStrings = Utils.copyByte(src, offset + 12 + 128 * 2, 4);
        resTabPackage.keyStrings = Utils.byte2int(keyStrings);
        System.out.println("keyString:" + resTabPackage.keyStrings);

        //����lastPublicKey
        byte[] lastPublicKey = Utils.copyByte(src, offset + 12 + 128 * 2, 4);
        resTabPackage.lastPublicKey = Utils.byte2int(lastPublicKey);

        //�����ȡ�����ַ�����ƫ��ֵ�������ַ�����ƫ��ֵ
        keyStringPoolChunkOffset = (packageChunkOffset + resTabPackage.keyStrings);
        typeStringPoolChunkOffset = (packageChunkOffset + resTabPackage.typeStrings);

    }

    /**
     * ���������ַ�������
     *
     * @param src
     */
    public static void parseTypeStringPoolChunk(byte[] src) {
        System.out.println("typestring offset:" + Utils.bytesToHexString(Utils.int2Byte(typeStringPoolChunkOffset)));
        ResStringPoolHeader stringPoolHeader = parseStringPoolChunk(src, typeStringList, typeStringPoolChunkOffset);
        System.out.println("size:" + stringPoolHeader.header.size);
    }

    /**
     * ����key�ַ�������
     *
     * @param src
     */
    public static void parseKeyStringPoolChunk(byte[] src) {
        System.out.println("keystring offset:" + Utils.bytesToHexString(Utils.int2Byte(keyStringPoolChunkOffset)));
        ResStringPoolHeader stringPoolHeader = parseStringPoolChunk(src, keyStringList, keyStringPoolChunkOffset);
        System.out.println("size:" + stringPoolHeader.header.size);
        //������key�ַ���֮����Ҫ��ֵ��resType��ƫ��ֵ,��������Ҫ��������
        resTypeOffset = (keyStringPoolChunkOffset + stringPoolHeader.header.size);
    }

    /**
     * ����ResTypeSepc������������
     *
     * @param src
     */
    public static void parseResTypeSpec(byte[] src) {
        System.out.println("res type spec offset:" + Utils.bytesToHexString(Utils.int2Byte(resTypeOffset)));
        ResTableTypeSpec typeSpec = new ResTableTypeSpec();
        //����ͷ����Ϣ
        typeSpec.header = parseResChunkHeader(src, resTypeOffset);

        int offset = (resTypeOffset + typeSpec.header.getHeaderSize());

        //����id����
        byte[] idByte = Utils.copyByte(src, offset, 1);
        typeSpec.id = (byte) (idByte[0] & 0xFF);
        resTypeId = typeSpec.id;

        //����res0�ֶ�,����ֶ��Ǳ��õģ�ʼ����0
        byte[] res0Byte = Utils.copyByte(src, offset + 1, 1);
        typeSpec.res0 = (byte) (res0Byte[0] & 0xFF);

        //����res1�ֶΣ�����ֶ��Ǳ��õģ�ʼ����0
        byte[] res1Byte = Utils.copyByte(src, offset + 2, 2);
        typeSpec.res1 = Utils.byte2Short(res1Byte);

        //entry���ܸ���
        byte[] entryCountByte = Utils.copyByte(src, offset + 4, 4);
        typeSpec.entryCount = Utils.byte2int(entryCountByte);

        System.out.println("res type spec:" + typeSpec);

        System.out.println("type_name:" + typeStringList.get(typeSpec.id - 1));

        //��ȡentryCount��int����
        int[] intAry = new int[typeSpec.entryCount];
        int intAryOffset = resTypeOffset + typeSpec.header.headerSize;
        System.out.print("int element:");
        for (int i = 0; i < typeSpec.entryCount; i++) {
            int element = Utils.byte2int(Utils.copyByte(src, intAryOffset + i * 4, 4));
            intAry[i] = element;
            System.out.print(element + ",");
        }
        System.out.println();

        resTypeOffset += typeSpec.header.size;

    }

    /**
     * ����������Ϣ����
     *
     * @param src
     */
    public static void parseResTypeInfo(byte[] src) {
        System.out.println("type chunk offset:" + Utils.bytesToHexString(Utils.int2Byte(resTypeOffset)));
        ResTableType type = new ResTableType();
        //����ͷ����Ϣ
        type.header = parseResChunkHeader(src, resTypeOffset);

        int offset = (resTypeOffset + type.header.getHeaderSize());

        //����type��idֵ
        byte[] idByte = Utils.copyByte(src, offset, 1);
        type.id = (byte) (idByte[0] & 0xFF);

        //����res0�ֶε�ֵ�������ֶΣ�ʼ����0
        byte[] res0 = Utils.copyByte(src, offset + 1, 1);
        type.res0 = (byte) (res0[0] & 0xFF);

        //����res1�ֶε�ֵ�������ֶΣ�ʼ����0
        byte[] res1 = Utils.copyByte(src, offset + 2, 2);
        type.res1 = Utils.byte2Short(res1);

        byte[] entryCountByte = Utils.copyByte(src, offset + 4, 4);
        type.entryCount = Utils.byte2int(entryCountByte);

        byte[] entriesStartByte = Utils.copyByte(src, offset + 8, 4);
        type.entriesStart = Utils.byte2int(entriesStartByte);

        ResTableConfig resConfig = new ResTableConfig();
        resConfig = parseResTableConfig(Utils.copyByte(src, offset + 12, resConfig.getSize()));
        System.out.println("config:" + resConfig);

        System.out.println("res type info:" + type);

        System.out.println("type_name:" + typeStringList.get(type.id - 1));

        //�Ȼ�ȡentryCount��int����
        System.out.print("type int elements:");
        int[] intAry = new int[type.entryCount];
        for (int i = 0; i < type.entryCount; i++) {
            int element = Utils.byte2int(Utils.copyByte(src, resTypeOffset + type.header.headerSize + i * 4, 4));
            intAry[i] = element;
            System.out.print(element + ",");
        }
        System.out.println();

        //���￪ʼ���������Ӧ��ResEntry��ResValue
        int entryAryOffset = resTypeOffset + type.entriesStart;
        ResTableEntry[] tableEntryAry = new ResTableEntry[type.entryCount];
        ResValue[] resValueAry = new ResValue[type.entryCount];
        System.out.println("entry offset:" + Utils.bytesToHexString(Utils.int2Byte(entryAryOffset)));

        //�������һ��������������ResMapEntry�Ļ���ƫ��ֵ�ǲ�һ���ģ�����������Ҫ���㲻ͬ��ƫ��ֵ
        int bodySize = 0, valueOffset = entryAryOffset;
        for (int i = 0; i < type.entryCount; i++) {
            int resId = getResId(i);
            System.out.println("resId:" + Utils.bytesToHexString(Utils.int2Byte(resId)));
            ResTableEntry entry = new ResTableEntry();
            ResValue value = new ResValue();
            valueOffset += bodySize;
            System.out.println("valueOffset:" + Utils.bytesToHexString(Utils.int2Byte(valueOffset)));
            entry = parseResEntry(Utils.copyByte(src, valueOffset, entry.getSize()));

            //������Ҫע����ǣ����ж�entry��flag�����Ƿ�Ϊ1,���Ϊ1�Ļ����Ǿ�ResTable_map_entry
            if (entry.flags == 1) {
                //�����Ǹ������͵�value
                ResTableMapEntry mapEntry = new ResTableMapEntry();
                mapEntry = parseResMapEntry(Utils.copyByte(src, valueOffset, mapEntry.getSize()));
                System.out.println("map entry:" + mapEntry);
                ResTableMap resMap = new ResTableMap();
                for (int j = 0; j < mapEntry.count; j++) {
                    int mapOffset = valueOffset + mapEntry.getSize() + resMap.getSize() * j;
                    resMap = parseResTableMap(Utils.copyByte(src, mapOffset, resMap.getSize()));
                    System.out.println("map:" + resMap);
                }
                bodySize = mapEntry.getSize() + resMap.getSize() * mapEntry.count;
            } else {
                System.out.println("entry:" + entry);
                //�����Ǽ򵥵����͵�value
                value = parseResValue(Utils.copyByte(src, valueOffset + entry.getSize(), value.getSize()));
                System.out.println("value:" + value);
                bodySize = entry.getSize() + value.getSize();
            }

            tableEntryAry[i] = entry;
            resValueAry[i] = value;

            System.out.println("======================================");
        }

        resTypeOffset += type.header.size;

    }

    /**
     * ����ResEntry����
     *
     * @param src
     * @return
     */
    public static ResTableEntry parseResEntry(byte[] src) {
        ResTableEntry entry = new ResTableEntry();

        byte[] sizeByte = Utils.copyByte(src, 0, 2);
        entry.size = Utils.byte2Short(sizeByte);

        byte[] flagByte = Utils.copyByte(src, 2, 2);
        entry.flags = Utils.byte2Short(flagByte);

        ResStringPoolRef key = new ResStringPoolRef();
        byte[] keyByte = Utils.copyByte(src, 4, 4);
        key.index = Utils.byte2int(keyByte);
        entry.key = key;

        return entry;
    }

    /**
     * ����ResMapEntry����
     *
     * @param src
     * @return
     */
    public static ResTableMapEntry parseResMapEntry(byte[] src) {
        ResTableMapEntry entry = new ResTableMapEntry();

        byte[] sizeByte = Utils.copyByte(src, 0, 2);
        entry.size = Utils.byte2Short(sizeByte);

        byte[] flagByte = Utils.copyByte(src, 2, 2);
        entry.flags = Utils.byte2Short(flagByte);

        ResStringPoolRef key = new ResStringPoolRef();
        byte[] keyByte = Utils.copyByte(src, 4, 4);
        key.index = Utils.byte2int(keyByte);
        entry.key = key;

        ResTableRef ref = new ResTableRef();
        byte[] identByte = Utils.copyByte(src, 8, 4);
        ref.ident = Utils.byte2int(identByte);
        entry.parent = ref;
        byte[] countByte = Utils.copyByte(src, 12, 4);
        entry.count = Utils.byte2int(countByte);

        return entry;
    }

    /**
     * ����ResValue����
     *
     * @param src
     * @return
     */
    public static ResValue parseResValue(byte[] src) {
        ResValue resValue = new ResValue();
        byte[] sizeByte = Utils.copyByte(src, 0, 2);
        resValue.size = Utils.byte2Short(sizeByte);

        byte[] res0Byte = Utils.copyByte(src, 2, 1);
        resValue.res0 = (byte) (res0Byte[0] & 0xFF);

        byte[] dataType = Utils.copyByte(src, 3, 1);
        resValue.dataType = (byte) (dataType[0] & 0xFF);

        byte[] data = Utils.copyByte(src, 4, 4);
        resValue.data = Utils.byte2int(data);

        return resValue;
    }

    /**
     * ����ResTableMap����
     *
     * @param src
     * @return
     */
    public static ResTableMap parseResTableMap(byte[] src) {
        ResTableMap tableMap = new ResTableMap();

        ResTableRef ref = new ResTableRef();
        byte[] identByte = Utils.copyByte(src, 0, ref.getSize());
        ref.ident = Utils.byte2int(identByte);
        tableMap.name = ref;

        ResValue value = new ResValue();
        value = parseResValue(Utils.copyByte(src, ref.getSize(), value.getSize()));
        tableMap.value = value;

        return tableMap;

    }

    /**
     * ����ResTableConfig������Ϣ
     *
     * @param src
     * @return
     */
    public static ResTableConfig parseResTableConfig(byte[] src) {
        ResTableConfig config = new ResTableConfig();

        byte[] sizeByte = Utils.copyByte(src, 0, 4);
        config.size = Utils.byte2int(sizeByte);

        //���½ṹ��Union
        byte[] mccByte = Utils.copyByte(src, 4, 2);
        config.mcc = Utils.byte2Short(mccByte);
        byte[] mncByte = Utils.copyByte(src, 6, 2);
        config.mnc = Utils.byte2Short(mncByte);
        byte[] imsiByte = Utils.copyByte(src, 4, 4);
        config.imsi = Utils.byte2int(imsiByte);

        //���½ṹ��Union
        byte[] languageByte = Utils.copyByte(src, 8, 2);
        config.language = languageByte;
        byte[] countryByte = Utils.copyByte(src, 10, 2);
        config.country = countryByte;
        byte[] localeByte = Utils.copyByte(src, 8, 4);
        config.locale = Utils.byte2int(localeByte);

        //���½ṹ��Union
        byte[] orientationByte = Utils.copyByte(src, 12, 1);
        config.orientation = orientationByte[0];
        byte[] touchscreenByte = Utils.copyByte(src, 13, 1);
        config.touchscreen = touchscreenByte[0];
        byte[] densityByte = Utils.copyByte(src, 14, 2);
        config.density = Utils.byte2Short(densityByte);
        byte[] screenTypeByte = Utils.copyByte(src, 12, 4);
        config.screenType = Utils.byte2int(screenTypeByte);

        //���½ṹ��Union
        byte[] keyboardByte = Utils.copyByte(src, 16, 1);
        config.keyboard = keyboardByte[0];
        byte[] navigationByte = Utils.copyByte(src, 17, 1);
        config.navigation = navigationByte[0];
        byte[] inputFlagsByte = Utils.copyByte(src, 18, 1);
        config.inputFlags = inputFlagsByte[0];
        byte[] inputPad0Byte = Utils.copyByte(src, 19, 1);
        config.inputPad0 = inputPad0Byte[0];
        byte[] inputByte = Utils.copyByte(src, 16, 4);
        config.input = Utils.byte2int(inputByte);

        //���½ṹ��Union
        byte[] screenWidthByte = Utils.copyByte(src, 20, 2);
        config.screenWidth = Utils.byte2Short(screenWidthByte);
        byte[] screenHeightByte = Utils.copyByte(src, 22, 2);
        config.screenHeight = Utils.byte2Short(screenHeightByte);
        byte[] screenSizeByte = Utils.copyByte(src, 20, 4);
        config.screenSize = Utils.byte2int(screenSizeByte);

        //���½ṹ��Union
        byte[] sdVersionByte = Utils.copyByte(src, 24, 2);
        config.sdVersion = Utils.byte2Short(sdVersionByte);
        byte[] minorVersionByte = Utils.copyByte(src, 26, 2);
        config.minorVersion = Utils.byte2Short(minorVersionByte);
        byte[] versionByte = Utils.copyByte(src, 24, 4);
        config.version = Utils.byte2int(versionByte);

        //���½ṹ��Union
        byte[] screenLayoutByte = Utils.copyByte(src, 28, 1);
        config.screenLayout = screenLayoutByte[0];
        byte[] uiModeByte = Utils.copyByte(src, 29, 1);
        config.uiMode = uiModeByte[0];
        byte[] smallestScreenWidthDpByte = Utils.copyByte(src, 30, 2);
        config.smallestScreenWidthDp = Utils.byte2Short(smallestScreenWidthDpByte);
        byte[] screenConfigByte = Utils.copyByte(src, 28, 4);
        config.screenConfig = Utils.byte2int(screenConfigByte);

        //���½ṹ��Union
        byte[] screenWidthDpByte = Utils.copyByte(src, 32, 2);
        config.screenWidthDp = Utils.byte2Short(screenWidthDpByte);
        byte[] screenHeightDpByte = Utils.copyByte(src, 34, 2);
        config.screenHeightDp = Utils.byte2Short(screenHeightDpByte);
        byte[] screenSizeDpByte = Utils.copyByte(src, 32, 4);
        config.screenSizeDp = Utils.byte2int(screenSizeDpByte);

        byte[] localeScriptByte = Utils.copyByte(src, 36, 4);
        config.localeScript = localeScriptByte;

        byte[] localeVariantByte = Utils.copyByte(src, 40, 8);
        config.localeVariant = localeVariantByte;
        return config;
    }

    /**
     * ͳһ�����ַ�������
     *
     * @param src
     * @param stringList
     * @param stringOffset
     * @return
     */
    public static ResStringPoolHeader parseStringPoolChunk(byte[] src, ArrayList<String> stringList, int stringOffset) {
        ResStringPoolHeader stringPoolHeader = new ResStringPoolHeader();

        //����ͷ����Ϣ
        stringPoolHeader.header = parseResChunkHeader(src, stringOffset);

        System.out.println("header size:" + stringPoolHeader.header.headerSize);
        System.out.println("size:" + stringPoolHeader.header.size);

        int offset = stringOffset + stringPoolHeader.header.getHeaderSize();

        //��ȡ�ַ����ĸ���
        byte[] stringCountByte = Utils.copyByte(src, offset, 4);
        stringPoolHeader.stringCount = Utils.byte2int(stringCountByte);

        //������ʽ�ĸ���
        byte[] styleCountByte = Utils.copyByte(src, offset + 4, 4);
        stringPoolHeader.styleCount = Utils.byte2int(styleCountByte);

        //�����ʾ�ַ����ĸ�ʽ:UTF-8/UTF-16
        byte[] flagByte = Utils.copyByte(src, offset + 8, 4);
        System.out.println("flag:" + Utils.bytesToHexString(flagByte));
        stringPoolHeader.flags = Utils.byte2int(flagByte);

        //�ַ������ݵĿ�ʼλ��
        byte[] stringStartByte = Utils.copyByte(src, offset + 12, 4);
        stringPoolHeader.stringsStart = Utils.byte2int(stringStartByte);
        System.out.println("string start:" + Utils.bytesToHexString(stringStartByte));

        //��ʽ���ݵĿ�ʼλ��
        byte[] sytleStartByte = Utils.copyByte(src, offset + 16, 4);
        stringPoolHeader.stylesStart = Utils.byte2int(sytleStartByte);
        System.out.println("style start:" + Utils.bytesToHexString(sytleStartByte));

        //��ȡ�ַ������ݵ������������ʽ���ݵ���������
        int[] stringIndexAry = new int[stringPoolHeader.stringCount];
        int[] styleIndexAry = new int[stringPoolHeader.styleCount];

        System.out.println("string count:" + stringPoolHeader.stringCount);
        System.out.println("style count:" + stringPoolHeader.styleCount);

        int stringIndex = offset + 20;
        for (int i = 0; i < stringPoolHeader.stringCount; i++) {
            stringIndexAry[i] = Utils.byte2int(Utils.copyByte(src, stringIndex + i * 4, 4));
        }

        int styleIndex = stringIndex + 4 * stringPoolHeader.stringCount;
        for (int i = 0; i < stringPoolHeader.styleCount; i++) {
            styleIndexAry[i] = Utils.byte2int(Utils.copyByte(src, styleIndex + i * 4, 4));
        }

        //ÿ���ַ�����ͷ�����ֽڵ����һ���ֽ����ַ����ĳ���
        //�����ȡ�����ַ���������
        int stringContentIndex = styleIndex + stringPoolHeader.styleCount * 4;
        System.out.println("string index:" + Utils.bytesToHexString(Utils.int2Byte(stringContentIndex)));
        int index = 0;
        while (index < stringPoolHeader.stringCount) {
            byte[] stringSizeByte = Utils.copyByte(src, stringContentIndex, 2);
            int stringSize = (stringSizeByte[1] & 0x7F);
            if (stringSize != 0) {
                String val = "";
                try {
                    val = new String(Utils.copyByte(src, stringContentIndex + 2, stringSize), "utf-8");
                } catch (Exception e) {
                    System.out.println("string encode error:" + e.toString());
                }
                stringList.add(val);
            } else {
                stringList.add("");
            }
            stringContentIndex += (stringSize + 3);
            index++;
        }
        for (String str : stringList) {
            System.out.println("str:" + str);
        }

        return stringPoolHeader;

    }

    /**
     * ������Դͷ����Ϣ
     * ���е�Chunk������ͷ����Ϣ
     *
     * @param src
     * @param start
     * @return
     */
    private static ResChunkHeader parseResChunkHeader(byte[] src, int start) {

        ResChunkHeader header = new ResChunkHeader();

        //����ͷ������
        byte[] typeByte = Utils.copyByte(src, start, 2);
        header.type = Utils.byte2Short(typeByte);

        //����ͷ����С
        byte[] headerSizeByte = Utils.copyByte(src, start + 2, 2);
        header.headerSize = Utils.byte2Short(headerSizeByte);

        //��������Chunk�Ĵ�С
        byte[] tableSizeByte = Utils.copyByte(src, start + 4, 4);
        header.size = Utils.byte2int(tableSizeByte);

        return header;
    }

    /**
     * �ж��Ƿ��ļ�ĩβ��
     *
     * @param length
     * @return
     */
    public static boolean isEnd(int length) {
        if (resTypeOffset >= length) {
            return true;
        }
        return false;
    }

    /**
     * �ж��ǲ�������������
     *
     * @param src
     * @return
     */
    public static boolean isTypeSpec(byte[] src) {
        ResChunkHeader header = parseResChunkHeader(src, resTypeOffset);
        if (header.type == 0x0202) {
            return true;
        }
        return false;
    }

    public static String getResString(int index) {
        if (index >= resStringList.size() || index < 0) {
            return "";
        }
        return resStringList.get(index);
    }

    public static String getKeyString(int index) {
        if (index >= keyStringList.size() || index < 0) {
            return "";
        }
        return keyStringList.get(index);
    }

    /**
     * ��ȡ��Դid
     * �����λ��packid����λ��restypeid����λ��entryid
     *
     * @param entryid
     * @return
     */
    public static int getResId(int entryid) {
        return (((packId) << 24) | (((resTypeId) & 0xFF) << 16) | (entryid & 0xFFFF));
    }

}

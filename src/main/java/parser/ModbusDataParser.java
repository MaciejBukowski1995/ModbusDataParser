package parser;


import models.RowModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ModbusDataParser {

    private int [] registers = new int [100];
    private List<Instruction> instructions = new ArrayList<>();
    public enum FileLocalization {LOCAL, ONLINE};

    public void loadInstructions(String fileName) {

        try{

            Scanner sc = new Scanner(new File(fileName));
            while(sc.hasNext()) {
                Instruction tmp = new Instruction();
                tmp.rangeOfRegisters[0] = sc.nextInt();
                tmp.rangeOfRegisters[1] = sc.nextInt();
                tmp.format = sc.next();
                tmp.unit = sc.next();
                tmp.nameOfVariable = sc.nextLine();

                instructions.add(tmp);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public List<RowModel> processData() {
        Converter converter = new Converter();
        List<RowModel> results = new ArrayList<>();
        for(int i = 0; i < instructions.size(); i++) {
            switch (instructions.get(i).format) {

                case "REAL4":
                    RowModel rowReal4 = new RowModel();
                    rowReal4.setNameOfVariable(instructions.get(i).nameOfVariable);
                    rowReal4.setValue(Float.toString(converter.convertToFloat(registers[instructions.get(i).rangeOfRegisters[0] - 1], registers[instructions.get(i).rangeOfRegisters[1] - 1])));
                    if(instructions.get(i).unit.equals("null")) rowReal4.setUnit("");
                    else rowReal4.setUnit(instructions.get(i).unit);
                    results.add(rowReal4);
                    break;

                case "LONG":
                    RowModel rowLong = new RowModel();
                    rowLong.setNameOfVariable(instructions.get(i).nameOfVariable);
                    rowLong.setValue(Long.toString(converter.convertToLong(registers[instructions.get(i).rangeOfRegisters[0] - 1], registers[instructions.get(i).rangeOfRegisters[1] - 1])));
                    if(instructions.get(i).unit.equals("null")) rowLong.setUnit("");
                    else rowLong.setUnit(instructions.get(i).unit);
                    results.add(rowLong);
                    break;

                case "BCD":
                    RowModel rowBcd = new RowModel();
                    byte[] bcd = new byte[(instructions.get(i).rangeOfRegisters[1] - instructions.get(i).rangeOfRegisters[0] + 1) * 2];
                    int registerIndex = instructions.get(i).rangeOfRegisters[0] - 1;
                    for (int j = 0; j < bcd.length ; j += 2) {
                        bcd[0 + j] = (byte) (registers[registerIndex] & 0xFF);
                        bcd[1 + j] = (byte) ((registers[registerIndex] >> 8) & 0xFF);
                        registerIndex += 1;
                    }
                    rowBcd.setNameOfVariable(instructions.get(i).nameOfVariable);
                    rowBcd.setValue(converter.BCDtoString(bcd));
                    if(instructions.get(i).unit.equals("null")) rowBcd.setUnit("");
                    else rowBcd.setUnit(instructions.get(i).unit);
                    results.add(rowBcd);
                    break;

                case "INTEGER":
                    RowModel rowInteger = new RowModel();
                    rowInteger.setNameOfVariable(instructions.get(i).nameOfVariable);
                    rowInteger.setValue(Integer.toString(registers[instructions.get(i).rangeOfRegisters[0] - 1]));
                    if(instructions.get(i).unit.equals("null")) rowInteger.setUnit("");
                    else rowInteger.setUnit(instructions.get(i).unit);
                    results.add(rowInteger);
                    break;

                case "INTEGER_QUALITY":
                    RowModel rowInteger1 = new RowModel();
                    RowModel rowInteger2 = new RowModel();
                    byte[] data = new byte[2];

                    data[0] = (byte) (registers[instructions.get(i).rangeOfRegisters[0] - 1] & 0xFF);
                    rowInteger1.setValue(Byte.toString(data[0]));
                    data[1] = (byte) ((registers[instructions.get(i).rangeOfRegisters[0] - 1] >> 8) & 0xFF);
                    rowInteger2.setValue(Byte.toString(data[1]));
                    String[] names = instructions.get(i).nameOfVariable.split(":");
                    rowInteger1.setNameOfVariable(names[1]);
                    rowInteger2.setNameOfVariable(names[0]);
                    rowInteger1.setUnit("");
                    rowInteger2.setUnit("");
                    results.add(rowInteger1);
                    results.add(rowInteger2);
                    break;

                case "INTEGER_LANGUAGE":
                    RowModel rowIntegerLanguage = new RowModel();
                    rowIntegerLanguage.setNameOfVariable(instructions.get(i).nameOfVariable);
                    if(registers[instructions.get(i).rangeOfRegisters[0] - 1] == 0) rowIntegerLanguage.setValue("English");
                    else if(registers[instructions.get(i).rangeOfRegisters[0] - 1] == 1) rowIntegerLanguage.setValue("Chinese");
                    else rowIntegerLanguage.setValue("Not mentioned in doc");
                    rowIntegerLanguage.setUnit("");
                    results.add(rowIntegerLanguage);
                    break;

                case "BIT_ERROR":
                    RowModel rowBitError = new RowModel();
                    rowBitError.setNameOfVariable(instructions.get(i).nameOfVariable);
                    rowBitError.setUnit("");
                    String value= "";
                    int input = registers[instructions.get(i).rangeOfRegisters[0] - 1];

                    boolean[] bits = new boolean[16];
                    for (int b = 15; b >= 0; b--) {
                        bits[b] = (input & (1 << b)) != 0;
                    }
                    if(bits[0]) value += "no received signal\n";
                    if(bits[1]) value += "low received signal\n";
                    if(bits[2]) value += "poor received signal\n";
                    if(bits[3]) value += "pipe empty\n";
                    if(bits[4]) value += "hardware failure\n";
                    if(bits[5]) value += "receiving circuits gain in adjusting\n";
                    if(bits[6]) value += "frequency at the frequency output over flow\n";
                    if(bits[7]) value += "current at 4-20mA over flow\n";
                    if(bits[8]) value += "RAM check-sum error\n";
                    if(bits[9]) value += "main clock or timer clock error\n";
                    if(bits[10]) value += "parameters check-sum error\n";
                    if(bits[11]) value += "ROM check-sum error\n";
                    if(bits[12]) value += "temperature circuits error\n";
                    if(bits[13]) value += "reserved\n";
                    if(bits[14]) value += "internal timer over flow\n";
                    if(bits[15]) value += "analog input over range\n";

                    rowBitError.setValue(value);
                    results.add(rowBitError);
                    break;
            }
        }
        return results;
    }

    public void loadFromFile(String fileName, FileLocalization fileLocalization) {
        int i = 0;
        String fileData;
        String line;
        Scanner sc = null;

        try{

            switch (fileLocalization)
            {
                case LOCAL:
                    sc = new Scanner(new File(fileName));
                    break;

                case ONLINE:
                    URL url = new URL(fileName);//http://tuftuf.gambitlabs.fi/feed.txt
                    sc = new Scanner(url.openStream());
                    break;
            }

            fileData = sc.nextLine();
            while(sc.hasNext()) {
                line = sc.nextLine();
                String[] parts = line.split(":");
                registers[i] = Integer.parseInt(parts[1]);
                i += 1;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private class Instruction {
        int [] rangeOfRegisters = new int[2];
        String nameOfVariable;
        String unit;
        String format;
    }

    private class Converter {

        public int convertToLong(int firstRegister, int secondRegister) {
            return ((secondRegister & 0xFFFF) << 16) | (firstRegister & 0xFFFF);
        }

        public float convertToFloat(int firstRegister,int secondRegister) {
            return Float.intBitsToFloat((secondRegister << 16) | firstRegister);
        }

        public String BCDtoString(byte bcd) {
            StringBuffer sb = new StringBuffer();

            byte high = (byte) (bcd & 0xf0);
            high >>>= (byte) 4;
            high = (byte) (high & 0x0f);
            byte low = (byte) (bcd & 0x0f);

            sb.append(high);
            sb.append(low);

            return sb.toString();
        }

        public String BCDtoString(byte[] bcd) {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < bcd.length; i++) {
                sb.append(BCDtoString(bcd[i]));
            }

            return sb.toString();
        }
    }
}


package org.vafada;

import java.util.Random;

public class CPU {
    private final int CHIP_8_WIDTH = 64;
    private final int CHIP_8_HEIGHT = 32;

    private short opcode;
    private byte[] memory = new byte[4096];
    // registers
    private int[] V = new int[16];
    // register "I"
    private short I;
    // Program Counter
    private int pc;
    private int[][] gfx = new int[CHIP_8_WIDTH][CHIP_8_HEIGHT];
    private byte delayTimer;
    private byte soundTimer;
    private int[] stack = new int[16];
    // stack pointer
    private short sp;
    private byte[] key = new byte[16];
    Random rand = new Random();

    public CPU() {
        pc = 0x200;  // Program counter starts at 0x200
        opcode = 0;  // Reset current opcode
        I = 0;       // Reset index register
        sp = 0;      // Reset stack pointer
        clearGFX();

        // TODO: Load fontset
        /*
        for(int i = 0; i < 80; ++i) {
            memory[i] = chip8_fontset[i];
        }
        */
        //for(int i = 0; i < CHIP_8_WIDTH; i++) {
            //gfx[i] = (byte)(i % 2);
            //gfx[i] = 1;
            //debugLog("i = " + i);
        //}
        /*gfx[0] = 1;
        gfx[2] = 1;
        gfx[65] = 1;
        gfx[66] = 1;
        gfx[67] = 1;
        gfx[128] = 1;*/

    }

    int count = 0;

    public void emulateCycle() {
        count++;
        //debugLog("count = " + count);
        // Fetch opcode
        opcode = (short) (memory[pc] << 8 | (memory[pc + 1] & 0xFF));

        /*
        String debugString = String.format("%16s", Integer.toBinaryString(opcode)).replace(' ', '0');
        debugLog("debugString = " + debugString);
        debugLog(Integer.toHexString(opcode));

        debugLog("first part = " + (opcode & 0xF000));
         */


        /*
        String hex = Integer.toHexString(memory[pc] & 0xffff);
        debugLog(hex);

        hex = Integer.toHexString(memory[pc + 1] & 0xffff);
        debugLog(hex);

         */
/*
        String hex = Integer.toBinaryString(memory[pc] << 8);
        //String hex = Integer.toBinaryString(opcode);
        debugLog("bin = " + hex);

        hex = Integer.toBinaryString((memory[pc + 1] ));
        //String hex = Integer.toBinaryString(opcode);
        debugLog("bin2 = " + hex);

        //hex = Integer.toHexString(opcode & 0xffff);
        hex = Integer.toHexString(opcode);
        debugLog(hex);

        hex = Integer.toBinaryString(opcode);
        debugLog(hex);
*/
        String hexOpCode = shortToHex(opcode);
        // debugLog(hexOpCode);

        // Decode opcode
        switch (opcode & 0xF000) {
            case 0x0000: {
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x00E0: Clears the screen
                        debugLog("CLS");
                        clearGFX();
                        nextInstruction();
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        debugLog("TODO: RET");
                        break;

                    default:
                        debugLog("Unknown opcode: " + hexOpCode);
                }
            }
            break;
            case 0x1000: {
                short nnn = (short) (opcode & 0x0FFF);
                pc = nnn;
                debugLog("1nnn - JP addr: " + shortToHex(opcode) + " nnn = " + nnn);
            }
            break;
            case 0x2000: {
                stack[sp] = pc;
                sp++;
                pc = (short) (opcode & 0x0FFF);
                debugLog("2nnn - CALL addr: setting pc = 0x" + shortToHex(pc));
            }
            break;
            case 0x3000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                byte kk = (byte) (opcode & 0x00FF);
                debugLog("3xkk - SE Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk + " V[x] = " + V[x]);
                if (V[x] == kk) {
                    pc = (short) (pc + 2);
                    skipInstruction();
                } else {
                    nextInstruction();
                }
            }
            break;
            case 0x6000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                byte kk = (byte) (opcode & 0x00FF);
                debugLog("6xkk - LD Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk);
                V[x] = kk;
                nextInstruction();
            }
            break;
            case 0x7000: {
                byte x = (byte) ((opcode & 0x0F00) >> 8);
                byte kk = (byte) (opcode & 0x00FF);
                debugLog("7xkk - ADD Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk);
                V[x] = V[x] + kk;
                nextInstruction();
            }
            break;
            case 0x8000: {
                switch (opcode & 0x000F) {
                    case 0x0000: {
                        byte x = (byte) ((opcode & 0x0F00) >> 8);
                        byte y = (byte) ((opcode & 0x00F0) >> 4);
                        debugLog("8xy0 - LD Vx, Vy: " + shortToHex(opcode) + " x = " + x + " y = " + y + " V[x] = " + V[x] + " V[y] = " + V[y]);
                        V[x] = V[y];
                        nextInstruction();
                    }
                    break;
                    case 0x0001: {
                        byte x = (byte) ((opcode & 0x0F00) >> 8);
                        byte y = (byte) ((opcode & 0x00F0) >> 4);
                        debugLog("8xy1 - OR Vx, Vy: " + shortToHex(opcode) + " x = " + x + " y = " + y + " V[x] = " + V[x] + " V[y] = " + V[y]);
                        V[x] = V[x] | V[y];
                        nextInstruction();
                    }
                    break;
                    case 0x0004: {
                        byte x = (byte) ((opcode & 0x0F00) >> 8);
                        byte y = (byte) ((opcode & 0x00F0) >> 4);
                        int xVal = V[x];
                        int yVal = V[y];

                        int sum = xVal + yVal;

                        debugLog("TODO: 8xy4 - ADD Vx, Vy: " + shortToHex(opcode) + " x = " + x + " y = " + y + " V[x] = " + V[x] + " V[y] = " + V[y] + " sum = " + sum);
                        if (sum > 255) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] = (sum & 0x00FF);
                        nextInstruction();
                    }
                    break;

                    default:
                        debugLog("Unknown opcode: " + hexOpCode);
                }
            }
            break;
            case 0xA000: {
                I = (short) (opcode & 0x0FFF);
                debugLog("Annn - LD I, addr: setting value of register I = 0x" + shortToHex(I));
                nextInstruction();
            }
            break;
            case 0xC000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                int randInt = rand.nextInt(256);
                short kk = (short) (opcode & 0x00FF);

                V[x] = randInt & kk;
                debugLog("Cxkk - RND Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk);
                nextInstruction();
            }
            break;
            case 0xD000: {
                int x = V[((opcode & 0x0F00) >> 8)];
                int y = V[((opcode & 0x00F0) >> 4)];
                byte height = (byte) (opcode & 0x000F);
                debugLog("Dxyn - DRW Vx, Vy, nibble: " + shortToHex(opcode) + " x = " + x + " y = " + y + " nibble = " + height);
                V[0xF] = 0;

                for (int yline = 0; yline < height; yline++) {
                    byte pixel = memory[I + yline];
                    for (int xline = 0; xline < 8; xline++) {
                        if ((pixel & (0x80 >> xline)) != 0) {
                            int xCoord = x + xline;
                            int yCoord = y + yline;

                            // System.out.println("x = " + xCoord + " y = " + yCoord);

                            //int position = yCoord * 32 + xCoord;
                            // if pixel already exists, set carry (collision)
                            if (gfx[xCoord][yCoord] == 1) {
                                V[0xF] = 1;
                            }
                            // draw via xor
                            gfx[xCoord][yCoord] ^= 1;

                        }
                    }
                }
                nextInstruction();
            }
            break;
            default:
                debugLog("Unknown opcode: " + hexOpCode);
        }

        // Update timers
        if (delayTimer > 0)
            --delayTimer;

        if (soundTimer > 0) {
            if (soundTimer == 1) {
                debugLog("BEEP!");
            }
            --soundTimer;
        }
    }

    public void loadProgram(byte[] program) {
        for (int i = 0; i < program.length; i++) {
            memory[i + 512] = program[i];
        }
    }

    private static String shortToHex(int val) {
        return String.format("%1$04X", val);
    }

    public int getPixel(int x, int y) {
        return gfx[x][y];
    }
    private void debugLog(String s) {
        System.out.println(s);
    }
    public void logGFX() {
        for (int y = 0; y < CHIP_8_HEIGHT; y++) {
            for (int x = 0; x < CHIP_8_WIDTH; x++) {
                System.out.print(gfx[x][y]);
            }
            System.out.println("\n");
        }
    }

    public void clearGFX() {
        for(int y = 0; y < CHIP_8_HEIGHT; y++) {
            for(int x = 0; x < CHIP_8_WIDTH; x++) {
                gfx[x][y] = 0;
            }
        }
    }

    private void nextInstruction() {
        pc = pc + 2;
    }

    private void skipInstruction() {
        pc = pc + 4;
    }
}

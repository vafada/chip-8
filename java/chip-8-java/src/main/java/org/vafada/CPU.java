package org.vafada;

import java.util.Random;

public class CPU {
    private short opcode;
    private byte[] memory = new byte[4096];
    // registers
    private int[] V = new int[16];
    // register "I"
    private short I;
    // Program Counter
    private short pc;
    private byte[] gfx = new byte[64 * 32];
    private byte delayTimer;
    private byte soundTimer;
    private short[] stack = new short[16];
    // stack pointer
    private short sp;
    private byte[] key = new byte[16];
    Random rand = new Random();

    public CPU() {
        pc = 0x200;  // Program counter starts at 0x200
        opcode = 0;  // Reset current opcode
        I = 0;       // Reset index register
        sp = 0;      // Reset stack pointer

        // TODO: Load fontset
        /*
        for(int i = 0; i < 80; ++i) {
            memory[i] = chip8_fontset[i];
        }
        */
    }

    int count = 0;

    public void emulateCycle() {
        count++;
        //System.out.println("count = " + count);
        // Fetch opcode
        opcode = (short) (memory[pc] << 8 | (memory[pc + 1] & 0xFF));

        /*
        String debugString = String.format("%16s", Integer.toBinaryString(opcode)).replace(' ', '0');
        System.out.println("debugString = " + debugString);
        System.out.println(Integer.toHexString(opcode));

        System.out.println("first part = " + (opcode & 0xF000));
         */


        /*
        String hex = Integer.toHexString(memory[pc] & 0xffff);
        System.out.println(hex);

        hex = Integer.toHexString(memory[pc + 1] & 0xffff);
        System.out.println(hex);

         */
/*
        String hex = Integer.toBinaryString(memory[pc] << 8);
        //String hex = Integer.toBinaryString(opcode);
        System.out.println("bin = " + hex);

        hex = Integer.toBinaryString((memory[pc + 1] ));
        //String hex = Integer.toBinaryString(opcode);
        System.out.println("bin2 = " + hex);

        //hex = Integer.toHexString(opcode & 0xffff);
        hex = Integer.toHexString(opcode);
        System.out.println(hex);

        hex = Integer.toBinaryString(opcode);
        System.out.println(hex);
*/
        String hexOpCode = shortToHex(opcode);
        // System.out.println(hexOpCode);

        // Decode opcode
        switch (opcode & 0xF000) {
            case 0x0000: {
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x00E0: Clears the screen
                        // System.out.println("CLS");
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        System.out.println("RET");
                        break;

                    default:
                        System.out.println("Unknown opcode: " + hexOpCode);
                }
            }
            break;
            case 0x1000: {
                short nnn = (short) (opcode & 0x0FFF);
                pc = nnn;
                System.out.println("1nnn - JP addr: " + shortToHex(opcode) + " nnn = " + nnn);
            }
            case 0x2000: {
                stack[sp] = pc;
                sp++;
                pc = (short) (opcode & 0x0FFF);
                System.out.println("2nnn - CALL addr: setting pc = 0x" + shortToHex(pc));
            }
            break;
            case 0x3000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                byte kk = (byte) (opcode & 0x00FF);
                System.out.println("3xkk - SE Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk + " V[x] = " + V[x]);
                if (V[x] == kk) {
                    pc = (short) (pc + 2);
                }
            }
            break;
            case 0x6000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                byte kk = (byte) (opcode & 0x00FF);
                System.out.println("6xkk - LD Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk);
                V[x] = kk;
            }
            break;
            case 0x7000: {
                byte x = (byte) ((opcode & 0x0F00) >> 8);
                byte kk = (byte) (opcode & 0x00FF);
                System.out.println("7xkk - ADD Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk);
                V[x] = V[x] + kk;
                // System.out.println("V[x] = " + V[x]);
            }
            break;
            case 0x8000: {
                switch (opcode & 0x000F) {
                    case 0x0000: {
                        byte x = (byte) ((opcode & 0x0F00) >> 8);
                        byte y = (byte) ((opcode & 0x00F0) >> 4);
                        System.out.println("8xy0 - LD Vx, Vy: " + shortToHex(opcode) + " x = " + x + " y = " + y + " V[x] = " + V[x] + " V[y] = " + V[y]);
                        V[x] = V[y];
                    }
                    break;
                    case 0x0004: {
                        byte x = (byte) ((opcode & 0x0F00) >> 8);
                        byte y = (byte) ((opcode & 0x00F0) >> 4);
                        int xVal = V[x];
                        int yVal = V[y];

                        int sum = xVal + yVal;

                        System.out.println("TODO: 8xy4 - ADD Vx, Vy: " + shortToHex(opcode) + " x = " + x + " y = " + y + " V[x] = " + V[x] + " V[y] = " + V[y] + " sum = " + sum);
                        if (sum > 255) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] = (sum & 0x00FF);
                    }
                    break;

                    default:
                        System.out.println("Unknown opcode: " + hexOpCode);
                }
            }
            break;
            case 0xA000: {
                I = (short) (opcode & 0x0FFF);
                System.out.println("Annn - LD I, addr: setting value of register I = 0x" + shortToHex(I));
            }
            break;
            case 0xC000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                int randInt = rand.nextInt(256);
                short kk = (short) (opcode & 0x00FF);

                V[x] = randInt & kk;
                System.out.println("Cxkk - RND Vx, byte: " + shortToHex(opcode) + " x = " + x + " kk = " + kk);
            }
            break;
            case 0xD000: {
                short x = (short) ((opcode & 0x0F00) >> 8);
                byte y = (byte) ((opcode & 0x00F0) >> 4);
                byte nibble = (byte) (opcode & 0x000F);
                System.out.println("Dxyn - DRW Vx, Vy, nibble: " + shortToHex(opcode) + " x = " + x + " y = " + y + " nibble = " + nibble);
            }
            break;


            default:
                System.out.println("Unknown opcode: " + hexOpCode);
        }

        // Update timers
        if (delayTimer > 0)
            --delayTimer;

        if (soundTimer > 0) {
            if (soundTimer == 1) {
                System.out.println("BEEP!");
            }
            --soundTimer;
        }

        pc = (short) (pc + 2);
    }

    public void loadProgram(byte[] program) {
        for (int i = 0; i < program.length; i++) {
            memory[i + 512] = program[i];
        }

        /*System.out.println("memory");
        System.out.println(Integer.toHexString(memory[pc + 1]));*/

    }

    private static String shortToHex(short val) {
        return String.format("%1$04X", val);
    }
}

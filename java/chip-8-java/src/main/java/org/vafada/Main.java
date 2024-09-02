package org.vafada;

import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.rect.SDL_Rect;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.render.SDL_Texture;
import io.github.libsdl4j.api.surface.SDL_Surface;
import io.github.libsdl4j.api.video.SDL_Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_KEYDOWN;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_SPACE;
import static io.github.libsdl4j.api.pixels.SDL_PixelFormatEnum.SDL_PIXELFORMAT_RGBA8888;
import static io.github.libsdl4j.api.render.SDL_RendererFlags.SDL_RENDERER_ACCELERATED;
import static io.github.libsdl4j.api.render.SDL_TextureAccess.SDL_TEXTUREACCESS_STREAMING;
import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateRenderer;
import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateTexture;
import static io.github.libsdl4j.api.render.SdlRender.SDL_LockTextureToSurface;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderCopy;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderPresent;
import static io.github.libsdl4j.api.render.SdlRender.SDL_UnlockTexture;
import static io.github.libsdl4j.api.surface.SdlSurface.SDL_FillRect;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int WINDOW_WIDTH = 1024;
        int WINDOW_HEIGHT = 512;

        int CHIP_8_WIDTH = 64;
        int CHIP_8_HEIGHT = 32;

        int PIXEL_WIDTH = WINDOW_WIDTH / CHIP_8_WIDTH;
        int PIXEL_HEIGHT = WINDOW_HEIGHT / CHIP_8_HEIGHT;

        int WHITE = 0xFFFFFFFF;
        int BLACK = 0x000000FF;
        if (args.length == 0) {
            System.out.println("Enter ROM file ");
            System.exit(1);
        }

        CPU cpu = new CPU();

        String romFileString = args[0];
        Path path = Paths.get(romFileString);
        try {
            byte[] bytes = Files.readAllBytes(path);
            cpu.loadProgram(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize SDL
        int result = SDL_Init(SDL_INIT_EVERYTHING);
        if (result != 0) {
            throw new IllegalStateException("Unable to initialize SDL library (Error code " + result + "): " + SDL_GetError());
        }

        // Create and init the window
        SDL_Window window = SDL_CreateWindow("Demo SDL2", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, WINDOW_WIDTH, WINDOW_HEIGHT, SDL_WINDOW_SHOWN | SDL_WINDOW_RESIZABLE);
        if (window == null) {
            throw new IllegalStateException("Unable to create SDL window: " + SDL_GetError());
        }

        // Create and init the renderer
        SDL_Renderer renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
        if (renderer == null) {
            throw new IllegalStateException("Unable to create SDL renderer: " + SDL_GetError());
        }


        SDL_Texture texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_RGBA8888, SDL_TEXTUREACCESS_STREAMING, 64, 32);
        if (texture == null) {
            throw new IllegalStateException("Unable to create SDL texture: " + SDL_GetError());
        }

        int count = 0;
        // Start an event loop and react to events
        SDL_Event evt = new SDL_Event();
        boolean shouldRun = true;
        while (shouldRun) {
            cpu.emulateCycle();

            while (SDL_PollEvent(evt) != 0) {
                switch (evt.type) {
                    case SDL_QUIT:
                        shouldRun = false;
                        break;
                    case SDL_KEYDOWN:
                        if (evt.key.keysym.sym == SDLK_SPACE) {
                            //System.out.println("SPACE pressed");
                        }
                        break;
                    default:
                        break;
                }
            }

            SDL_Surface.Ref surfaceRef = new SDL_Surface.Ref();
            if (SDL_LockTextureToSurface(texture, null, surfaceRef) != 0) {
                throw new AssertionError("SDL Failure: " + SDL_GetError());
            }
            SDL_Surface surface = surfaceRef.getSurface();
            SDL_Rect rect = new SDL_Rect();
            rect.x = 0;
            rect.y = 0;
            rect.w = WINDOW_WIDTH;
            rect.h = WINDOW_HEIGHT;

            if (SDL_FillRect(surface, rect, BLACK) != 0) {
                throw new AssertionError("SDL Failure: " + SDL_GetError());
            }

            int y = 0;
            while (y < CHIP_8_HEIGHT) {
                int x = 0;
                while (x < CHIP_8_WIDTH) {
                    int pixel = cpu.getPixel(x, y);
                    int val = BLACK;
                    if (pixel == 1) {
                        val = WHITE;
                    }

                    /*if (val == WHITE) {
                        System.out.println("RECT: x = " + x + " y = " + y + " index = " + (y * CHIP_8_WIDTH + x));
                    }*/

                    SDL_Rect innerRect = new SDL_Rect();
                    innerRect.x = x;
                    innerRect.y = y;
                    innerRect.w = PIXEL_WIDTH;
                    innerRect.h = PIXEL_HEIGHT;
                    if (SDL_FillRect(surface, innerRect, val) != 0) {
                        throw new AssertionError("SDL Failure: " + SDL_GetError());
                    }

                    x++;
                }
                y++;
            }



            SDL_UnlockTexture(texture);
            SDL_RenderCopy(renderer, texture, null, rect);
            SDL_RenderPresent(renderer);

            Thread.sleep(16);
            //Thread.sleep(500);
            //System.exit(0);
            count++;
            /*System.out.println("count = " + count);
            if (count == 6) {
                cpu.logGFX();
                break;
            }*/
        }

        SDL_Quit();

    }
}
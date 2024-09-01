package org.vafada;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
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
import static io.github.libsdl4j.api.render.SdlRender.SDL_LockTexture;
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

        /*for (;;) {
            cpu.emulateCycle();
        }*/


        // Initialize SDL
        int result = SDL_Init(SDL_INIT_EVERYTHING);
        if (result != 0) {
            throw new IllegalStateException("Unable to initialize SDL library (Error code " + result + "): " + SDL_GetError());
        }

        // Create and init the window
        SDL_Window window = SDL_CreateWindow("Demo SDL2", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, 1024, 512, SDL_WINDOW_SHOWN | SDL_WINDOW_RESIZABLE);
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

        /*SDL_SetRenderDrawColor(renderer, (byte) 0, (byte) 0, (byte) 0, (byte) 0);

        // Clear the window and make it all red
        SDL_RenderClear(renderer);

        // Render the changes above ( which up until now had just happened behind the scenes )
        SDL_RenderPresent(renderer);*/

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

            int y = 0;
            while (y < 32) {
                int x = 0;
                while (x < 64) {
                    byte pixel = cpu.getPixel(y * 64 + x);
                    int val = BLACK;
                    if (pixel == 1) {
                        val = WHITE;
                    }


                    SDL_Surface.Ref surfaceRef = new SDL_Surface.Ref();
                    if (SDL_LockTextureToSurface(texture, null, surfaceRef) != 0) {
                        throw new AssertionError("SDL Failure: " + SDL_GetError());
                    }
                    SDL_Surface surface = surfaceRef.getSurface();
                    SDL_Rect rect = new SDL_Rect();
                    rect.x = x;
                    rect.y = y;
                    rect.w = 100;
                    rect.h = 100;
                    //rect.w = 1024 / 64;
                    //rect.h = 512 / 32;
                    if (SDL_FillRect(surface, rect, val) != 0) {
                        throw new AssertionError("SDL Failure: " + SDL_GetError());
                    }
                    SDL_UnlockTexture(texture);
                    SDL_RenderCopy(renderer, texture, null, rect);
                    SDL_RenderPresent(renderer);
                    x = x + 1;
                }
                y = y + 1;
            }



            Thread.sleep(16);
        }

        SDL_Quit();

    }
}
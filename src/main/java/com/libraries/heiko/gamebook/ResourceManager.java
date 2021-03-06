package com.libraries.heiko.gamebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

import com.libraries.heiko.gamebook.tools.*;

/**
 * Created by heiko on 28.02.2016.
 */
public class ResourceManager
{
    private GameStack<GameResource> images;     // holds resources of the type 'image'
    private GameStack<GameResource> fonts;      // holds resources of the type 'font'
    private GameStack<GameResource> tilesets;   // holds resources of the type 'font'
    private GameFont tempFont;                  // hold a font to load it
    private Tileset tempTileset;               // hold a font to load it
    private GameStack<GameResource> tempStack;  // used to iterate trough resurce-stacks
    private GameResource tempResource;          // used to temporarily hold a new resource when adding it
    private GameBook book;                      // reference to the GameBook

    public ResourceManager(GameBook a_book)
    {
        this.book = a_book;
        this.images = new GameStack<GameResource>();
        this.fonts = new GameStack<GameResource>();
        this.tilesets = new GameStack<GameResource>();
        this.tempStack = new GameStack<GameResource>();
    }

    /*
        Function: GetImage
            Gets a previously stored image

        Parameter:
            a_id    - String    | ID of the stored image

        Returns:
            Bitmap -> - The requestes image
    */
    public Bitmap GetImage(String a_id)
    {
        return (Bitmap) this._GetResource(a_id, this.images);
    }

    /*
        Function: AddImage
            Stores an image using an id to later retrieve it

        Parameter:
            a_id    - String    | ID of the stored image
            a_image - Bitmap    | Image to store

        Returns:
            Bitmap -> - The image that just got stored
    */
    public Bitmap AddImage(String a_id, Bitmap a_image)
    {
        return (Bitmap) this._AddResource(a_id, this.images, a_image);
    }

    /*
        Function: AddImage
            Stores an image using an id to later retrieve it

        Parameter:
            a_id    - String    | ID of the stored image
            a_image - Resource  | Resource of the image to store

        Returns:
            Bitmap -> - The image that just got stored
    */
    public Bitmap AddImage(String a_id, int a_image)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = this.book.bitmapConfig;
        options.inScaled = false;
        return (Bitmap) this._AddResource(a_id, this.images, BitmapFactory.decodeResource(this.book.getContext().getResources(), a_image, options));
    }

    /*
        Function: AddImage
            Stores an image using an id to later retrieve it

        Parameter:
            a_id    - String    | ID of the stored image
            a_path  - String    | Path to the image to store

        Returns:
            Bitmap -> - The image that just got stored
    */
    public Bitmap AddImage(String a_id, String a_path)
    {
        return (Bitmap) this._AddResource(a_id, this.images, BitmapFactory.decodeFile(a_path));
    }

    /*
        Function: RemoveImage
            Removes a previously stored image from the ResourceManager

        Parameter:
            a_id    - String    | ID of the image to remove
    */
    public void RemoveImage(String a_id)
    {
        this._RemoveResource(a_id, this.images);
    }

    /*
        Function: AddFont
            Stores an font using an id to later retrieve it

        Parameter:
            a_id        - String    | ID of the stored font
            a_font      - String    | path of the font-file inside the assets-folder
            a_fontSize  - Integer   | FontSize to use
            a_padX      - Integer   | Letter-distance on the x-axis
            a_padY      - Integer   | Letter-distance in the y-axis

        Returns:
            GameFont -> - The Loaded font
    */
    public GameFont AddFont(String a_id, String a_font, int a_fontSize, int a_padX, int a_padY, int a_spaceX)
    {
        tempFont =  new GameFont(this.book.getContext().getAssets(), a_font, a_fontSize, a_padX, a_padY, a_spaceX);
        this._AddResource(a_id, this.fonts, tempFont);
        if (this.book.gameRenderer.oglReady == true)
        {
            tempFont.Load();
            tempFont.SetDisplayScale(this.book.gameRenderer.horzVertexRatio, this.book.gameRenderer.vertVertexRatio);
        }
        return (GameFont) this._GetResource(a_id, this.fonts);
    }

    /*
        Function: AddFont
            Stores an image using an id to later retrieve it. letter-padding is set to 2px

        Parameter:
            a_id        - String    | ID of the stored font
            a_font      - String    | path of the font-file inside the assets-folder
            a_fontSize  - Integer   | FontSize to use
            a_spaceX    - Integer   | FontSpacing to use

        Returns:
            GameFont -> - The Loaded font
    */
    public GameFont AddFont(String a_id, String a_font, int a_fontSize, int a_spaceX)
    {
        return this.AddFont(a_id, a_font, a_fontSize, 2, 2, a_spaceX);
    }

    /*
        Function: AddFont
            Stores an image using an id to later retrieve it. letter-padding is set to 2px

        Parameter:
            a_id        - String    | ID of the stored font
            a_font      - String    | path of the font-file inside the assets-folder
            a_fontSize  - Integer   | FontSize to use

        Returns:
            GameFont -> - The Loaded font
    */
    public GameFont AddFont(String a_id, String a_font, int a_fontSize)
    {
        return this.AddFont(a_id, a_font, a_fontSize, 2, 2, 0);
    }

    /*
        Function: RemoveImage
            Removes a previously stored font from the ResourceManager

        Parameter:
            a_id    - String    | ID of the font to remove
    */
    public void RemoveFont(String a_id)
    {
        this._RemoveResource(a_id, this.fonts);
    }

    /*
        Function: GetImage
            Gets a previously stored Tileset

        Parameter:
            a_id    - String    | ID of the stored Tileset

        Returns:
            Tileset -> - The requested Tileset
    */
    public Tileset GetTileset(String a_id)
    {
        return (Tileset) this._GetResource(a_id, this.tilesets);
    }

    /*
        Function: AddTileset
            Stores a tileset using an id to later retrieve it

        Parameter:
            a_id            - String    | ID of the stored tileset
            a_image         - Bitmap    | Image to use
            a_tileWidth     - Integer   | width of the tiles
            a_tileHeight    - Integer   | height of the tiles

        Returns:
            Tileset -> - The Tileset that just got stored
    */
    public Tileset AddTileset(String a_id, Bitmap a_image, int a_tileWidth, int a_tileHeight)
    {
        tempTileset = new Tileset(a_image, a_tileWidth, a_tileHeight);
        if (this.book.gameRenderer.oglReady == true)
            tempTileset.CreateTexture();

        return (Tileset) this._AddResource(a_id, this.tilesets, tempTileset);
    }

    /*
        Function: AddTileset
            Stores a tileset using an id to later retrieve it

        Parameter:
            a_id            - String    | ID of the stored tileset
            a_image         - Resource  | Resource of the image to use
            a_tileWidth     - Integer   | width of the tiles
            a_tileHeight    - Integer   | height of the tiles

        Returns:
            Tileset -> - The Tileset that just got stored
    */
    public Tileset AddTileset(String a_id, int a_image, int a_tileWidth, int a_tileHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = this.book.bitmapConfig;
        options.inScaled = false;
        return this.AddTileset(a_id, BitmapFactory.decodeResource(this.book.getContext().getResources(), a_image, options), a_tileWidth, a_tileHeight);
    }

    /*
        Function: AddTileset
            Stores a tileset using an id to later retrieve it

        Parameter:
            a_id            - String    | ID of the stored tileset
            a_image         - String    | Path to the image to store
            a_tileWidth     - Integer   | width of the tiles
            a_tileHeight    - Integer   | height of the tiles

        Returns:
            Tileset -> - The Tileset that just got stored
    */
    public Tileset AddTileset(String a_id, String a_path, int a_tileWidth, int a_tileHeight)
    {
        return this.AddTileset(a_id, BitmapFactory.decodeFile(a_path), a_tileWidth, a_tileHeight);
    }

    /*
        Function: RemoveImage
            Removes a previously stored tileset from the ResourceManager

        Parameter:
            a_id    - String    | ID of the tileset to remove
    */
    public void RemoveTileset(String a_id)
    {
        this._RemoveResource(a_id, this.tilesets);
    }

    public void _OGLReady()
    {
        this.tempStack = this.fonts;
        while (this.tempStack.content != null)
        {
            if (!((GameFont) this.tempStack.content.resource).fontLoaded)
                ((GameFont) this.tempStack.content.resource).Load();

            this.tempStack = this.tempStack.next;
        }

        this.tempStack = this.tilesets;
        while (this.tempStack.content != null)
        {
            if (!((Tileset) this.tempStack.content.resource).textureCreated)
                ((Tileset) this.tempStack.content.resource).CreateTexture();

            this.tempStack = this.tempStack.next;
        }
    }

    public void _UpdateScreenDimensions(float a_horzVertexRatio, float a_vertVertexRatio)
    {
        this.tempStack = this.fonts;
        while (this.tempStack.content != null)
        {
            ((GameFont) this.tempStack.content.resource).SetDisplayScale(a_horzVertexRatio, a_vertVertexRatio);
            this.tempStack = this.tempStack.next;
        }
    }

    private Object _GetResource(String a_id, GameStack<GameResource> a_targetStack)
    {
        this.tempStack = a_targetStack;
        while (this.tempStack.content != null)
        {
            if (this.tempStack.content.id.equals(a_id))
                return this.tempStack.content.resource;

            this.tempStack = this.tempStack.next;
        }
        return null;
    }

    private Object _AddResource(String a_id, GameStack<GameResource> a_targetStack, Object a_resource)
    {
        if (this._GetResource(a_id, a_targetStack) != null)
            throw new RuntimeException("resource already registered: " + a_id);

        this.tempResource = new GameResource(a_id, a_resource);
        a_targetStack.push(this.tempResource);
        this.tempResource = null;
        return a_targetStack.peek().resource;
    }

    private void _RemoveResource(String a_id, GameStack<GameResource> a_targetStack)
    {
        this.tempStack = a_targetStack;
        while (this.tempStack.content != null)
        {
            if (this.tempStack.content.id.equals(a_id))
            {
                this.tempStack.pop();
                return;
            }

            this.tempStack = this.tempStack.next;
        }
    }

    class GameResource
    {
        String id;
        Object resource;
        public GameResource(String a_id)
        {
            this.id = a_id;
        }

        public GameResource(String a_id, Object a_resource)
        {
            this.id = a_id;
            this.resource = a_resource;
        }
    }
}

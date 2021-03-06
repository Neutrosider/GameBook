package com.libraries.heiko.gamebook;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.v7.widget.FitWindowsViewGroup;

import com.libraries.heiko.gamebook.tools.GameStack;

/**
 * Created by heiko on 19.02.2016.
 */
public class GameElement
{
    public String id;                               // The ID of the GameElement
    protected GamePage page;                        // Reference to the GamePage that created this element
    protected GameBook book;                        // Reference to the GameBook that created this element
    protected GameElement parent;                   // Reference to the GameElement that acts as the Parent of this elemente

    protected Object value;                         // the current value of the element
    protected int x = 0;                            // the x-position of the element
    protected int y = 0;                            // the y-position of the element
    protected int width = 0;                        // The width of the elemenet
    protected int height = 0;                       // The height of the element

    protected float vectorX = 0;					// The x-position of the element to be used in vertexShaders
    protected float vectorY = 0;					// The x-position of the element to be used in vertexShaders
    protected float vectorWidth = 0;				// The width of the element to be used in vertexShaders
    protected float vectorHeight = 0;				// The height of the element to be used in vertexShaders

    protected boolean visible = true;               // true: The GameElement is visible, false: The GameElement is not visible
    protected boolean hideOverflow = false;         // true: childelements visually can't be oudside this element, false: they can
    protected float zIndex = 0;                     // z-index of the Element. Elements with a lower z-index will be drawn first (below other pages)
    protected boolean needsUpdate = false;          // true: the element needs to be updated every frame, false: the element doesn't need updating
    float drawOrder = 0;                            // z-index of the Element. Elements with a lower z-index will be drawn first (below other pages)

    // cache-variables to prevent memory-allocations
    protected GameStack<GAnimation> animations;       // Stack of the currently active animations
    protected GameStack<GameElement> children;        // Stack of the child-elements

    // cache-variables to prevent memory-allocations
    private GameStack<GAnimation> drawAnimations;   // used by the Draw function to iterate through the animations
    private GameStack<GameElement> drawElements;    // used by the Draw function to iterate through the child-elements
    private GameStack<GAnimation> tempAnimations;   // used by everything but the Draw function to iterate through the child-elements
    private GameStack<GameElement> tempElements;    // used by everything but the Draw function to iterate through the child-elements
    private GameStack<GameElement> tempElements2;   // used by everything but the Draw function to iterate through the child-elements
    private GameElement tempElement;                // used to cache a GameElement

    // OpenGL-Stuff
    private float[] elementMvpMatrix;               // projection matrix including animations for this element and subelements
    public GameElement(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        this.id = a_id;
        this.page = a_page;
        this.book = a_book;
        this.parent = a_parent;

        this.children = new GameStack<GameElement>();
        this.animations = new GameStack<GAnimation>();
        this.elementMvpMatrix = new float[16];
    }

    /*
        Function: AddChild
            Registers a GameElement as a child of this element

        Parameter:
            a_element   - GameElement   | The GameElement to register
    */
    public GameElement AddChild(GameElement a_element)
    {
        this.children.push(a_element);
        return a_element;
    }

    /*
        Function: RemoveChild
            Removes a direct child of this GameElement

        Parameter:
            a_id   - String   | The id of the child to remove
    */
    public void RemoveChild(String a_id)
    {
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            if (this.tempElements.content.id.equals(a_id))
            {
                this.tempElements.content.RemoveAllChildren();
                this.tempElements.pop();
                return;
            }
        }
    }

    /*
        Function: RemoveAllChildren
            Removes all child-elements of this element
    */
    public void RemoveAllChildren()
    {
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            this.tempElements.content.RemoveAllChildren();
            this.tempElements.pop();
        }
    }

    /*
        Function: GetElement
            Returns the GameElement with the given ID

        Parameter:
            a_id    - String    | The ID of the element

        Returns:
            GameElement -> - The GameElement with the given ID
    */
    public GameElement GetElement(String a_id)
    {
        this.tempElements = this.children;
        while (this.tempElements != null && this.tempElements.content != null)
        {
            if (this.tempElements.content.id.equals(a_id))
                return this.tempElements.content;

            this.tempElement = this.tempElements.content.GetElement(a_id);
            if (this.tempElement != null)
                return this.tempElement;

            this.tempElements = this.tempElements.next;
        }

        return null;
    }

    /*
        Function: SetValue
            Returns the GameElement with the given ID

        Parameter:
            a_id    - String    | The ID of the element

        Returns:
            GameElement -> - The GameElement with the given ID
    */
    public void SetValue(Object a_value)
    {
        this.value = a_value;
    }

    /*
        Function: SetSize
            Sets the size of the element relative to its parent

        Parameter:
            a_width     - Integer   | width to set
            a_height    - Integer   | height to set
    */
    public void SetSize(int a_width, int a_height)
    {
        this.width = a_width;
        this.height = a_height;
		this.vectorWidth = ((float) this.width / this.book.gameWidth) * this.book.gameRenderer.width;
		this.vectorHeight = ((float) this.height / this.book.gameHeight) * this.book.gameRenderer.height;
        this.SetPosition(this.x, this.y);
    }

    /*
        Function: SetPosition
            Sets the position of the element relative to its parent

        Parameter:
            a_x - Integer   | x-position to set
            a_y - Integer   | y-position to set
    */
    public void SetPosition(int a_x, int a_y)
    {
        this.x = a_x;
        this.y = a_y;
		this.vectorX = this.book.gameRenderer.left + ((float) this.x / this.book.gameWidth) * this.book.gameRenderer.width;
        if (this.parent == null)
		    this.vectorY = this.book.gameRenderer.bottom + ((float) (this.book.gameHeight - (this.y + this.height)) / this.book.gameHeight) * this.book.gameRenderer.height;
        else
            this.vectorY = this.book.gameRenderer.bottom + ((float) (this.parent.height - (this.y + this.height)) / this.book.gameHeight) * this.book.gameRenderer.height;
    }

    /*
        Function: SetDrawOrder
            Sets the drawOrder-Index of this element. Higher draworder = later rendering = above other elements

        Parameter:
            a_drawOrder - Integer   | The drawOrder-index this element should get
    */
    public void SetDrawOrder(int a_drawOrder)
    {
        if (this.drawOrder == a_drawOrder)
            return;

        if (this.parent != null)
            this.parent.SetChildDrawOrder(this.id, a_drawOrder);
        else
            this.page.SetChildDrawOrder(this.id, a_drawOrder);
    }

    /*
        Function: SetChildDrawOrder
            Sets the drawOrder-Index of a child-element of this element. Higher draworder = later rendering = above other elements

        Parameter:
            a_drawOrder - Integer   | The drawOrder-index the element should get
    */
    public void SetChildDrawOrder(String a_id, int a_drawOrder)
    {
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            if (this.tempElements.content.id.equals(a_id))
                break;

            this.tempElements = this.tempElements.next;
        }

        // if the element with the given ID was not found, abort
        if (this.tempElements.content == null)
            return;

        this.tempElements2 = this.tempElements;
        if (this.tempElements.content.drawOrder > a_drawOrder)
            this.tempElements = this.children;

        // find the first entry that has a higher zIndex than the element should get
        while (this.tempElements.content != null)
        {
            if (this.tempElements.content.drawOrder > a_drawOrder)
                break;

            this.tempElements = this.tempElements.next;
        }

        // remove the Element from its old position and push it to the new one
        this.tempElements.push(this.tempElements2.pop());
        this.tempElements.content.drawOrder = a_drawOrder;
    }

    /*
        Function: HideOverflow
            Enables or disables subelement-overflow-clipping

        Parameter:
            a_hideOverflow  - boolean   | true: overflowing content will be clipped, false: content won't be clipped
    */
    public void HideOverflow(boolean a_hideOverflow)
    {
        this.hideOverflow = a_hideOverflow;
    }

    // calculates frame-updates that are valid for all element-types and updates the child-elemente
    final void Update(long a_timeDelta, double a_timeFactor, long a_timePassed)
    {
        this.tempAnimations = this.animations;
        while (this.tempAnimations.content != null)
        {
            this.tempAnimations.content.Update(a_timeDelta, a_timeFactor, a_timePassed);
            this.tempAnimations = this.tempAnimations.next;
        }

        this._Update(a_timeDelta, a_timeFactor, a_timePassed);
        this.tempElements = this.children;
        while (this.tempElements != null && this.tempElements.content != null)
        {
            if (this.tempElements.content.needsUpdate == true)
                this.tempElements.content.Update(a_timeDelta, a_timeFactor, a_timePassed);
            this.tempElements = this.tempElements.next;
        }
    }

    // Draws this element and all its child-elements to the framebuffer
    final void Draw(float[] a_mvpMatrix, int a_zIndex)
    {
        if (!this.visible)
            return;

        for (int i = 0; i < 16; i++)
        {
            this.elementMvpMatrix[i] = a_mvpMatrix[i];
        }

		Matrix.translateM(this.elementMvpMatrix, 0, this.vectorX + this.vectorWidth/2, this.vectorY + this.vectorHeight/2, -(this.zIndex + 1));
        this.drawAnimations = this.animations;
        while (this.drawAnimations.content != null)
        {
            this.drawAnimations.content.Apply(this.elementMvpMatrix);
            this.drawAnimations = this.drawAnimations.next;
        }
        Matrix.translateM(this.elementMvpMatrix, 0, -this.vectorWidth / 2 - this.vectorX, -this.vectorHeight / 2 - this.vectorY, (this.zIndex + 1));

		// activate the usage of the currently set stencil/mask and draw the Element
        GLES20.glDepthMask(false);
        GLES20.glStencilMask(0x00);
        GLES20.glStencilFunc(GLES20.GL_EQUAL, a_zIndex, 0xFF);
        this._Draw(this.elementMvpMatrix);

        // apply masks, this potentially increases the z-index
        if (this.hideOverflow)
            a_zIndex = this._ApplyMask(this.elementMvpMatrix, a_zIndex);

		Matrix.translateM(this.elementMvpMatrix, 0, this.vectorX - this.book.gameRenderer.left, this.vectorY - this.book.gameRenderer.bottom, 0);
        this.drawElements = this.children;
        while (this.drawElements.content != null)
        {
            this.drawElements.content.Draw(this.elementMvpMatrix, a_zIndex);
            this.drawElements = this.drawElements.next;
        }
    }

	// gets called once OpenGL is ready to be used
    final void OGLReady()
    {
        this._OGLReady();
        this.drawElements = this.children;
        while (this.drawElements.content != null)
        {
            this.drawElements.content.OGLReady();
            this.drawElements = this.drawElements.next;
        }
    }

	// gets called when the screen changes (e.g. on orientiation change, and on startup)
	final void UpdateScreenDimensions(float a_horzVertexRatio, float a_vertVertexRatio)
	{
		this.SetPosition(this.x, this.y);
		this.SetSize(this.width, this.height);

		this._UpdateScreenDimensions(a_horzVertexRatio, a_vertVertexRatio);
		this.drawElements = this.children;
		while (this.drawElements.content != null)
		{
			this.drawElements.content.UpdateScreenDimensions(a_horzVertexRatio, a_vertVertexRatio);
			this.drawElements = this.drawElements.next;
		}
	}

    protected final void EnableUpdating()
    {
        this.needsUpdate = true;
        if (this.parent != null)
            this.parent.EnableUpdating();
    }

    // placeholder for the _OGLReady-function. Can be overwritten by the actual controls
    protected void _OGLReady()
    {
    }

	// placeholder for the _UpdateScreenDimensions-function. Can be overwritten by the actual controls
    protected void _UpdateScreenDimensions(float a_horzVertexRatio, float a_vertVertexRatio)
	{
	}

    // placeholder for the _Update-function. Can be overwritten by the actual controls
    protected void _Update(long a_timeDelta, double a_timeFactor, long a_timePassed)
    {
    }

    // placeholder for the _Draw-function. Can be overwritten by the actual controls
    protected void _Draw(float[] a_mvpMatrix)
    {
    }

    // placeholder for the _ApplyMask-function. Can be overwritten by the actual controls
    protected int _ApplyMask(float[] a_mvpMatrix, int a_zIndex)
    {
        return a_zIndex;
    }
}
